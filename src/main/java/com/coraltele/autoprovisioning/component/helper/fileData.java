package com.coraltele.autoprovisioning.component.helper;

public class fileData {
    private String fileName;
    private byte[] fileData;

    public fileData(String fileName, byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
