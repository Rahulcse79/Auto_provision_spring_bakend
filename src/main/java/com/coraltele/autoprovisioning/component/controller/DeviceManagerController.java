package com.coraltele.autoprovisioning.component.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.coraltele.autoprovisioning.component.entity.PostData;
import com.coraltele.autoprovisioning.component.helper.ApplyChangesRequest;
import com.coraltele.autoprovisioning.component.helper.RequestResponse;
import com.coraltele.autoprovisioning.component.helper.fileData;
import com.coraltele.autoprovisioning.component.helper.provisioningConfig;
import com.coraltele.autoprovisioning.component.service.DeviceManagerService;

@RestController
@CrossOrigin
@RequestMapping("/api/deviceManager")
public class DeviceManagerController {

    private static final Logger logger = LogManager.getLogger(DeviceManagerController.class);

    @Autowired
    private DeviceManagerService deviceService;

    // Api of reboot device.
    @GetMapping("/reboot/{macAddress}")
    public ResponseEntity<RequestResponse> rebootDevice(@RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        String ApiName = "reboot";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.resetOrReboot(token, macAddress, ApiName);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Reboot api call fail.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/rebootBulk")
    public ResponseEntity<RequestResponse> rebootDeviceBulk(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<String> macAddress) {
        RequestResponse response = new RequestResponse();
        String token = authHeader.substring(7);
        logger.info("Token: " + token);
        for (String mac : macAddress) {
            try {
                RequestResponse rebootResponse = deviceService.resetOrReboot(token, mac, "reboot");

                if (rebootResponse.getStatus() != 0) {
                    logger.error("Failed to reboot device with MAC address: " + mac);
                    response.setStatus(1);
                    response.setMessage("Failed to reboot device with MAC address: " + mac);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                logger.error("Error during reboot of MAC address: " + mac, e);
                response.setStatus(1);
                response.setMessage("An internal server error occurred for MAC address: " + mac);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        response.setStatus(0);
        response.setMessage("All devices rebooted successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resetBulk")
    public ResponseEntity<RequestResponse> resetDeviceBulk(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<String> macAddresses) {
        RequestResponse response = new RequestResponse();
        String token = authHeader.substring(7);
        logger.info("Token: " + token);
        String apiName = "factoryReset";
        for (String macAddress : macAddresses) {
            try {
                RequestResponse resetResponse = deviceService.resetOrReboot(token, macAddress, apiName);
                if (resetResponse.getStatus() != 0) {
                    logger.error("Failed to reset device with MAC address: " + macAddress);
                    response.setStatus(1);
                    response.setMessage("Failed to reset device with MAC address: " + macAddress);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                logger.error("Error during reset of MAC address: " + macAddress, e);
                response.setStatus(1);
                response.setMessage("An internal server error occurred for MAC address: " + macAddress);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        response.setStatus(0);
        response.setMessage("All devices reset successfully.");
        return ResponseEntity.ok(response);
    }

    // Api of configuration file upload
    @PostMapping("/uploadConfig/{macAddress}")
    public ResponseEntity<RequestResponse> apiCallOfConfigUpload(@RequestHeader String fileName,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress, @RequestParam("file") MultipartFile file) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        try {
            byte[] fileData = file.getBytes();
            if (returnValue.getStatus() == 0) {
                returnValue = deviceService.methodOfUploadConfig(token, fileName, fileData, macAddress);
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Api configuration file upload failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during file upload or service method invocation: {}", e.getMessage());
            returnValue.setMessage("Exception occurred during file upload or service method invocation.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api of update configuration.
    @GetMapping("/updateConfig/{macAddress}")
    public ResponseEntity<RequestResponse> updateConfig(@RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        String updateCall = "configuration";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.update(token, macAddress, updateCall);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Api configuration file update failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api of update firmware file.
    @GetMapping("/updateFirmware/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfFirmwareUpdate(
            @RequestHeader("Authorization") String authHeader, @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        String updateCall = "firmware";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.update(token, macAddress, updateCall);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Api firmware file upload and update failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api of upload firmware file.
    @PutMapping("/uploadFirmware/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfFirmwareUpload(@RequestHeader String extensionName,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress, @RequestParam("file") MultipartFile file) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        try {
            byte[] fileData = file.getBytes();
            if (returnValue.getStatus() == 0) {
                returnValue = deviceService.methodOfUploadFirmware(token, extensionName, fileData, macAddress);
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Api firmware file upload failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during file upload or service method invocation: {}", e.getMessage());
            returnValue.setMessage("Exception occurred during file upload or service method invocation.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api to fetch device manager history.
    @PostMapping("/searchHistory/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfGetHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress,
            @RequestHeader String Filetype,
            @RequestHeader String deviceVersion) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        logger.info(token);
        String version = deviceVersion;
        String updateCall = Filetype;
        boolean SetUpdateCall = false;
        if (updateCall.equals("firmware")) {
            updateCall = "1 Firmware Upgrade Image";
            SetUpdateCall = true;
        } else if (updateCall.equals("configuration")) {
            updateCall = "3 Vendor Configuration File";
            SetUpdateCall = true;
        }
        if (returnValue.getStatus() == 0 && SetUpdateCall) {
            returnValue = deviceService.getHistory(macAddress, version, token, updateCall);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Api for history failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api call of add data in automatic upload and update table.
    @PutMapping("/addFileAutoDeploy/{macAddress}")
    public ResponseEntity<RequestResponse> autoDeployFile(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress,
            @RequestHeader String dateoffile,
            @RequestHeader String time,
            @RequestHeader String Filetype,
            @RequestParam("file") MultipartFile file) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        boolean SetUpdateCall = false;
        String fileFormat = Filetype;
        try {
            if (fileFormat.equals("firmware")) {
                fileFormat = "1 Firmware Upgrade Images";
                SetUpdateCall = true;
            } else if (fileFormat.equals("configuration")) {
                fileFormat = "3 Vendor Configuration File";
                SetUpdateCall = true;
            }
            logger.info("Token: " + token + " FileFormat: " + fileFormat);

            if (file.isEmpty()) {
                returnValue.setMessage("File is empty.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }

            byte[] fileInByte = file.getBytes();

            if (returnValue.getStatus() == 0 && SetUpdateCall) {
                returnValue = deviceService.addItemInAutoDeploy(token, macAddress, dateoffile, time, fileFormat,
                        fileInByte);
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("API to add file in automatic update and upload API call failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            logger.error("Error reading file content: {}", e.getMessage());
            returnValue.setMessage("Error reading file content.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception occurred during file upload or service method invocation: {}", e.getMessage());
            returnValue.setMessage("Exception occurred during file upload or service method invocation.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api of sip ip and extension update.
    @PostMapping("/sip/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfSetExtension(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress,
            @RequestBody PostData postData) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Authorization header missing or invalid");
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);
            String fileName = "cfg" + macAddress + ".xml";
            if (postData.getAccountNo().equals("-1")) {
                String data = postData.getAccount().getSipUserId();
                String[] values = data.split(",");
                boolean result = true;
                int number = 1;
                for (String value : values) {
                    value = value.trim();
                    postData.getAccount().setSipUserId(value);
                    postData.getAccount().setDisplayName(value);
                    postData.getAccount().setLabel(value);
                    postData.getAccount().setAuthenticateID(value);
                    postData.setAccountNo(String.valueOf(number));
                    number++;
                    returnValue = deviceService.updateSipExtension(token, fileName, macAddress, postData);
                    if (returnValue.getStatus() != 0) {
                        result = false;
                    }
                }
                if (result) {
                    return ResponseEntity.ok(returnValue);
                } else {
                    returnValue.setMessage("Extension update failed.");
                    return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
                }
            } else {
                returnValue = deviceService.updateSipExtension(token, fileName, macAddress, postData);
                if (returnValue.getStatus() == 0) {
                    return ResponseEntity.ok(returnValue);
                } else {
                    returnValue.setMessage("Extension update failed.");
                    return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            returnValue.setMessage("Bad request: " + e.getMessage());
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            returnValue.setMessage("Internal server error: " + e.getMessage());
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api of list devices.
    @GetMapping("/listDevices")
    public ResponseEntity<RequestResponse> methodOfFileUploadListDevices(
            @RequestHeader("Authorization") String authHeader) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String token = authHeader.substring(7);
            returnValue = deviceService.methodOfFileListDevices(token);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Failed to list devices. Status: " + returnValue.getStatus());
                return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while listing devices.");
            logger.error(returnValue.getMessage(), e);
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api of download file.
    @GetMapping("/download_file")
    public RequestResponse methodOfDownloadFile(
            @RequestHeader("Authorization") String authHeader, @RequestHeader("FileName") String FileName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String token = authHeader.substring(7);
            returnValue = deviceService.downloadFile(token, FileName);
            if (returnValue.getStatus() == 0) {
                return returnValue;
            } else {
                returnValue.setMessage("Failed to download file. Status: " + returnValue.getStatus());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while download file.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Api of delete file.
    @DeleteMapping("/delete_file")
    public RequestResponse methodOfDeleteFile(
            @RequestHeader("Authorization") String authHeader, @RequestHeader("FileName") String FileName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String token = authHeader.substring(7);
            returnValue = deviceService.deleteFile(token, FileName);
            if (returnValue.getStatus() == 0) {
                return returnValue;
            } else {
                returnValue.setMessage("Failed to delete file. Status: " + returnValue.getStatus());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while delete file.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Api of delete list devices.
    @DeleteMapping("/deleteListItem")
    public ResponseEntity<RequestResponse> deleteListItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("macAddress") String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || macAddress == null || macAddress.isEmpty()) {
                returnValue.setStatus(-1);
                returnValue.setMessage("Invalid headers");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
            String token = authHeader.substring(7);
            returnValue = deviceService.methodofDeleteListDevices(token, macAddress);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Failed to delete device. Status: " + returnValue.getStatus());
                return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while deleting device.");
            logger.error(returnValue.getMessage(), e);
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API to get the fault list
    @GetMapping("/faultList")
    public ResponseEntity<RequestResponse> getFaultList(
            @RequestHeader("Authorization") String authHeader) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Authorization header missing or invalid");
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);
            returnValue = deviceService.faultList(token);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Failed to retrieve fault list.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            returnValue.setMessage("Bad request: " + e.getMessage());
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            returnValue.setMessage("Internal server error: " + e.getMessage());
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updateIpFile")
    public ResponseEntity<RequestResponse> updateIpFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
            if (token == null || token.isEmpty()) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            if (file.isEmpty()) {
                returnValue.setMessage("File is empty.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
            byte[] fileInByte = file.getBytes();
            returnValue = deviceService.updateFilePreasented(fileName, fileInByte);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("API configuration file changes failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            logger.error("Error reading file content for file: {}: {}", fileName, e.getMessage());
            returnValue.setMessage("Error reading file content.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            logger.error("Failed to update file: {}: {}", fileName, e.getMessage());
            returnValue.setMessage("Internal server error while updating files.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error while updating file: {}: {}", fileName, e.getMessage());
            returnValue.setMessage("Unexpected error occurred while updating the file.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allPhoneFile")
    public ResponseEntity<List<fileData>> getAllFiles() {
        try {
            List<fileData> fileDataList = deviceService.getAllPhoneFiles();
            if (!fileDataList.isEmpty()) {
                return new ResponseEntity<>(fileDataList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (RuntimeException e) {
            logger.error("Fail to fetch alll phone files.");
            return null;
        }
    }

    @PostMapping("/ApplyChanges")
    public ResponseEntity<RequestResponse> updateConfigChanges(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ApplyChangesRequest updateRequest) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.changesInConfigFile(token, updateRequest);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("API configuration file changes failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/bulkProvisioning")
    public ResponseEntity<RequestResponse> bulkProvisioning(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody provisioningConfig provisioningConfig) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);
            String sipServerIp = provisioningConfig.getSipServerIp();
            String epochTime = provisioningConfig.getEpochTime();
            String sipPort = provisioningConfig.getSipPort();
            StringBuilder macAddressBuilder = new StringBuilder();
            List<String> macAddressList = new ArrayList<>();
            for (provisioningConfig.Accounts account : provisioningConfig.getAccounts()) {
                try {
                    int sipPortInt = (sipPort != null && !sipPort.isEmpty()) ? Integer.parseInt(sipPort) : 5060;
                    RequestResponse returnVal = new RequestResponse();
                    String fileName = "cfg" + account.getMacAddress() + ".xml";
                    PostData postData = new PostData();
                    postData.setSipServer(sipServerIp);
                    postData.setMacAddress(account.getMacAddress());
                    postData.setProfileNo(account.getProfile());
                    postData.setAccountNo("1");
                    PostData.Account postDataAccount = new PostData.Account();
                    postDataAccount.setActive(account.isAccountsActive());
                    postDataAccount.setAuthenticateID(account.getAuthenticateID());
                    postDataAccount.setDisplayName(account.getDisplayName());
                    postDataAccount.setLabel(account.getLabel());
                    postDataAccount.setLocalSipPort(sipPortInt);
                    postDataAccount.setPassword(account.getPassword());
                    postDataAccount.setSipUserId(account.getSipUserId());
                    postData.setAccount(postDataAccount);
                    returnVal = deviceService.updateSipExtension(token, fileName, account.getMacAddress(), postData);
                    if (returnVal.getStatus() == 0) {
                        macAddressList.add(account.getMacAddress());
                    }
                } catch (Exception e) {
                }
            }

            for (String macAddress : macAddressList) {
               macAddressBuilder.append(macAddress).append(", ");
            }
            String macAddressListData = macAddressBuilder.toString();
            logger.info("SuccessFull updated IPPhones macAddress: {} ", macAddressListData);
            returnValue.setMessageDetail(macAddressListData);
            returnValue.setMessage("Bulk provisioning completed successfully.");
            returnValue.setStatus(0);
            return new ResponseEntity<>(returnValue, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unexpected error while bulk provisioning: {}", e.getMessage(), e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error occurred during bulk provisioning.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updateByFile")
    public ResponseEntity<RequestResponse> updateByFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<String> macAddressList) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);
            List<String> ReturnMacAddressList = new ArrayList<>();
            StringBuilder macAddressBuilder = new StringBuilder();
            for (String macAddress : macAddressList) {
                RequestResponse result = new RequestResponse();
                try {
                    String fileName = "cfg" + macAddress + ".xml";
                    result = deviceService.updateByFile(token, fileName, macAddress);
                    if (result.getStatus() == 0) {
                        ReturnMacAddressList.add(macAddress);
                    }
                } catch (Exception e) {
                }
            }
            for (String macAddress : ReturnMacAddressList) {
                macAddressBuilder.append(macAddress).append(", ");
            }
            String macAddressListData = macAddressBuilder.toString();
            logger.info("SuccessFull updated IPPhones macAddress: {} ", macAddressListData);
            returnValue.setMessageDetail(macAddressListData);
            returnValue.setMessage("Bulk provisioning completed successfully.");
            returnValue.setStatus(0);
            return new ResponseEntity<>(returnValue, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unexpected error while bulk provisioning: {}", e.getMessage(), e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error occurred during bulk provisioning.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/SyncConfig")
    public ResponseEntity<RequestResponse> SyncConfig(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<String> macAddressList) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            List<String> ReturnMacAddressList = new ArrayList<>();
            StringBuilder macAddressBuilder = new StringBuilder();
            for (String macAddress : macAddressList) {
                RequestResponse result = new RequestResponse();
                try {
                    String fileName = "cfg" + macAddress + ".xml";
                    result = deviceService.SyncConfig(fileName, macAddress);
                    if (result.getStatus() == 0) {
                        ReturnMacAddressList.add(macAddress);
                    }
                } catch (Exception e) {
                }
            }
            for (String macAddress : ReturnMacAddressList) {
                macAddressBuilder.append(macAddress).append(", ");
            }
            String macAddressListData = macAddressBuilder.toString();
            logger.info("SuccessFull updated Sync config macAddress: {} ", macAddressListData);
            returnValue.setMessageDetail(macAddressListData);
            returnValue.setMessage("Sync config completed successfully.");
            returnValue.setStatus(0);
            return new ResponseEntity<>(returnValue, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unexpected error while Sync config: {}", e.getMessage(), e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error occurred during Sync config.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/changeMacAddress")
    public ResponseEntity<RequestResponse> getChangeMacAddress(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("oldMacAddress") String oldMacAddress,
            @RequestHeader("NewMacAddress") String newMacAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            if (oldMacAddress == null || newMacAddress == null) {
                returnValue.setMessage("Missing old MAC address or new MAC address.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
            boolean success = deviceService.getChangeMacAddress(oldMacAddress, newMacAddress);
            if (success) {
                logger.info("Successfully changed MAC address from {} to {}", oldMacAddress, newMacAddress);
                returnValue.setStatus(0);
                returnValue.setMessage("Successfully changed MAC address to: " + newMacAddress);
                return ResponseEntity.ok(returnValue);
            } else {
                logger.info("Failed to change MAC address from {} to {}", oldMacAddress, newMacAddress);
                returnValue.setStatus(-1);
                returnValue.setMessage("Failed to change MAC address: " + oldMacAddress);
                return ResponseEntity.status(500).body(returnValue);
            }

        } catch (Exception e) {
            logger.error("Unexpected error occurred during MAC address change", e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error occurred during MAC address change.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/FirewareBulkUpdateAndUpload")
    public ResponseEntity<RequestResponse> FirewareBulkUpdateAndUpload(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("macAddresses") List<String> macAddresses,
            @RequestParam("extensionName") String extensionName,
            @RequestParam("date") String date,
            @RequestParam("time") String time) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }

            if (file.isEmpty()) {
                returnValue.setMessage("No file uploaded.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
            String token = authHeader.substring(7);
            List<String> returnMacAddressList = new ArrayList<>();
            StringBuilder macAddressBuilder = new StringBuilder();
            byte[] fileData = file.getBytes();
            String  fileFormat = "1 Firmware Upgrade Images";
            for (String macAddress : macAddresses) {
                logger.info("Firmware add to time schedule for MAC address: {}", macAddress);
                try {
                    RequestResponse result1 = new RequestResponse();
                    result1 = deviceService.addItemInAutoDeploy(token, macAddress, date, time, fileFormat,
                    fileData);
                    if(result1.getStatus() == 0){
                        returnMacAddressList.add(macAddress);
                    }
                } catch (Exception e) {
                    logger.error("Error firmware to time schedule for macAddress: {} - {}", macAddress, e.getMessage());
                }
            }
            for (String macAddress : returnMacAddressList) {
                macAddressBuilder.append(macAddress).append(", ");
            }

            String macAddressListData = macAddressBuilder.toString();
            logger.info("Successfully firmware to time schedule for MAC addresses: {}", macAddressListData);
            returnValue.setMessageDetail(macAddressListData);
            returnValue.setMessage("Firmware bulk to time schedule completed successfully.");
            returnValue.setStatus(0);
            return new ResponseEntity<>(returnValue, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Unexpected error while bulk to time schedule firmware: {}", e.getMessage(), e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error occurred during firmware to time schedule.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
