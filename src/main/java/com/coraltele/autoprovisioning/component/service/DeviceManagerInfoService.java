package com.coraltele.autoprovisioning.component.service;

import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerInfo;
import com.coraltele.autoprovisioning.component.helper.MongoConfig;
import com.coraltele.autoprovisioning.component.helper.RequestResponse;
import com.coraltele.autoprovisioning.component.repository.DeviceManagerInfoRespository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DeviceManagerInfoService {

    private final MongoTemplate mongoTemplate;
    private static final Logger logger = LogManager.getLogger(DeviceManagerInfoService.class);

    @Autowired
    public DeviceManagerInfoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    private DeviceManagerService deviceService;

    @Autowired
    private DeviceManagerInfoRespository repository;

    public static String convertToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return "";
        }
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd','HH:mm:ss");
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, inputFormatter);
            return localDateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    // new
    public void getAllDevices(String token) {
        try {
            List<Document> data = mongoTemplate.findAll(Document.class, MongoConfig.CollectionName);
            for (Document document : data) {
                try {
                    Object deviceIdObject = document.get("_deviceId");
                    Object deviceIdObject2 = document.get("_id");
                    if (deviceIdObject instanceof Document) {
                        Document deviceIdDoc = (Document) deviceIdObject;
                        Document device = document.get("Device", Document.class);
                        String id = deviceIdObject2 != null ? deviceIdObject2.toString() : null;
                        String productClass = null;
                        if (id != null) {
                            String[] parts = id.split("-");
                            if (parts.length > 1) {
                                productClass = parts[1];
                            }
                        }
                        String manufacturer = deviceIdDoc.getString("_Manufacturer");
                        String oui = deviceIdDoc.getString("_OUI");
                        String macAddress = deviceIdDoc.getString("_SerialNumber");
                        String registered = convertToLocalDateTime(document.get("_registered").toString());
                        String timestamp = "";
                        String lastInform = convertToLocalDateTime(document.get("_lastInform").toString());
                        try {
                            Object lastInformObj = document.get("_timestamp");
                            if (lastInformObj != null) {
                                timestamp = convertToLocalDateTime(lastInformObj.toString());
                            }
                        } catch (Exception E) {
                        }
                        boolean active = false;
                        String deviceId = oui + "-" + productClass + "-" + macAddress;
                        String ping = "";
                        String ipAddress = "";
                        String lastRebootTime = "";
                        try {
                            if (device != null) {
                                Document lan = device.get("LAN", Document.class);
                                if (lan != null) {
                                    Document ipAddressObject = lan.get("IPAddress", Document.class);
                                    if (ipAddressObject != null) {
                                        ipAddress = ipAddressObject.getString("_value");
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                        try {
                            Object rebootTimeObj = document.get("Reboot");
                            if (rebootTimeObj instanceof Document) {
                                lastRebootTime = convertToLocalDateTime(document.get("_timestamp").toString());
                                if (lastRebootTime == null) {
                                    lastRebootTime = "";
                                }
                            }
                        } catch (Exception e) {
                        }
                        ipAddress = macAddressToIpAddress(deviceId);
                        DeviceManagerInfo user = new DeviceManagerInfo();
                        user.setConfigurationVersion("0");
                        user.setFirmwareVersion("0");
                        user.setMacAddress(macAddress);
                        user.setOui(oui);
                        user.setProductClass(productClass);
                        user.setManufacturer(manufacturer);
                        user.setActive(active);
                        user.setPing(ping);
                        user.setLastRebootTime(lastRebootTime);
                        user.setLastInform(lastInform);
                        user.setTimeStamp(timestamp);
                        user.setRegistered(registered);
                        user.setIpAddress(ipAddress);
                        RequestResponse result = UpdateCall(lastInform, timestamp, registered, lastRebootTime, token,
                                deviceId, macAddress, ping, ipAddress);
                        if (result.getStatus() == 1) {
                            saveInfo(user);
                        }
                    }
                } catch (Exception e) {
                    logger.info("Failed to fetch single devices from loop.");
                }
            }
            return;
        } catch (Exception e) {
            logger.info("Failed to retrieve devices from MongoDB");
            return;
        }
    }

    public Iterable<DeviceManagerInfo> getInfos() {
        try {
            Iterable<DeviceManagerInfo> allData = repository.findAll();
            return allData;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public DeviceManagerInfo saveInfo(DeviceManagerInfo info) {
        return repository.save(info);
    }

    public RequestResponse UpdateCall(String lastInform, String timestamp, String registered, String lastRebootTime,
            String token, String deviceId, String macAddress, String ping,
            String ipAddress) {
        RequestResponse result = new RequestResponse();
        try {
            DeviceManagerInfo info = repository.findByMacAddress(macAddress);
            if (info != null) {
                boolean active = false;
                RequestResponse responcePing = deviceService.pingDevice(token, macAddress);
                if (responcePing.getMessage().equals("done")) {
                    active = true;
                }
                ipAddress = macAddressToIpAddress(deviceId);
                result.setStatus(0);
                result.setData(info);
                info.setActive(active);
                info.setPing(ping);
                info.setLastRebootTime(lastRebootTime);
                info.setLastInform(lastInform);
                info.setRegistered(registered);
                info.setTimeStamp(timestamp);
                info.setIpAddress(ipAddress);
                repository.save(info);
            } else {
                result.setStatus(1);
                result.setMessage("DeviceManagerInfo not found for Mac Address: " + macAddress);
            }
        } catch (Exception e) {
            result.setStatus(2);
            result.setMessage("Failed to fetch DeviceManagerInfo for Mac Address: " + macAddress);
        }
        return result;
    }

    public RequestResponse getOnlineDevices(String token) {
        RequestResponse result = new RequestResponse();
        try {
            int onlineDeviceCount = 0;
            int totalDeviceCount = 0;
            Iterable<DeviceManagerInfo> devices = getDevices();
            for (DeviceManagerInfo device : devices) {
                totalDeviceCount++;
                if (device.isActive()) {
                    onlineDeviceCount++;
                }
            }
            result.setStatus(0);
            result.setValue(onlineDeviceCount);
            result.setTotal(totalDeviceCount);
        } catch (Exception e) {
            result.setStatus(1);
            result.setMessage("Failed to fetch DeviceManagerInfo for Mac Address: ");
        }
        return result;
    }

    public Iterable<DeviceManagerInfo> getDevices() {
        return repository.findAll();
    }

    public RequestResponse deleteInfo(String macAddress) {
        RequestResponse result = new RequestResponse();
        try {
            DeviceManagerInfo res = repository.findByMacAddress(macAddress);
            if (res.getId() != null) {
                repository.deleteById(res.getId());
                result.setStatus(0);
                result.setMessage("DeviceManagerInfo deleted successfully for MAC Address: " + macAddress);
            } else {
                result.setStatus(1);
                result.setMessage("DeviceManagerInfo not found for MAC Address: " + macAddress);
            }
        } catch (Exception e) {
            result.setStatus(1);
            result.setMessage("Failed to delete DeviceManagerInfo with MAC Address: " + macAddress);
        }
        return result;
    }

    public String macAddressToIpAddress(String DeviceIdFind) {
        List<Document> data = mongoTemplate.findAll(Document.class, MongoConfig.CollectionName);
        for (Document document : data) {
            try {
                Object deviceId = document.get("_id");
                if (deviceId.equals(DeviceIdFind)) {
                    Document device = document.get("Device", Document.class);
                    if (device != null) {
                        Document lan = device.get("LAN", Document.class);
                        if (lan != null) {
                            Document ipAddressObject = lan.get("IPAddress", Document.class);
                            if (ipAddressObject != null) {
                                String ipAddress = ipAddressObject.getString("_value");
                                return ipAddress;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing document in macAddress to ipAddress.");
            }
        }
        return "-1";
    }

    public RequestResponse createInfo(DeviceManagerInfo data, String token) {
        RequestResponse result = new RequestResponse();
        try {
            DeviceManagerInfo existingInfo = repository.findByMacAddress(data.getMacAddress());
            if (existingInfo != null) {
                result.setStatus(1);
                result.setMessage("DeviceManagerInfo with Mac Address " + data.getMacAddress() + " already exists.");
                return result;
            }
            repository.save(data);
            result.setStatus(0);
            result.setMessage("DeviceManagerInfo saved successfully");
        } catch (Exception e) {
            result.setStatus(-1);
            result.setMessage("Failed to save DeviceManagerInfo");
        }
        return result;
    }

    public void SyncConfigAutoRunable() {
        try {
            String filePath = "/srv/tftp/IPfiles/";
            File directory = new File(filePath);
            Iterable<DeviceManagerInfo> allData = getInfos();

            // Check if directory exists
            if (directory.exists() && directory.isDirectory()) {
                // Loop through all devices
                for (DeviceManagerInfo device : allData) {
                    try {
                        String IPAddress = device.getIpAddress();
                        String macAddress = device.getMacAddress();
                        Boolean active = device.isActive();
                        if (!active) {
                            continue;
                        }
                        // Validate IP address and MAC address
                        if (IPAddress != null && !IPAddress.isEmpty() && macAddress != null && !macAddress.isEmpty()) {
                            String fileName = "cfg" + macAddress + ".xml";
                            String fullFilePath = filePath + fileName;

                            // Command to download the file
                            String downloadCommand = "wget --user=admin --password=admin -O " + fullFilePath
                                    + " http://"
                                    + IPAddress + "/download_xml_cfg";

                            // Execute download command
                            if (executeCommand(downloadCommand, "File downloaded successfully to: " + fullFilePath,
                                    "Failed to download file for MAC: " + macAddress + " from IP: " + IPAddress)) {

                                String chmodCommand = "sudo chmod 777 " + fullFilePath;
                                if (!executeCommand(chmodCommand,
                                        "Permissions updated successfully for: " + fullFilePath,
                                        "Failed to update permissions for: " + fullFilePath)) {
                                    logger.error("Permissions update failed for file: " + fullFilePath);
                                }

                                String find1 = "TR069_ConReqUserName";
                                String find2 = "TR069_ConReqPassword";
                                String prev1 = "<P8106 para=\"TR069_ConReqUserName\"></P8106>";
                                String prev2 = "<P8107 para=\"TR069_ConReqPassword\"></P8107>";
                                String editCommand2 = String.format("sudo sed -i '/%s/c\\%s' %s", find1, prev1,
                                        fullFilePath);
                                editCommand2 += " && sudo sed -i '/%s/c\\%s' " + fullFilePath;
                                editCommand2 = String.format(editCommand2, find2, prev2);
                                Boolean EmptyTR = executeCommand(editCommand2, "File edited successfully",
                                        "Failed to edit the file");
                                // Command to remove XML declaration line (if needed)
                                String editCommand = "sudo sed -i 's/&copy;//g' " + fullFilePath;
                                if (executeCommand(editCommand, "File edited successfully",
                                        "Failed to edit the file for MAC: " + macAddress)) {

                                }

                            }
                        } else {
                            logger.warn("Invalid IP or MAC address for device: " + device);
                        }
                    } catch (Exception deviceException) {
                        // Handle specific device-related exception and continue with the next device
                        logger.error(
                                "Exception occurred while processing device (MAC: " + device.getMacAddress() + "): ",
                                deviceException);
                    }
                }
            } else {
                logger.error("Directory not found: " + filePath);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during sync config auto runnable: ", e);
        }
    }

    private boolean executeCommand(String command, String successMessage, String errorMessage) {
        try {
            logger.info("Executing command: " + command);
            Process process = new ProcessBuilder("bash", "-c", command).start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info(successMessage);
                return true;
            } else {
                logger.error(errorMessage + ". Exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error while executing command: " + command, e);
            return false;
        }
    }

}
