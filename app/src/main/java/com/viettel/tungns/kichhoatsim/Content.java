package com.viettel.tungns.kichhoatsim;

import com.google.gson.Gson;

public class Content {
    private String action;
    private String destination;
    private String type;
    private String smsBody;

    public Content() {
    }

    public Content(String action, String destination, String type, String smsBody) {
        this.action = action;
        this.destination = destination;
        this.type = type;
        this.smsBody = smsBody;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSmsBody() {
        return smsBody;
    }

    public void setSmsBody(String smsBody) {
        this.smsBody = smsBody;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
