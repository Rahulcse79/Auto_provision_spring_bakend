package com.coraltele.autoprovisioning.component.service;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import com.coraltele.autoprovisioning.component.helper.ApplyChangesRequest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.NodeList;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerAutoDeploy;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerHistory;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerInfo;
import com.coraltele.autoprovisioning.component.entity.PostData;
import com.coraltele.autoprovisioning.component.helper.Constants;
import com.coraltele.autoprovisioning.component.helper.RequestResponse;
import com.coraltele.autoprovisioning.component.helper.fileData;
import com.coraltele.autoprovisioning.component.model.DeviceManagerInfoModel;
import com.coraltele.autoprovisioning.component.repository.DeviceManagerInfoRespository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@Service
public class DeviceManagerService {

    private static final Logger logger = LogManager.getLogger(DeviceManagerService.class);

    @Autowired
    private DeviceManagerInfoRespository deviceManagerInfoRespository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DeviceManagerHistoryService deviceManagerHistoryService;

    @Autowired
    private DeviceManagerInfoService deviceManagerInfoService;

    @Autowired
    private DeviceManagerAutoDeployService deviceManagerAutoDeployService;

    private String AcsTokenForAutoDeploy;

    // Function of get product details.
    public DeviceManagerInfoModel getProductDetails(String macAddress) {
        DeviceManagerInfoModel returnValue = new DeviceManagerInfoModel();
        DeviceManagerInfo productDetail = deviceManagerInfoRespository.findByMacAddress(macAddress);

        if (productDetail != null) {
            returnValue.setConfigurationVersion(productDetail.getConfigurationVersion());
            returnValue.setFirmwareVersion(productDetail.getFirmwareVersion());
            returnValue.setMacAddress(productDetail.getMacAddress());
            returnValue.setManufacturer(productDetail.getManufacturer());
            returnValue.setOui(productDetail.getOui());
            returnValue.setProductClass(productDetail.getProductClass());
        }
        return returnValue;
    }

    // Method to read the file content.
    private byte[] readFileContent(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    // Method to reset or reboot.
    public RequestResponse resetOrReboot(String token, String macAddress, String ApiName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            String requestBody = "[{\"device\": \"" + deviceData.getOui() + "-" + deviceData.getProductClass() + "-"
                    + macAddress + "\", \"name\": \"" + ApiName + "\", \"status\": \"success\"}]";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "//api/devices/"
                    + deviceData.getOui()
                    + "-" + deviceData.getProductClass() + "-" + macAddress + "/tasks";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info(ApiName + " api call successful.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail(ApiName + " api call successfully.");
                return returnValue;
            } else {
                logger.error("API call failed with status code: ", response.getStatusCode());
                returnValue.setStatus(-1);
                returnValue.setMessage(ApiName + " API call failed.");
                return returnValue;
            }
        } catch (Exception e) {
            logger.error("Exception error :{}", e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Error while " + ApiName + " the device.");
            return returnValue;
        }
    }

    // Method to update a file.
    public RequestResponse update(String token, String macAddress, String updateCall) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            String fileName = ""; // FileName.
            String bodyFileName = ""; // Body file name.
            if (updateCall.equals("firmware")) {
                fileName = deviceData.getProductClass() + ".rom";
                bodyFileName = "1 Firmware Upgrade Image";
            } else if (updateCall.equals("configuration")) {
                fileName = "cfg" + macAddress + ".xml";
                bodyFileName = "3 Vendor Configuration File";
            }
            HttpHeaders header = new HttpHeaders();
            header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            String bodyData = "[{\"device\": \"" + deviceData.getOui() + "-" + deviceData.getProductClass() + "-"
                    + macAddress
                    + "\", \"name\": \"download\", \"fileName\": \"" + fileName
                    + "\", \"fileType\": \"" + bodyFileName + "\", \"status\": \"pending\"}]";
            HttpEntity<String> entity = new HttpEntity<>(bodyData, header);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/api/devices/"
                    + deviceData.getOui()
                    + "-"
                    + deviceData.getProductClass() + "-" + macAddress + "/tasks";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Update file api call successful.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Update file api call successfully.");
                return returnValue;
            } else {
                logger.error("Update api file failed: ");
                returnValue.setStatus(-1);
                returnValue.setMessage("Api update file failed to update.");
                return returnValue;
            }
        } catch (Exception e) {
            logger.error("Update api file failed:{}", e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Error in Update api of file failed.");
            return returnValue;
        }
    }

    // Method to upload and update a file.
    public RequestResponse upload(String token, byte[] FileData, String macAddress, String updateCall, String Version) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            if (deviceData != null) {
                String fileName = ""; // FileName.
                String fileDir = ""; // FileDir.
                String bodyFileName = ""; // BodyFileName.
                String ApiName = ""; // ApiName.
                String DeviceVersion = null;
                byte[] fileContent = null;
                if (updateCall.equals("firmware")) {
                    fileName = deviceData.getProductClass() + ".rom";
                    fileDir = "firmware";
                    bodyFileName = "1 Firmware Upgrade Image";
                    ApiName = "firmware";
                    DeviceVersion = deviceData.getFirmwareVersion();
                    int versionNumber = Integer.parseInt(DeviceVersion);
                    DeviceVersion = Integer.toString(versionNumber + 1);
                    if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setFirmwareVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current firmware version is updated in device model table." + updatedData);
                    }
                } else if (updateCall.equals("configuration")) {
                    fileName = "cfg" + macAddress + ".xml";
                    fileDir = "configs";
                    bodyFileName = "3 Vendor Configuration File";
                    ApiName = "configuration";
                    DeviceVersion = deviceData.getConfigurationVersion();
                    int versionNumber = Integer.parseInt(DeviceVersion.trim());
                    versionNumber += 1;
                    DeviceVersion = Integer.toString(versionNumber);
                    if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setConfigurationVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current configuration version is updated in device model table." + updatedData);
                    }
                }
                if (FileData == null || FileData.length == 0) {
                    String filePath = "/var/www/html/" + fileDir + "/" + fileName; // fileName;
                    fileContent = readFileContent(filePath);
                } else {
                    fileContent = FileData;
                    if (updateCall.equals("configuration")) {
                        DeviceVersion = deviceData.getConfigurationVersion();
                    } else if (updateCall.equals("firmware")) {
                        DeviceVersion = deviceData.getFirmwareVersion();
                    }
                    int versionNumber = Integer.parseInt(DeviceVersion);
                    DeviceVersion = Integer.toString(versionNumber + 1);
                    if (DeviceVersion != null && !DeviceVersion.isEmpty()
                            && updateCall.equals("configuration")) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setConfigurationVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current configuration version is updated in device model table." + updatedData);
                    } else if (DeviceVersion != null && !DeviceVersion.isEmpty()
                            && updateCall.equals("firmware")) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setFirmwareVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current firmware version is updated in device model table." + updatedData);
                    }
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
                headers.set("Metadata-Filetype", bodyFileName);
                headers.set("Metadata-Oui", deviceData.getOui());
                headers.set("Metadata-Productclass", deviceData.getProductClass());
                headers.set("Metadata-Version", DeviceVersion);
                HttpEntity<byte[]> entity = new HttpEntity<>(fileContent, headers);
                String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/api/files/"
                        + fileName;
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                        String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    RequestResponse updateResponse = update(token, macAddress, updateCall);
                    RequestResponse setHistoryResponse = setHistory(macAddress, fileContent,
                            DeviceVersion,
                            deviceData.getProductClass(), bodyFileName, fileName);
                    logger.info(ApiName + " File upload and update api call successfully.");
                    returnValue.setStatus(0);
                    returnValue.setMessage(response.getBody());
                    returnValue.setMessageDetail(ApiName + " File upload and update api call successfully.");
                    if (updateResponse.getStatus() == -1) {
                        returnValue.setStatus(-1);
                        returnValue.setMessage("API to upload " + ApiName + " file upload and update fail.");
                        return returnValue;
                    } else if (setHistoryResponse.getStatus() == -1) {
                        returnValue.setStatus(-1);
                        returnValue.setMessage("API to history " + ApiName + " file upload fail.");
                        return returnValue;
                    } else {
                        return returnValue;
                    }
                } else {
                    returnValue.setStatus(-1);
                    returnValue.setMessage("Upload " + ApiName + " to device manager failed for : " + macAddress);
                    logger.info(returnValue.getMessage());
                    return returnValue;
                }
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(
                        "Upload to device manager failed for : " + macAddress
                                + " : Product class not found");
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Upload to device manager failed for : " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Api for configuration upload call.
    public RequestResponse methodOfUploadFirmware(String token, String extensionName, byte[] FileData,
            String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            String DeviceVersion = deviceData.getFirmwareVersion();
            if (DeviceVersion == null || DeviceVersion.isEmpty()) {
                DeviceVersion = "0";
            }
            int versionNumber = Integer.parseInt(DeviceVersion);
            DeviceVersion = Integer.toString(versionNumber + 1);
            String fileName = deviceData.getProductClass() + "." + extensionName;
            String bodyFileName = "1 Firmware Upgrade Image";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            headers.set("Metadata-Filetype", bodyFileName);
            headers.set("Metadata-Oui", deviceData.getOui());
            headers.set("Metadata-Productclass", deviceData.getProductClass());
            headers.set("Metadata-Version", DeviceVersion);
            HttpEntity<byte[]> entity = new HttpEntity<>(FileData, headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/api/files/"
                    + fileName;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                    DeviceManagerInfo deviceModel = data;
                    deviceModel.setFirmwareVersion(DeviceVersion);
                    DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                    logger.info("Current firmware version is updated in device model table." + updatedData);
                }
                RequestResponse setHistoryResponse = setHistory(macAddress, FileData,
                        DeviceVersion,
                        deviceData.getProductClass(), bodyFileName, fileName);
                logger.info("Firmware file upload api call successfully.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Firmware file upload api call successfully and history added.");
                if (setHistoryResponse.getStatus() == -1)
                    returnValue.setMessageDetail("Firmware file upload api call successfully and history api fail.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Firmware file upload to device manager failed for : " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Firmware file upload Internal server error macAddress: " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Api for configuration upload call.
    public RequestResponse methodOfUploadConfig(String token, String fileName, byte[] FileData, String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            String DeviceVersion = deviceData.getConfigurationVersion();
            int versionNumber = Integer.parseInt(DeviceVersion);
            DeviceVersion = Integer.toString(versionNumber + 1);
            String bodyFileName = "3 Vendor Configuration File";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            headers.set("Metadata-Filetype", bodyFileName);
            headers.set("Metadata-Oui", deviceData.getOui());
            headers.set("Metadata-Productclass", deviceData.getProductClass());
            headers.set("Metadata-Version", DeviceVersion);
            HttpEntity<byte[]> entity = new HttpEntity<>(FileData, headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/api/files/"
                    + fileName;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                    DeviceManagerInfo deviceModel = data;
                    deviceModel.setConfigurationVersion(DeviceVersion);
                    DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                    logger.info("Current configuration version is updated in device model table." + updatedData);
                }
                RequestResponse setHistoryResponse = setHistory(macAddress, FileData,
                        DeviceVersion,
                        deviceData.getProductClass(), bodyFileName, fileName);
                logger.info("Configuration file upload api call successfully.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Configuration file upload api call successfully and history added.");
                if (setHistoryResponse.getStatus() == -1)
                    returnValue
                            .setMessageDetail("Configuration file upload api call successfully and history api fail.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Configuration file upload to device manager failed for : " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Configuration file upload Internal server error macAddress: " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Function of get currect date and time.
    public static String[] getCurrentDateTimeIST() {
        LocalDateTime currentIST = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentDate = currentIST.format(dateFormatter);
        String currentTime = currentIST.format(timeFormatter);
        return new String[] { currentDate, currentTime };
    }

    // Method of set history.
    public RequestResponse setHistory(String macAddress, byte[] fileContent, String version, String productClass,
            String fileFormat, String fileName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String[] dateTimeIST = getCurrentDateTimeIST(); // Date and time.
            DeviceManagerHistory history = new DeviceManagerHistory();
            history.setFiles(fileContent);
            history.setMacAddress(macAddress);
            history.setFileName(fileName);
            history.setProductClass(productClass);
            history.setTime(dateTimeIST[1]);
            history.setDate(dateTimeIST[0]);
            history.setFileFormat(fileFormat);
            history.setVersion(version);
            DeviceManagerHistory responseData = deviceManagerHistoryService.saveHistory(history);
            if (responseData.getId() != null) {
                logger.info("A new history record was inserted successfully.");
                returnValue.setStatus(0);
                returnValue.setMessage("History inserted successfully.");
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Failed to insert history for MAC address: " + macAddress);
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Failed to insert history for MAC address: " + macAddress);
            logger.error(returnValue.getMessage(), e);
        }
        return returnValue;
    }

    // Method of get history.
    public RequestResponse getHistory(String macAddress, String version, String token, String fileFormat) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            String productClass = deviceData.getProductClass();
            RequestResponse returnValueOfUpload = new RequestResponse();
            DeviceManagerHistory responseData = deviceManagerHistoryService
                    .getHistoryByVersionAndMacAddressAndProductClassFileFormat(version, macAddress, productClass,
                            fileFormat);
            byte[] files = responseData.getFiles();
            String retrievedVersion = responseData.getVersion();
            if ((files != null) && (retrievedVersion != null)) {
                String ApiCall = "";
                if (fileFormat.equals("3 Vendor Configuration File")) {
                    ApiCall = "configuration";
                } else if (fileFormat.equals("1 Firmware Upgrade Image")) {
                    ApiCall = "firmware";
                }
                // Upload and update file.
                returnValueOfUpload = upload(token, files, macAddress, ApiCall, retrievedVersion);
            }
            if (returnValueOfUpload.getStatus() == 0) {
                returnValue.setStatus(0);
                returnValue.setMessage("History retrieved and file upload and update successfully.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("History retrieved fail.");
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setMessage("Failed to retrieve history for MAC address: " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Automatically delete backup files after three months.
    public RequestResponse deleteBackup() {
        RequestResponse returnValue = new RequestResponse();
        try {
            String[] dateTimeIST = getCurrentDateTimeIST();
            String currentDate = dateTimeIST[0];
            LocalDate currentDateParsed = LocalDate.parse(currentDate);
            List<DeviceManagerHistory> resultList = (List<DeviceManagerHistory>) deviceManagerHistoryService
                    .getHistories();
            int resultListSize = resultList.size();
            for (int i = 0; i < resultListSize; i++) {
                String macAddress = resultList.get(i).getMacAddress();
                String productclass = resultList.get(i).getProductClass();
                String fileName = resultList.get(i).getFileName();
                int id = resultList.get(i).getId();
                String version = resultList.get(i).getVersion();
                LocalDate dbDate = LocalDate.parse(resultList.get(i).getDate());
                LocalDate compareDate = currentDateParsed.minusMonths(3);
                if (dbDate.isBefore(compareDate)) {
                    int DeletedHistory = deviceManagerHistoryService.deleteHistory(id);
                    if (DeletedHistory == id) {
                        logger.info("History was deleted successfully for macaddress: " + macAddress + " version: "
                                + version + " product class: " + productclass + " file name: " + fileName);
                    } else {
                        logger.info("History was not deleted for macaddress: " + macAddress + " version: " + version
                                + " product class: " + productclass + " file name: " + fileName);
                    }
                }
            }
            returnValue.setStatus(0);
            returnValue.setMessage("Three month's back history was deleted successfully.");
            return returnValue;
        } catch (Exception e) {
            logger.error("Exception Error.", e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Exception Error.");
            return returnValue;
        }
    }

    // Method of get acs token.
    public RequestResponse ACSLoginCall() {
        RequestResponse returnValue = new RequestResponse();
        try {
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/login";
            String username = "admin";
            String password = "admin";
            String requestBody = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String Token = response.getBody();
                int startIndex = Token.indexOf(":") + 2;
                int endIndex = Token.lastIndexOf("\"");
                Token = Token.substring(startIndex, endIndex);
                returnValue.setStatus(0);
                returnValue.setMessageDetail("Get acs token call successfully.");
                returnValue.setMessage(Token);
                logger.error(returnValue.getMessageDetail());
                setAcsGlobelToken(Token);
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(null);
                returnValue.setMessageDetail("Get acs token call failed.");
                logger.error(returnValue.getMessageDetail());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage(null);
            returnValue.setMessageDetail("Exception error in get acs token call failed.");
            logger.error(returnValue.getMessageDetail(), e);
            return returnValue;
        }
    }

    public String getAcsGlobelToken() {
        if (AcsTokenForAutoDeploy == null) {
            RequestResponse returnValue = ACSLoginCall();
            return returnValue.getMessage();
        }
        return AcsTokenForAutoDeploy;
    }

    public void setAcsGlobelToken(String token) {
        this.AcsTokenForAutoDeploy = token;
    }

    // Function of automatically upload and update file.
    public RequestResponse automaticallyUploadAndUpdateStart() {
        RequestResponse returnValue = new RequestResponse();
        try {
            String[] dateTimeIST = getCurrentDateTimeIST();
            String currentDate = dateTimeIST[0];
            String curretTime = dateTimeIST[1];
            String[] curretTimeParts = curretTime.split(":");
            String curretModifiedTime = curretTimeParts[0] + ":" + curretTimeParts[1];
            String token = getAcsGlobelToken();
            String version = "", fileFormatName = "", macAddress = "", fileFormat = "", productclass = "";
            byte[] fileData = null;
            List<DeviceManagerAutoDeploy> resultList = (List<DeviceManagerAutoDeploy>) deviceManagerAutoDeployService
                    .getAllAutoDeployData();
            int resultListSize = resultList.size();
            RequestResponse responseOfUpload = new RequestResponse();
            if (token != null && token != "") {
                for (int i = 0; i < resultListSize; i++) {
                    macAddress = resultList.get(i).getMacAddress();
                    productclass = resultList.get(i).getProductClass();
                    int id = resultList.get(i).getId();
                    fileData = resultList.get(i).getFiles();
                    fileFormat = resultList.get(i).getFileFormat();
                    version = resultList.get(i).getVersion();
                    String DbDate = resultList.get(i).getDate();
                    String DbTime = resultList.get(i).getTime();
                    String[] dbTimeParts = DbTime.split(":");
                    String dbModifiedTime = dbTimeParts[0] + ":" + dbTimeParts[1];
                    DbDate = DbDate.replaceAll("\\D", "");
                    currentDate = currentDate.replaceAll("\\D", "");
                    String rearrangedDate = currentDate.substring(6, 8) + // Day
                            currentDate.substring(4, 6) + // Month
                            currentDate.substring(0, 4); // Year

                    if (DbDate.equals(rearrangedDate) && dbModifiedTime.equals(curretModifiedTime)) {

                        if (fileFormat.equals("3 Vendor Configuration File")) {
                            fileFormatName = "configuration";
                        } else if (fileFormat.equals("1 Firmware Upgrade Images")) {
                            fileFormatName = "firmware";
                        }
                        responseOfUpload = upload(token, fileData, macAddress, fileFormatName, version);
                        if (responseOfUpload.getStatus() == 0) {
                            int deleteItem = deviceManagerAutoDeployService.deleteAutoDeployData(id);
                            logger.info("Device manager auto deploy row deleted successfully." + deleteItem);
                        }
                    }
                }
            }
            if (responseOfUpload.getStatus() == 0) {
                returnValue.setStatus(0);
                returnValue.setMessage(
                        "Upload and update files automatically at scheduled times and date successfully for macAddress: "
                                + macAddress + " ,fileFormat " + fileFormat + " and product class " + productclass);
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(
                        "Upload and update files automatically at scheduled times and date failed for macAddress: "
                                + macAddress + " ,fileFormat " + fileFormat + " and product class " + productclass);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Upload and update files automatically at scheduled times and date failed.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Add file in automatically update and upload api call.
    public RequestResponse addItemInAutoDeploy(String token, String macAddress, String date, String time,
            String fileFormat, byte[] fileInByte) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerAutoDeploy Data = new DeviceManagerAutoDeploy();
            String productClass = deviceData.getProductClass();
            String fileName = "";
            if (fileFormat.equals("3 Vendor Configuration File")) {
                fileName = "cfg" + macAddress + ".xml";
            } else if (fileFormat.equals("1 Firmware Upgrade Images")) {
                fileName = productClass + ".rom";
            }
            Data.setDate(date);
            Data.setTime(time);
            Data.setFiles(fileInByte);
            Data.setFileName(fileName);
            Data.setFileFormat(fileFormat);
            Data.setProductClass(productClass);
            Data.setVersion("0");
            Data.setMacAddress(macAddress);
            DeviceManagerAutoDeploy responseData = deviceManagerAutoDeployService.saveAutoDeployData(Data);
            if (responseData.getMacAddress() == macAddress) {
                returnValue.setStatus(0);
                returnValue.setMessage(
                        "Add file in automatically update and upload api call successfully macAddress: " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(
                        "Add file in automatically update and upload api call failed macAddress: " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Exception Error in automatically update and upload api call.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Method for listing devices
    public RequestResponse methodOfFileListDevices(String token) {
        RequestResponse returnValue = new RequestResponse();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port
                    + "/api/files/?filter=true";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Successfully fetched device list.");
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Failed to fetch device list. Status code: " + response.getStatusCode());
                logger.info(returnValue.getMessage());
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while fetching device list.");
            logger.error(returnValue.getMessage(), e);
        }
        return returnValue;
    }

    // Method for listing devices
    public RequestResponse methodofDeleteListDevices(String token, String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP
                    + ":" + Constants.tr069_server_Port + "/api/devices/" + deviceData.getOui() + "-"
                    + deviceData.getProductClass() + "-"
                    + macAddress + "";
            RequestResponse res = deviceManagerInfoService.deleteInfo(macAddress);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                returnValue.setStatus(0);
                returnValue.setMessage(deviceData.getMacAddress());
                returnValue.setMessageDetail("Successfully fetched device list.");
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Failed to fetch device list. Status code: " + response.getStatusCode());
                logger.info(returnValue.getMessage());
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while fetching device list.");
            logger.error(returnValue.getMessage(), e);
        }
        return returnValue;
    }

    public static String base64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static byte[] readFileContent2(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean checkFilePresence(String fileName) {
        Path path = Paths.get("/srv/tftp/IPfiles/", fileName);
        File file = path.toFile();
        if (file.exists()) {
            return true;
        } else {
            try {
                String sourcePath = "/srv/tftp/IPfiles/" + Constants.Tftp_sample_file_name;
                String destinationPath = "/srv/tftp/IPfiles/" + fileName;
                ProcessBuilder processBuilder = new ProcessBuilder("cp", sourcePath, destinationPath);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                int exitCode;
                try {
                    exitCode = process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                if (exitCode == 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }
    }

    public static void updateFileIfDifferent(String fileName, byte[] updatedFileData) {
        Path path = Paths.get("/srv/tftp/IPfiles/", fileName);
        try {
            byte[] existingFileData = Files.readAllBytes(path);
            Files.write(path, updatedFileData);
        } catch (IOException e) {
        }
    }

    public boolean updatePortToFile(String token, String filePath, byte[] fileData, PostData postData, String fileName,
            String macAddress) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(fileData));
            PostData.Account account = postData.getAccount();
            String accountNo = postData.getAccountNo();
            String profileNo = postData.getProfileNo();
            int profileNoInt = Integer.parseInt(profileNo) - 1;
            profileNo = String.valueOf(profileNoInt);
            if (account != null && account.isActive() && accountNo.equals("1")) {
                updateXmlElement(document, "P271", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24082", profileNo);
                updateXmlElement(document, "P20000", account.getLabel());
                updateXmlElement(document, "P35", account.getSipUserId());
                updateXmlElement(document, "P36", account.getAuthenticateID());
                updateXmlElement(document, "P34", account.getPassword());
                updateXmlElement(document, "P3", account.getDisplayName());
                updateXmlElement(document, "P40", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("2")) {
                updateXmlElement(document, "P401", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24083", profileNo);
                updateXmlElement(document, "P20001", account.getLabel());
                updateXmlElement(document, "P735", account.getSipUserId());
                updateXmlElement(document, "P736", account.getAuthenticateID());
                updateXmlElement(document, "P734", account.getPassword());
                updateXmlElement(document, "P703", account.getDisplayName());
                updateXmlElement(document, "P740", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("3")) {
                updateXmlElement(document, "P501", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24084", profileNo);
                updateXmlElement(document, "P20002", account.getLabel());
                updateXmlElement(document, "P504", account.getSipUserId());
                updateXmlElement(document, "P505", account.getAuthenticateID());
                updateXmlElement(document, "P506", account.getPassword());
                updateXmlElement(document, "P507", account.getDisplayName());
                updateXmlElement(document, "P513", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("4")) {
                updateXmlElement(document, "P601", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24085", profileNo);
                updateXmlElement(document, "P20003", account.getLabel());
                updateXmlElement(document, "P604", account.getSipUserId());
                updateXmlElement(document, "P605", account.getAuthenticateID());
                updateXmlElement(document, "P606", account.getPassword());
                updateXmlElement(document, "P607", account.getDisplayName());
                updateXmlElement(document, "P613", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("5")) {
                updateXmlElement(document, "P20360", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24086", profileNo);
                updateXmlElement(document, "P20378", account.getLabel());
                updateXmlElement(document, "P1704", account.getSipUserId());
                updateXmlElement(document, "P1705", account.getAuthenticateID());
                updateXmlElement(document, "P1706", account.getPassword());
                updateXmlElement(document, "P1707", account.getDisplayName());
                updateXmlElement(document, "P1713", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("6")) {
                updateXmlElement(document, "P20361", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24087", profileNo);
                updateXmlElement(document, "P20379", account.getLabel());
                updateXmlElement(document, "P1804", account.getSipUserId());
                updateXmlElement(document, "P1805", account.getAuthenticateID());
                updateXmlElement(document, "P1806", account.getPassword());
                updateXmlElement(document, "P1807", account.getDisplayName());
                updateXmlElement(document, "P1813", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("7")) {
                updateXmlElement(document, "P24090", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24088", profileNo);
                updateXmlElement(document, "P24100", account.getLabel());
                updateXmlElement(document, "P24110", account.getSipUserId());
                updateXmlElement(document, "P24120", account.getAuthenticateID());
                updateXmlElement(document, "P24130", account.getPassword());
                updateXmlElement(document, "P24140", account.getDisplayName());
                updateXmlElement(document, "P24150", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("8")) {
                updateXmlElement(document, "P24091", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24089", profileNo);
                updateXmlElement(document, "P24101", account.getLabel());
                updateXmlElement(document, "P24111", account.getSipUserId());
                updateXmlElement(document, "P24121", account.getAuthenticateID());
                updateXmlElement(document, "P24131", account.getPassword());
                updateXmlElement(document, "P24141", account.getDisplayName());
                updateXmlElement(document, "P24151", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("9")) {
                updateXmlElement(document, "P24092", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24720", profileNo);
                updateXmlElement(document, "P24102", account.getLabel());
                updateXmlElement(document, "P24112", account.getSipUserId());
                updateXmlElement(document, "P24122", account.getAuthenticateID());
                updateXmlElement(document, "P24132", account.getPassword());
                updateXmlElement(document, "P24142", account.getDisplayName());
                updateXmlElement(document, "P24152", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("10")) {
                updateXmlElement(document, "P24093", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24721", profileNo);
                updateXmlElement(document, "P24103", account.getLabel());
                updateXmlElement(document, "P24113", account.getSipUserId());
                updateXmlElement(document, "P24123", account.getAuthenticateID());
                updateXmlElement(document, "P24133", account.getPassword());
                updateXmlElement(document, "P24143", account.getDisplayName());
                updateXmlElement(document, "P24153", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("11")) {
                updateXmlElement(document, "P24094", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24722", profileNo);
                updateXmlElement(document, "P24104", account.getLabel());
                updateXmlElement(document, "P24114", account.getSipUserId());
                updateXmlElement(document, "P24124", account.getAuthenticateID());
                updateXmlElement(document, "P24134", account.getPassword());
                updateXmlElement(document, "P24144", account.getDisplayName());
                updateXmlElement(document, "P24154", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("12")) {
                updateXmlElement(document, "P24095", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24723", profileNo);
                updateXmlElement(document, "P24105", account.getLabel());
                updateXmlElement(document, "P24115", account.getSipUserId());
                updateXmlElement(document, "P24125", account.getAuthenticateID());
                updateXmlElement(document, "P24135", account.getPassword());
                updateXmlElement(document, "P24145", account.getDisplayName());
                updateXmlElement(document, "P24155", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("13")) {
                updateXmlElement(document, "P24096", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24724", profileNo);
                updateXmlElement(document, "P24106", account.getLabel());
                updateXmlElement(document, "P24116", account.getSipUserId());
                updateXmlElement(document, "P24126", account.getAuthenticateID());
                updateXmlElement(document, "P24136", account.getPassword());
                updateXmlElement(document, "P24146", account.getDisplayName());
                updateXmlElement(document, "P24156", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("14")) {
                updateXmlElement(document, "P24097", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24725", profileNo);
                updateXmlElement(document, "P24107", account.getLabel());
                updateXmlElement(document, "P24117", account.getSipUserId());
                updateXmlElement(document, "P24127", account.getAuthenticateID());
                updateXmlElement(document, "P24137", account.getPassword());
                updateXmlElement(document, "P24147", account.getDisplayName());
                updateXmlElement(document, "P24157", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("15")) {
                updateXmlElement(document, "P24098", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24726", profileNo);
                updateXmlElement(document, "P24108", account.getLabel());
                updateXmlElement(document, "P24118", account.getSipUserId());
                updateXmlElement(document, "P24128", account.getAuthenticateID());
                updateXmlElement(document, "P24138", account.getPassword());
                updateXmlElement(document, "P24148", account.getDisplayName());
                updateXmlElement(document, "P24158", String.valueOf(account.getLocalSipPort()));
            } else if (account != null && account.isActive() && accountNo.equals("16")) {
                updateXmlElement(document, "P24099", account.isActive() ? "1" : "0");
                updateXmlElement(document, "P24727", profileNo);
                updateXmlElement(document, "P24109", account.getLabel());
                updateXmlElement(document, "P24119", account.getSipUserId());
                updateXmlElement(document, "P24129", account.getAuthenticateID());
                updateXmlElement(document, "P24139", account.getPassword());
                updateXmlElement(document, "P24149", account.getDisplayName());
                updateXmlElement(document, "P24159", String.valueOf(account.getLocalSipPort()));
            }

            if (profileNo.equals("1")) {
                updateXmlElement(document, "P747", postData.getSipServer());
            } else if (profileNo.equals("2")) {
                updateXmlElement(document, "P502", postData.getSipServer());
            } else if (profileNo.equals("3")) {
                updateXmlElement(document, "P602", postData.getSipServer());
            } else if (profileNo.equals("4")) {
                updateXmlElement(document, "P20362", postData.getSipServer());
            } else if (profileNo.equals("5")) {
                updateXmlElement(document, "P20363", postData.getSipServer());
            } else {
                updateXmlElement(document, "P47", postData.getSipServer());
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no"); // No indentation to avoid extra spaces
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            byte[] updatedFileData = outputStream.toByteArray();
            updateFileIfDifferent(fileName, updatedFileData);
            RequestResponse response1 = methodOfUploadConfig(token, fileName, updatedFileData, macAddress);
            RequestResponse response2 = update(token, macAddress, "configuration");

            return (response1.getStatus() == 0 && response2.getStatus() == 0);
        } catch (Exception e) {
            System.err.println("Error updating and uploading configuration: " + e.getMessage());
            return false;
        }
    }

    private void updateXmlElement(Document document, String tagName, String value) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            element.setTextContent(value);
            printUpdatedLine(document, tagName);
        }
    }

    private void printUpdatedLine(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            String xmlLine = convertElementToString(element);
        }
    }

    private String convertElementToString(Element element) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");

            // Convert XML element to string
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            System.err.println("Error converting XML element to string: " + e.getMessage());
            return "";
        }
    }

    public RequestResponse updateSipExtension(String token, String fileName, String macAddress, PostData postData) {
        RequestResponse result = new RequestResponse();
        try {
            String filePath = "/srv/tftp/IPfiles/" + Constants.Tftp_sample_file_name;
            boolean fileExit = checkFilePresence(fileName);
            byte[] fileData;
            if (fileExit) {
                filePath = "/srv/tftp/IPfiles/" + fileName;
                fileData = readFileContent(filePath);
            } else {
                fileData = readFileContent(filePath);
            }
            boolean updateSuccess = updatePortToFile(token, filePath, fileData, postData, fileName, macAddress);
            if (updateSuccess) {
                result.setStatus(0);
                result.setMessage("SIP extension updated successfully.");
            } else {
                result.setStatus(1);
                result.setMessage("Failed to update SIP extension.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: Failed to read file. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }

    public RequestResponse downloadFile(String token, String fileName) {
        RequestResponse result = new RequestResponse();
        try {
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port
                    + "/api/blob/files/" + fileName;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] fileData = response.getBody();
                result.setFileData(fileData);
                result.setStatus(0);
                result.setMessage("File data fetched successfully.");
            } else {
                result.setStatus(1);
                result.setMessage("Failed to fetch file data. HTTP Status Code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            e.printStackTrace();
            result.setStatus(e.getStatusCode().value());
            result.setMessage("HTTP error: " + e.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }

    public RequestResponse pingDevice(String token, String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            String deviceId = data.getOui() + "-" + data.getProductClass() + "-" + macAddress;
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/api/devices/"
                    + deviceId + "/tasks";
            String rawJson = "[{\"name\":\"getParameterValues\",\"parameterNames\":[\"InternetGatewayDevice.DeviceInfo.HardwareVersion\",\"InternetGatewayDevice.DeviceInfo.SoftwareVersion\",\"InternetGatewayDevice.WANDevice.*.WANConnectionDevice.*.WANIPConnection.*.MACAddress\",\"InternetGatewayDevice.WANDevice.*.WANConnectionDevice.*.WANIPConnection.*.ExternalIPAddress\",\"InternetGatewayDevice.LANDevice.*.WLANConfiguration.*.SSID\",\"InternetGatewayDevice.LANDevice.*.WLANConfiguration.*.KeyPassphrase\",\"InternetGatewayDevice.LANDevice.*.Hosts.Host.*.HostName\",\"InternetGatewayDevice.LANDevice.*.Hosts.Host.*.IPAddress\",\"InternetGatewayDevice.LANDevice.*.Hosts.Host.*.MACAddress\"],\"device\":\""
                    + deviceId + "\",\"status\":\"pending\"}]";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(rawJson, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode arrayNode = objectMapper.readTree(responseBody);
            JsonNode firstElement = arrayNode.get(0);
            String findStatus = firstElement.get("status").asText();

            if (response.getStatusCode() == HttpStatus.OK) {
                returnValue.setStatus(0);
                returnValue.setMessage(findStatus);
                returnValue.setMessageDetail("Ping call successfully executed.");
                logger.info(returnValue.getMessageDetail());
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("pending");
                returnValue.setMessageDetail("Ping call failed with status: " + response.getStatusCode());
                logger.error(returnValue.getMessageDetail());
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("pending");
            returnValue.setMessageDetail("Exception occurred during ping call.");
            logger.error(returnValue.getMessageDetail(), e);
        }
        return returnValue;
    }

    public RequestResponse deleteFile(String token, String fileName) {
        RequestResponse result = new RequestResponse();
        try {
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port + "/api/files/"
                    + fileName;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                result.setStatus(0);
                result.setMessage("File data delete successfully.");
            } else {
                result.setStatus(1);
                result.setMessage("Failed to delete file data. HTTP Status Code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            result.setStatus(e.getStatusCode().value());
            result.setMessage("HTTP error: " + e.getStatusText());
        } catch (Exception e) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }

    // method of function.
    public RequestResponse faultList(String token) {
        RequestResponse result = new RequestResponse();
        try {
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.tr069_server_Port
                    + "/api/faults/?filter=true";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = new String(response.getBody(), StandardCharsets.UTF_8);
                result.setData(responseBody);
                result.setStatus(0);
                result.setMessage("Fault data fetch successfully.");
            } else {
                result.setStatus(1);
                result.setMessage("Failed to fault data. HTTP Status Code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            e.printStackTrace();
            result.setStatus(e.getStatusCode().value());
            result.setMessage("HTTP error: " + e.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }

    public RequestResponse updateFilePreasented(final String fileName, final byte[] fileData) {
        final RequestResponse returnValue = new RequestResponse();
        try {
            final String directoryPath = Constants.Ip_files;
            final File directory = new File(directoryPath);
            if (directory.exists() && directory.isDirectory()) {
                final File[] files = directory.listFiles();
                if (files != null) {
                    boolean fileFound = false;
                    for (File file : files) {
                        if (file.getName().equals(fileName)) {
                            Files.write(file.toPath(), fileData, StandardOpenOption.TRUNCATE_EXISTING);
                            fileFound = true;
                            break;
                        }
                    }
                    if (!fileFound) {
                        File newFile = new File(directory, fileName);
                        Files.write(newFile.toPath(), fileData);
                    }
                } else {
                    throw new RuntimeException("No files found in directory: " + directoryPath);
                }
            } else {
                throw new RuntimeException("Directory not found or is not a directory: " + directoryPath);
            }
            returnValue.setStatus(0);
            returnValue.setMessage("File data updated successfully.");
        } catch (final Exception e) {
            logger.error("Error updating file '{}': {}", fileName, e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Error updating file: " + e.getMessage());
        }
        return returnValue;
    }

    public List<fileData> getAllPhoneFiles() {
        List<fileData> fileDataList = new ArrayList<>();
        String directoryPath = Constants.Ip_files;
        try {
            File directory = new File(directoryPath);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName();
                        byte[] fileData = Files.readAllBytes(file.toPath());
                        fileDataList.add(new fileData(fileName, fileData));
                    }
                }
            } else {
                throw new RuntimeException("Directory not found: " + directoryPath);
            }
        } catch (Exception e) {
            System.err.println("Error reading directory.");
        }
        return fileDataList;
    }

    // Method to update a file.
    public RequestResponse changesInConfigFile(String token, ApplyChangesRequest changeRequest) {
        RequestResponse returnValue = new RequestResponse();
        String directoryPath = Constants.Ip_files;
        String callOfApi = changeRequest.getCallOfAPI();
        List<String> list = changeRequest.getSelectedFiles();

        try {
            List<String> successfulChanges = new ArrayList<>();
            for (String fileName : list) {

                String fullPath = directoryPath + File.separator + fileName;
                File file = new File(fullPath);
                if (!file.exists()) {
                    logger.warn("File not found: {}", fullPath);
                    continue;
                }
                StringBuilder fileContent = new StringBuilder();
                boolean isModified = false;
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (changeRequest != null && callOfApi.equals("1")) {

                            if (!changeRequest.getIpAddress().equals("")
                                    && line.contains("<P5432 para=\"LDAP_ServerAddress\"")) {
                                line = "        <P5432 para=\"LDAP_ServerAddress\">" + changeRequest.getIpAddress()
                                        + "</P5432>";
                                isModified = true;
                            }
                            if (!changeRequest.getUser().equals("") && line.contains("<P5435 para=\"LDAP_UserName\"")) {
                                line = "        <P5435 para=\"LDAP_UserName\">" + changeRequest.getUser() + "</P5435>";
                                isModified = true;
                            }
                        } else if (callOfApi.equals("2")) {
                            if (!changeRequest.getIpAddressNTP().equals("")
                                    && line.contains("<P30 para=\"NW_Adv_UrlOrIpAddress\"")) {
                                line = "        <P30 para=\"NW_Adv_UrlOrIpAddress\">" + changeRequest.getIpAddressNTP()
                                        + "</P30>";
                                isModified = true;
                            }
                        }
                        fileContent.append(line).append(System.lineSeparator());
                    }
                }
                if (isModified) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writer.write(fileContent.toString());
                    }
                    successfulChanges.add(fileName);
                }
            }

            returnValue.setStatus(0);
            returnValue.setMessage("File data changes successfully.");
            return returnValue;
        } catch (IOException e) {
            logger.error("Changes for file failed: {}", e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Error in changes for file: " + e.getMessage());
            return returnValue;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error occurred: " + e.getMessage());
            return returnValue;
        }
    }

    public RequestResponse updateByFile(String token, String fileName, String macAddress) {
        RequestResponse result = new RequestResponse();
        try {
            String filePath = "/srv/tftp/IPfiles/" + Constants.Tftp_sample_file_name;
            boolean fileExit = checkFilePresence(fileName);
            byte[] fileData;
            if (fileExit) {
                filePath = "/srv/tftp/IPfiles/" + fileName;
                fileData = readFileContent(filePath);
            } else {
                fileData = readFileContent(filePath);
            }
            RequestResponse response1 = methodOfUploadConfig(token, fileName, fileData, macAddress);
            RequestResponse response2 = new RequestResponse();
            if (response1.getStatus() == 0) {
                response2 = update(token, macAddress, "configuration");
            }
            if ((response1.getStatus() == 0 && response2.getStatus() == 0)) {
                result.setStatus(0);
                result.setMessage("Updated successfully.");
            } else {
                result.setStatus(1);
                result.setMessage("Failed to update.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: Failed to read file. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }

    public RequestResponse SyncConfig(String fileName, String macAddress) {
        RequestResponse result = new RequestResponse();
        String filePath = "/srv/tftp/IPfiles/";
        File directory = new File(filePath);

        try {
            DeviceManagerInfo productDetail = deviceManagerInfoRespository.findByMacAddress(macAddress);
            if (productDetail == null || productDetail.getIpAddress() == null
                    || productDetail.getIpAddress().isEmpty()) {
                logger.error("No IP address found for MAC: " + macAddress);
                result.setStatus(1);
                result.setMessage("Invalid MAC address or no IP address found.");
                return result;
            }

            String ipAddress = productDetail.getIpAddress();
            Boolean active = productDetail.isActive();
            if (!active) {
                result.setStatus(1);
                result.setMessage("Device offline.");
            }
            logger.info("Device IP Address: " + ipAddress);

            if (!directory.exists() || !directory.isDirectory()) {
                logger.error("Directory not found: " + filePath);
                result.setStatus(1);
                result.setMessage("Directory does not exist: " + filePath);
                return result;
            }

            String fullFilePath = filePath + fileName;
            String downloadCommand = String.format(
                    "wget --user=admin --password=admin -O %s http://%s/download_xml_cfg", fullFilePath, ipAddress);

            // Execute file download command
            if (executeCommand(downloadCommand, "File downloaded successfully", "Failed to download the file")) {
                String chmodCommand = "sudo chmod 777 " + fullFilePath;

                // Execute chmod command
                if (executeCommand(chmodCommand, "Permissions updated successfully", "Failed to set permissions")) {
                    String editCommand = "sudo sed -i 's/&copy;//g' " + fullFilePath;
                    String find1 = "TR069_ConReqUserName";
                    String find2 = "TR069_ConReqPassword";
                    String prev1 = "<P8106 para=\"TR069_ConReqUserName\"></P8106>";
                    String prev2 = "<P8107 para=\"TR069_ConReqPassword\"></P8107>";
                    String editCommand2 = String.format("sudo sed -i '/%s/c\\%s' %s", find1, prev1, fullFilePath);
                    editCommand2 += " && sudo sed -i '/%s/c\\%s' " + fullFilePath;
                    editCommand2 = String.format(editCommand2, find2, prev2);
                    Boolean EmptyTR = executeCommand(editCommand2, "File edited successfully", "Failed to edit the file");
                    
                    // Execute file edit command
                    if (executeCommand(editCommand, "File edited successfully", "Failed to edit the file") && EmptyTR) {
                        result.setStatus(0);
                        result.setMessage("File downloaded, permissions set, and file edited successfully.");
                    } else {
                        result.setStatus(0);
                        result.setMessage("File downloaded and permissions set, but editing failed.");
                    }
                } else {
                    result.setStatus(1);
                    result.setMessage("File downloaded, but failed to set permissions.");
                }
            } else {
                result.setStatus(1);
                result.setMessage("Failed to download the file.");
            }
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            result.setStatus(1);
            result.setMessage("Internal server error: " + e.getMessage());
        }

        return result;
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

    public Boolean getChangeMacAddress(String oldMacAddress, String macAddress) {
        String directoryPath = Constants.Ip_files;
        String oldFileName = "cfg" + oldMacAddress + ".xml";
        String newFileName = "cfg" + macAddress + ".xml";
        try {
            File directory = new File(directoryPath);
            if (directory.exists() && directory.isDirectory()) {
                File oldFile = new File(directoryPath + File.separator + oldFileName);
                File newFile = new File(directoryPath + File.separator + newFileName);
                if (oldFile.exists()) {
                    if (!newFile.exists()) {
                        ProcessBuilder processBuilder = new ProcessBuilder("mv", oldFile.getAbsolutePath(),
                                newFile.getAbsolutePath());
                        processBuilder.directory(directory);
                        Process process = processBuilder.start();
                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            logger.info("File renamed successfully from " + oldFileName + " to " + newFileName);
                            return true;
                        } else {
                            logger.info("Error renaming the file.");
                            return false;
                        }
                    } else {
                        logger.info("File with new name already exists: " + newFileName);
                        return false;
                    }
                } else {
                    logger.info("Old file does not exist: " + oldFileName);
                    return false;
                }
            } else {
                throw new RuntimeException("Directory not found: " + directoryPath);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error processing files: " + e.getMessage(), e);
            return false;
        }
    }

}
