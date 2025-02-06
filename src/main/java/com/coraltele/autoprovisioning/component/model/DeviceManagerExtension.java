package com.coraltele.autoprovisioning.component.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceManagerExtension {
    private String sipServer;
    private String account1_Label;
    private String account1_SipUserId;
    private String account1_AuthenticateID;
    private String account1_DispalyName;
    private boolean account1_Active;
    private int account1_LocalSipPort;
    private String account;
}
