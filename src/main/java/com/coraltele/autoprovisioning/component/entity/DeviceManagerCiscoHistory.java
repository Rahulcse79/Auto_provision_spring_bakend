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
@Table(name = "device_manager_cisco_history")
public class DeviceManagerCiscoHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "date")
    private String date;

    @Column(name = "time")
    private String time;

    @Column(name = "dhcp")
    private boolean dhcp;

    @Column(name = "tftp")
    private boolean tftp;

    @Column(name = "file_present")
    private boolean filePresent;

    @Column(name = "default_file")
    private boolean defaultFile;
}
