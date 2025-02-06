package com.coraltele.autoprovisioning.component.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerHistory;
import com.coraltele.autoprovisioning.component.repository.DeviceManagerHistoryRepository;

@Service
public class DeviceManagerHistoryService {
    
    @Autowired
    private DeviceManagerHistoryRepository repository;

    public DeviceManagerHistory saveHistory(DeviceManagerHistory history)
    {
       return repository.save(history);
    }

    public Iterable<DeviceManagerHistory> getHistories() {
        return repository.findAll();
    }
    
    public DeviceManagerHistory getHistoryById(int id)
    {
        return repository.findById(id).orElse(null);
    }

    public DeviceManagerHistory getHistoryByVersionAndMacAddressAndProductClassFileFormat(String version, String macAddress, String productClass, String fileFormat) {
        return repository.findByVersionAndMacAddressAndProductClassAndFileFormat(version, macAddress, productClass,fileFormat);
    }

    public int deleteHistory(int id)
    {
        repository.deleteById(id);
        return id;
    }
}
