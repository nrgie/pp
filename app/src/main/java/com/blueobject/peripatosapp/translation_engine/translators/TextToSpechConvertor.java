package com.blueobject.peripatosapp.translation_engine.translators;
 
import android.annotation.TargetApi; 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.blueobject.peripatosapp.App;
import com.blueobject.peripatosapp.translation_engine.utils.ConversionCallback;
import com.blueobject.peripatosapp.translation_engine.utils.TranslatorUtil;
 
import java.util.HashMap;


public class TextToSpechConvertor implements IConvertor { 
 
    public TextToSpechConvertor(ConversionCallback conversioCallBack) {
        this.conversionCallBack = conversioCallBack;
    }

    public TextToSpechConvertor() {

    }

    private ConversionCallback conversionCallBack;
 
    public TextToSpeech textToSpeech;
    public int oldmusicvalue = 0;

    public TextToSpeech getTTS() {
        return this.textToSpeech;
    }
 
    /** 
     * This method will initialize and translate message provided 
     * 
     * @param message    message to speak 
     * @param appContext Note:- Donnot forgot to call finish when 
     */ 
    @Override 
    public TextToSpechConvertor initialize(final String message, Activity appContext) {
        textToSpeech = new TextToSpeech(appContext, new TextToSpeech.OnInitListener() {
            @Override 
            public void onInit(int status) {
 
                if (status != TextToSpeech.ERROR) {
                    //textToSpeech.setLanguage(Locale.getDefault());

                    int result = textToSpeech.setLanguage(App.TTSLANG);

                    if(result == TextToSpeech.LANG_MISSING_DATA) {

                        Intent i = new Intent();
                        i.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        App.appContext.startActivity(i);

                    } else {


                        textToSpeech.setPitch(1f);
                        textToSpeech.setSpeechRate(1f);
                        //textToSpeech.setLanguage(Locale.UK);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                            ttsGreater21(message);

                        } else {
                            ttsUnder20(message);
                        }

                    }

                } else {
                    Log.e("APP", TranslatorUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
                    if(conversionCallBack != null)
                        conversionCallBack.onErrorOccured(TranslatorUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
                } 
            } 
        }); 
 
        return this;
    }

    public TextToSpechConvertor initialize(Context appContext) {
        textToSpeech = new TextToSpeech(appContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status != TextToSpeech.ERROR) {
                    //textToSpeech.setLanguage(Locale.getDefault());

                    int result = textToSpeech.setLanguage(App.TTSLANG);

                    if(result == TextToSpeech.LANG_MISSING_DATA) {

                        Intent i = new Intent();
                        i.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.appContext.startActivity(i);

                    } else {

                        /*
                        textToSpeech.setPitch(1f);
                        textToSpeech.setSpeechRate(1.1f);
                        //textToSpeech.setLanguage(Locale.UK);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                            ttsGreater21(message);

                        } else {
                            ttsUnder20(message);
                        }
                        */

                    }

                } else {
                    Log.e("APP", TranslatorUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
                    if(conversionCallBack != null)
                        conversionCallBack.onErrorOccured(TranslatorUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
                }
            }
        });

        return this;
    }

    public void setSpeechRate(float rate) {
        App.speechRate = rate;
        textToSpeech.setSpeechRate(rate);
    }

    public void speak(String message) {

        textToSpeech.setLanguage(App.TTSLANG);

        textToSpeech.setPitch(1f);
        textToSpeech.setSpeechRate(App.speechRate);

        AudioManager audioManager = (AudioManager) App.appContext.getSystemService(Context.AUDIO_SERVICE);
        if(oldmusicvalue == 0)
            oldmusicvalue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //Log.e("OLDVOLUME", ""+oldmusicvalue);

        // 15 körül van max ?

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 7, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(message);
        } else {
            ttsUnder20(message);
        }




    }


    public void finish() { 
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        } 
    } 
 
    @SuppressWarnings("deprecation") 
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override 
            public void onStart(String utteranceId) {}
 
            @Override 
            public void onError(String utteranceId) {
                Log.e("APP", TranslatorUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
            } 
 
            @Override 
            public void onDone(String utteranceId) {
                if(conversionCallBack != null) conversionCallBack.onCompletion();
            } 
        }); 
 
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    } 
 
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";

        Bundle map = new Bundle();
        map.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION);
        map.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1);

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map, utteranceId);

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onError(String utteranceId) {
                Log.e("APP", TranslatorUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
            }

            @Override
            public void onDone(String utteranceId) {
                AudioManager audioManager = (AudioManager) App.appContext.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldmusicvalue, 0);
                oldmusicvalue = 0;
            }
        });



    } 
 
} 
