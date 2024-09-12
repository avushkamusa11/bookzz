package com.fmi.bookzz.helper;

import org.json.JSONException;

public interface ResponseListener {
    void onResponse(String response) throws JSONException;
    void onError(String error);
}
