package com.coraltele.autoprovisioning.component.repository;

import org.springframework.stereotype.Repository;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerInfo;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface DeviceManagerInfoRespository extends CrudRepository<DeviceManagerInfo,Integer>{
    DeviceManagerInfo findByMacAddress(String macAddress);
}
