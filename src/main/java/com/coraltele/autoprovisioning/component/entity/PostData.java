package com.coraltele.autoprovisioning.component.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostData {

    @JsonProperty("sipServer")
    private String sipServer;

    @JsonProperty("macAddress")
    private String macAddress;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("accountNo")
    private String accountNo;

    @JsonProperty("profileNo")
    private String profileNo;


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

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getProfileNo() {
        return profileNo;
    }

    public void setProfileNo(String profileNo) {
        this.profileNo = profileNo;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "PostData{" +
                "sipServer='" + sipServer + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", profileNo='" + profileNo + '\'' +
                ", account=" + account +

                '}';
    }

    public static class Account {

        @JsonProperty("Label")
        private String label;

        @JsonProperty("SipUserId")
        private String sipUserId;

        @JsonProperty("AuthenticateID")
        private String authenticateID;

        @JsonProperty("DisplayName")
        private String displayName;

        @JsonProperty("Active")
        private boolean active;

        @JsonProperty("LocalSipPort")
        private int localSipPort;

        @JsonProperty("Password")
        private String password;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getLocalSipPort() {
            return localSipPort;
        }

        public void setLocalSipPort(int localSipPort) {
            this.localSipPort = localSipPort;
        }

        @Override
        public String toString() {
            return "Account{" +
                    "label='" + label + '\'' +
                    ", sipUserId='" + sipUserId + '\'' +
                    ", authenticateID='" + authenticateID + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", active=" + active +
                    ", localSipPort=" + localSipPort +
                    ", password=" + password +
                    '}';
        }
    }
}
