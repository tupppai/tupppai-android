package com.pires.wesee.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 上传求p的标签
 *
 * @author ZouMengyuan
 */
public class Label {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Label createLabel(JSONObject jsonObj)
            throws JSONException {
        Label label = new Label();
        label.id = jsonObj.getInt("id");
        label.name = jsonObj.getString("name");
        return label;
    }
}
