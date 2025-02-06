package com.coraltele.autoprovisioning.component.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceManagerHistoryModel {

    private byte[] files;
    private String macAddress;
    private String productClass;
    private String version;
    private String fileName;
    private String fileFormat;
    private String date;
    private String time;
}
