package com.coraltele.autoprovisioning.component.helper;

import java.util.List;

public class RequestResponse {

    private Constants.NodeStatus issue;
    private int status;
    private String message;
    private String messageDetail;
    private Object data;
    private int value;
    private int total;
    byte[] fileData;
    private List<Integer> ans;

    public RequestResponse() {
        this.status = 0;
        this.issue= Constants.NodeStatus.OK;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<Integer> getAns() {
        return ans;
    }

    public void setAns(List<Integer> ans) {
        this.ans = ans;
    }

    public String getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(String messageDetail) {
        this.messageDetail = messageDetail;
    }

    public int getStatus() {
        return status;
    }

    public byte[] getFileData(){
        return fileData;
    }

    public void setFileData(byte[] fileData){
        this.fileData = fileData;
    }

    public int getTotal(){
        return total;
    }

    public void setTotal(int total){
        this.total = total;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Constants.NodeStatus getIssue() {
        return issue;
    }

    public void setIssue(Constants.NodeStatus issue) {
        this.issue = issue;
    }

}
