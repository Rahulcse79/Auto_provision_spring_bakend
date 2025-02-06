// ApplyChangesRequest.java
package com.coraltele.autoprovisioning.component.helper;

import java.util.List;

public class ApplyChangesRequest {

    private String ipAddress;
    private String user;
    private String ipAddressNTP;
    private List<String> selectedFiles;
    private String callOfAPI;

    // Getters and Setters
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIpAddressNTP() {
        return ipAddressNTP;
    }

    public void setIpAddressNTP(String ipAddressNTP) {
        this.ipAddressNTP = ipAddressNTP;
    }

    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public String getCallOfAPI() {
        return callOfAPI;
    }

    public void setCallOfAPI(String callOfAPI) {
        this.callOfAPI = callOfAPI;
    }
}
