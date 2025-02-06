package com.coraltele.autoprovisioning.component.repository;

import org.springframework.stereotype.Repository;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerCiscoHistory;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface DeviceManagerCiscoRepository extends CrudRepository<DeviceManagerCiscoHistory,Integer> {
    DeviceManagerCiscoHistory findByMacAddress(String macAddress);
}
