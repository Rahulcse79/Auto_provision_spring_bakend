package com.coraltele.autoprovisioning.component.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerAutoDeploy;
import com.coraltele.autoprovisioning.component.service.DeviceManagerAutoDeployService;

@CrossOrigin
@RequestMapping("/api/deviceManagerAutoDeploy")
@RestController
public class DeviceManagerAutoDeployController {
    
    @Autowired
    private DeviceManagerAutoDeployService service;

    @PostMapping("/addAutoDeployData")
    public DeviceManagerAutoDeploy addAutoDeployData(@RequestBody DeviceManagerAutoDeploy Data) {
        return service.saveAutoDeployData(Data);
    }

     @GetMapping("/allAutoDeployData")
    public Iterable<DeviceManagerAutoDeploy> findAllAutoDeployData() {
        return service.getAllAutoDeployData();
    }

    @GetMapping("/AutoDeployData/{id}")
    public DeviceManagerAutoDeploy findAutoDeployDataById(@PathVariable int id) {
        return service.getAutoDeployDataById(id);
    }

    @DeleteMapping("/deleteAutoDeployData/{id}")
    public int deleteAutoDeployDataFunction(@PathVariable int id) {
        return service.deleteAutoDeployData(id);
    }

    @PostMapping("/AutoDeployDataByDetails")
    public DeviceManagerAutoDeploy findAutoDeployDataByVersionAndMacAddressAndProductClassFileFormat(@RequestBody String version,
            @RequestBody String macAddress, @RequestBody String productClass, @RequestBody String fileFormat) {
        return service.getAutoDeployDataByVersionAndMacAddressAndProductClassAndFileFormat(version, macAddress, productClass,fileFormat);
    }
}
