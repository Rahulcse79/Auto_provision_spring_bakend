package com.coraltele.autoprovisioning.component.entity;

import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "device_info")
public class DeviceManagerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uuid;

    @PrePersist
    public void generateUniqueId() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    @Column(name = "product_class")
    private String productClass;

    @Column(name = "mac_address")
    private String macAddress;
    
    @Column(name = "oui")
    private String oui;
    
    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "configuration_version")
    private String configurationVersion;

    @Column(name = "active")
    private boolean active;

    @Column(name = "ping")
    private String ping;

    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "lastRebootTime")
    private String lastRebootTime;

    @Column(name = "lastInform")
    private String lastInform;

    @Column(name = "registered")
    private String registered;

    @Column(name = "timeStamp")
    private String timeStamp;
}

