package com.coraltele.autoprovisioning.component.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerCiscoHistory;
import com.coraltele.autoprovisioning.component.entity.PostDataOfCisco;
import com.coraltele.autoprovisioning.component.helper.RequestResponse;
import com.coraltele.autoprovisioning.component.service.DeviceManagerCiscoService;

@CrossOrigin
@RestController
@RequestMapping("/api/deviceManagerCiscoHistory")
public class DeviceManagerCiscoController {

    @Autowired
    private DeviceManagerCiscoService service;

    @GetMapping("/alldata")
    public Iterable<DeviceManagerCiscoHistory> findAll() {
        return service.getall();
    }

    @DeleteMapping("/delete/{id}")
    public int delete(@PathVariable int id) {
        return service.delete(id);
    }

    @GetMapping("/data/{macAddress}")
    public DeviceManagerCiscoHistory rebootDevice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress) {
        String token = authHeader.substring(7);
        DeviceManagerCiscoHistory returnValue = service.findData(token, macAddress);
        return returnValue;
    }

    @PostMapping("/adddata")
    public DeviceManagerCiscoHistory addData(@RequestBody DeviceManagerCiscoHistory data) {
        return service.save(data);
    }

    // Api of configuration cisco cp-3905.
    @PostMapping("/ciscoConfig")
    public ResponseEntity<RequestResponse> methodOfCiscoConfig(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PostDataOfCisco postData) {
        RequestResponse returnValue = new RequestResponse();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                returnValue.setMessage("Authorization header missing or invalid");
                return new ResponseEntity<>(returnValue, HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);
            returnValue = service.CiscoConfig(token, postData.getMacAddress(), postData);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Extension update failed.");
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
}
