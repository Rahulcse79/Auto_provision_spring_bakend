package com.coraltele.autoprovisioning.component.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coraltele.autoprovisioning.component.entity.DeviceManagerAutoDeploy;
import com.coraltele.autoprovisioning.component.repository.DeviceManagerAutoDeployRepository;

@Service
public class DeviceManagerAutoDeployService {
    
    @Autowired
    private DeviceManagerAutoDeployRepository repository;

    public DeviceManagerAutoDeploy saveAutoDeployData(DeviceManagerAutoDeploy Data)
    {
       return repository.save(Data);
    }

    public Iterable<DeviceManagerAutoDeploy> getAllAutoDeployData() {
        return repository.findAll();
    }
    
    public DeviceManagerAutoDeploy getAutoDeployDataById(int id)
    {
        return repository.findById(id).orElse(null);
    }

    public DeviceManagerAutoDeploy getAutoDeployDataByVersionAndMacAddressAndProductClassAndFileFormat(String version, String macAddress, String productClass, String fileFormat) {
        return repository.findByVersionAndMacAddressAndProductClassAndFileFormat(version, macAddress, productClass, fileFormat);
    }    

    public int deleteAutoDeployData(int id)
    {
        repository.deleteById(id);
        return id;
    }
}
