package com.example.raid_bot_2.oauth;

public class Data {

    public Data(Tweet data) {
        this.data = data;
    }

    public Data() {
    }

    public Tweet getData() {
        return data;
    }

    public void setData(Tweet data) {
        this.data = data;
    }

    private Tweet data;
}
