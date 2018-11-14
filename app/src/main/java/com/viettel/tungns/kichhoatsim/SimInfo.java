package com.viettel.tungns.kichhoatsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimInfo {
    private Map<String, ArrayList<String>> mapInfo;

    public SimInfo() {
        mapInfo = new HashMap<>();
    }

    public SimInfo(ArrayList<String> soThueBao, ArrayList<String> soSeriSIM, ArrayList<String> puk, ArrayList<String> pin) {
    }

    public Map<String, ArrayList<String>> getMapInfo() {
        return mapInfo;
    }

    public void setMapInfo(Map<String, ArrayList<String>> mapInfo) {
        this.mapInfo = mapInfo;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
