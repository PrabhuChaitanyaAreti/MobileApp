package com.vsoft.goodmankotlin.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DieIdDetailsModel {
    @SerializedName("response")
    @Expose
    private List<DieIdResponse> response = null;
    @SerializedName("statusCode")
    @Expose
    private Integer statusCode;

    public List<DieIdResponse> getResponse() {
        return response;
    }

    public void setResponse(List<DieIdResponse> response) {
        this.response = response;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

}
