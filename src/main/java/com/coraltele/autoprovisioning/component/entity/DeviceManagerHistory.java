package com.coraltele.autoprovisioning.component.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "device_manager_history")
public class DeviceManagerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "files")
    private byte[] files;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "product_class")
    private String productClass;

    @Column(name = "version")
    private String version;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_format")
    private String fileFormat;

    @Column(name = "date")
    private String date;

    @Column(name = "time")
    private String time;
}
