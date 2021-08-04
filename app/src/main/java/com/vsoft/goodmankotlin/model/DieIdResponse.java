package com.vsoft.goodmankotlin.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DieIdResponse {

    @SerializedName("die_id")
    @Expose
    private String dieId;
    @SerializedName("part_id")
    @Expose
    private List<String> partId = null;

    public String getDieId() {
        return dieId;
    }

    public void setDieId(String dieId) {
        this.dieId = dieId;
    }

    public List<String> getPartId() {
        return partId;
    }

    public void setPartId(List<String> partId) {
        this.partId = partId;
    }

}
