package com.viettel.tungns.kichhoatsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimInfo {
    private Map<ConfigParameter, ArrayList<String>> mapInfo;

    public SimInfo() {
        mapInfo = new HashMap<>();
    }

    public Map<ConfigParameter, ArrayList<String>> getMapInfo() {
        return mapInfo;
    }

    public void setMapInfo(Map<ConfigParameter, ArrayList<String>> mapInfo) {
        this.mapInfo = mapInfo;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
