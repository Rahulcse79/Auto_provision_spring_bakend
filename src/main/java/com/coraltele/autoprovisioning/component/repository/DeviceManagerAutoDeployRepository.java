package com.coraltele.autoprovisioning.component.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerAutoDeploy;

@Repository
public interface DeviceManagerAutoDeployRepository extends CrudRepository<DeviceManagerAutoDeploy,Integer>{
    DeviceManagerAutoDeploy findByVersionAndMacAddressAndProductClassAndFileFormat(String version, String macAddress, String productClass, String fileFormat);
}
