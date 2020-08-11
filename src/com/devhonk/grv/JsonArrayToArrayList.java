package com.devhonk.grv;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class JsonArrayToArrayList {
    public static ArrayList<JsonObject> jsonArrayToArrayList(JsonArray jsonArray) {
        ArrayList<JsonObject> listdata = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                listdata.add(jsonArray.get(i).getAsJsonObject());
            }
        }
        return listdata;
    }
}
