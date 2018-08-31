package com.blueobject.peripatosapp.translation_engine.utils;

public interface ConversionCallback {
 
    public void onSuccess(String result);
 
    public void onCompletion(); 
 
    public void onErrorOccured(String errorMessage);
 
} 
