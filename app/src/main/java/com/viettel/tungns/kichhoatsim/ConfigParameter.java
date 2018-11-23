package com.viettel.tungns.kichhoatsim;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ConfigParameter {
    private String id;
    private String name;
    private int position;
    private String pattern;

    public ConfigParameter(String id, String name, int position, String pattern) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.pattern = pattern;
    }

    public ConfigParameter() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static ConfigParameter getObjectFromString(String configParameterJSON) {
        return new Gson().fromJson(configParameterJSON, ConfigParameter.class);
    }

    public static List<ConfigParameter> getListObjectFromString(String configParameterJSON) {
        return new Gson().fromJson(configParameterJSON, new TypeToken<List<ConfigParameter>>(){}.getType());
    }
}
