package com.coraltele.autoprovisioning.component.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostDataOfCisco {

    @JsonProperty("sipServer")
    private String sipServer;

    @JsonProperty("macAddress")
    private String macAddress;

    @JsonProperty("macAddressBulk")
    private List<String> macAddressBulk;

    @JsonProperty("macExtensionBulk")
    private List<String> macExtensionBulk;

    @JsonProperty("AuthenticateID")
    private String authenticateID;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("extension")
    private String extension;

    @JsonProperty("port")
    private String port;

    @JsonProperty("securePort")
    private String securePort;

    // Getters and Setters

    public List<String> getMacAddressBulk() {
        return macAddressBulk;
    }

    public void setMacAddressBulk(List<String> macAddressBulk) {
        this.macAddressBulk = macAddressBulk;
    }

    public List<String> getMacExtensionBulk() {
        return macExtensionBulk;
    }

    public void setMacExtensionBulk(List<String> macExtensionBulk) {
        this.macExtensionBulk = macExtensionBulk;
    }

    public String getSipServer() {
        return sipServer;
    }

    public void setSipServer(String sipServer) {
        this.sipServer = sipServer;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getAuthenticateID() {
        return authenticateID;
    }

    public void setAuthenticateID(String authenticateID) {
        this.authenticateID = authenticateID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSecurePort() {
        return securePort;
    }

    public void setSecurePort(String securePort) {
        this.securePort = securePort;
    }

    @Override
    public String toString() {
        return "PostDataOfCisco{" +
                "sipServer='" + sipServer + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", authenticateID='" + authenticateID + '\'' +
                ", displayName='" + displayName + '\'' +
                ", extension='" + extension + '\'' +
                ", port='" + port + '\'' +
                ", securePort='" + securePort + '\'' +
                '}';
    }

}
