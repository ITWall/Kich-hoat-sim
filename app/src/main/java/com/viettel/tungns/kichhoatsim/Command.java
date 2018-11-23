package com.viettel.tungns.kichhoatsim;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Command {
    private String name;
    private Content content;

    public Command() {
    }

    public Command(String name, Content content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static List<Command> getListObjectFromString(String configCommandJSON) {
        return new Gson().fromJson(configCommandJSON, new TypeToken<List<Command>>(){}.getType());
    }
}
