package com.coraltele.autoprovisioning.component.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceManagerInfoModel {
    
    private String macAddress;
    private String productClass;
    private String manufacturer;
    private String firmwareVersion;
    private String configurationVersion;
    private String oui;
}



