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
import com.coraltele.autoprovisioning.component.entity.DeviceManagerHistory;
import com.coraltele.autoprovisioning.component.service.DeviceManagerHistoryService;

@CrossOrigin
@RequestMapping("/api/deviceManagerHistory")
@RestController
public class DeviceManagerHistoryController {

    @Autowired
    private DeviceManagerHistoryService service;

    @PostMapping("/addHistory")
    public DeviceManagerHistory addHistory(@RequestBody DeviceManagerHistory history) {
        return service.saveHistory(history);
    }

    @GetMapping("/historys")
    public Iterable<DeviceManagerHistory> findAllHistorys() {
        return service.getHistories();
    }

    @GetMapping("/history/{id}")
    public DeviceManagerHistory findHistoryById(@PathVariable int id) {
        return service.getHistoryById(id);
    }

    @DeleteMapping("/delete/{id}")
    public int deleteHistoryFunction(@PathVariable int id) {
        return service.deleteHistory(id);
    }

    @PostMapping("/history")
    public DeviceManagerHistory findHistoryByVersionAndMacAddressAndProductClassFileFormat(@RequestBody String version,
            @RequestBody String macAddress, @RequestBody String productClass, @RequestBody String fileFormat) {
        return service.getHistoryByVersionAndMacAddressAndProductClassFileFormat(version, macAddress, productClass,fileFormat);
    }
}
