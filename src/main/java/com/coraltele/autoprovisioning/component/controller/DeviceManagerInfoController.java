package com.coraltele.autoprovisioning.component.controller;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerInfo;
import com.coraltele.autoprovisioning.component.helper.RequestResponse;
import com.coraltele.autoprovisioning.component.service.DeviceManagerInfoService;

@CrossOrigin
@RequestMapping("/api/deviceManagerInfo")
@RestController
public class DeviceManagerInfoController {

    @Autowired
    private DeviceManagerInfoService service;

    @PostMapping("/createinfo")
    public ResponseEntity<RequestResponse> createinfodata(@RequestHeader String productClass, @RequestHeader String oui,
            @RequestHeader("Authorization") String authHeader, @RequestHeader String macAddress,
            @RequestHeader String manufacturer) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String token = authHeader.substring(7);
            DeviceManagerInfo data = new DeviceManagerInfo();
            data.setConfigurationVersion("0");
            data.setFirmwareVersion("0");
            data.setMacAddress(macAddress);
            data.setManufacturer(manufacturer);
            data.setOui(oui);
            data.setProductClass(productClass);

            returnValue = service.createInfo(data, token);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else if (returnValue.getStatus() == 1) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Failed to create DeviceManagerInfo.");
                return ResponseEntity.badRequest().body(returnValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create DeviceManagerInfo");
        }
    }

    // Api of online devices.
    @GetMapping("/onlineDevices")
    public ResponseEntity<RequestResponse> methodOfOnlineDevices(
            @RequestHeader("Authorization") String authHeader) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String token = authHeader.substring(7);
            returnValue = service.getOnlineDevices(token);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Failed to online devices. Status: " + returnValue.getStatus());
                return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Internal server error while listing devices.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allData")
    public ResponseEntity<Iterable<DeviceManagerInfo>> getAllDevicesList(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader;
            Iterable<DeviceManagerInfo> data = service.getInfos();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/SyncConfigAll")
    public ResponseEntity<RequestResponse> getSyncAll(
            @RequestHeader("Authorization") String authHeader) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Missing or invalid Authorization token.");
                returnValue.setStatus(-1);
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            service.SyncConfigAutoRunable();
            returnValue.setMessage("Sync config completed successfully.");
            returnValue.setStatus(0);
            return new ResponseEntity<>(returnValue, HttpStatus.OK);
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Unexpected error while Sync config auto runnable.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
