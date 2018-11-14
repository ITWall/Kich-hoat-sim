package com.viettel.tungns.kichhoatsim;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Config {
    private ArrayList<ScanPattern> scanPatternList;
    private String phoneNumberCall;
    private ArrayList<String> smsPhoneNumberList;
    private String smsContent;

    public Config() {
        scanPatternList = new ArrayList<>();
        smsPhoneNumberList = new ArrayList<>();
    }

    public Config(ArrayList<ScanPattern> scanPatternList, String phoneNumberCall, ArrayList<String> smsPhoneNumberList, String smsContent) {
        this.scanPatternList = scanPatternList;
        this.phoneNumberCall = phoneNumberCall;
        this.smsPhoneNumberList = smsPhoneNumberList;
        this.smsContent = smsContent;
    }

    public ArrayList<ScanPattern> getScanPatternList() {
        return scanPatternList;
    }

    public void setScanPatternList(ArrayList<ScanPattern> scanPatternList) {
        this.scanPatternList = scanPatternList;
    }

    public String getPhoneNumberCall() {
        return phoneNumberCall;
    }

    public void setPhoneNumberCall(String phoneNumberCall) {
        this.phoneNumberCall = phoneNumberCall;
    }

    public ArrayList<String> getSmsPhoneNumberList() {
        return smsPhoneNumberList;
    }

    public void setSmsPhoneNumberList(ArrayList<String> smsPhoneNumberList) {
        this.smsPhoneNumberList = smsPhoneNumberList;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
