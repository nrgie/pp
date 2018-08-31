package com.blueobject.peripatosapp.helper;

public interface ASFRequestListener<T> {
    void onSuccess(T response);
    void onFailure(Exception e);
}