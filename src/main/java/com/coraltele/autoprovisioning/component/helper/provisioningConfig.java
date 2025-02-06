package com.coraltele.autoprovisioning.component.helper;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class provisioningConfig {

    @JsonProperty("sipServerIp")
    private String sipServerIp;

    @JsonProperty("sipPort")
    private String sipPort;

    @JsonProperty("epochTime")
    private String epochTime;

    @JsonProperty("accounts")
    private List<Accounts> accounts;

    // Constructor
    public provisioningConfig(String sipServerIp, String sipPort, String epochTime, List<Accounts> accounts) {
        this.sipServerIp = sipServerIp;
        this.sipPort = sipPort;
        this.epochTime = epochTime;
        this.accounts = accounts;
    }

    // Getters and Setters
    public String getSipServerIp() {
        return sipServerIp;
    }

    public void setSipServerIp(String sipServerIp) {
        this.sipServerIp = sipServerIp;
    }

    public String getSipPort() {
        return sipPort;
    }

    public void setSipPort(String sipPort) {
        this.sipPort = sipPort;
    }

    public String getEpochTime() {
        return epochTime;
    }

    public void setEpochTime(String epochTime) {
        this.epochTime = epochTime;
    }

    public List<Accounts> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Accounts> accounts) {
        this.accounts = accounts;
    }

    public static class Accounts {

        @JsonProperty("macAddress")
        private String macAddress;

        @JsonProperty("accountsActive")
        private boolean accountsActive;

        @JsonProperty("label")
        private String label;

        @JsonProperty("displayName")
        private String displayName;

        @JsonProperty("sipUserId")
        private String sipUserId;

        @JsonProperty("authenticateID")
        private String authenticateID;

        @JsonProperty("Password")
        private String password;

        @JsonProperty("profile")
        private String profile;

        // Constructor
        public Accounts(String macAddress, boolean accountsActive, String label, String displayName, String sipUserId,
                String authenticateID, String password, String profile) {
            this.macAddress = macAddress;
            this.accountsActive = accountsActive;
            this.label = label;
            this.displayName = displayName;
            this.sipUserId = sipUserId;
            this.authenticateID = authenticateID;
            this.password = password;
            this.profile = profile;
        }

        // Getters and Setters
        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public boolean isAccountsActive() {
            return accountsActive;
        }

        public void setAccountsActive(boolean accountsActive) {
            this.accountsActive = accountsActive;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getSipUserId() {
            return sipUserId;
        }

        public void setSipUserId(String sipUserId) {
            this.sipUserId = sipUserId;
        }

        public String getAuthenticateID() {
            return authenticateID;
        }

        public void setAuthenticateID(String authenticateID) {
            this.authenticateID = authenticateID;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }
    }
}
