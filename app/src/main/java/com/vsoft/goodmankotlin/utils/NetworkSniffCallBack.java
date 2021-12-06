package com.vsoft.goodmankotlin.utils;

import java.util.ArrayList;

public interface NetworkSniffCallBack {
    void networkSniffResponse(String response, ArrayList<String> ipAddressList);
}

