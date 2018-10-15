package com.blueobject.peripatosapp.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.blueobject.peripatosapp.App;

/**
 * Created by nrgie on 2018.03.16..
 */

public class BackgroundMusicPlayer extends Service {

    private static final String TAG = null;
    public static MediaPlayer player;

    public IBinder onBind(Intent i) {
        return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        App.musicPlayer = this;

        player = new MediaPlayer();
        try {
            player.setDataSource(App.currentMusic);
            player.setLooping(true); // Set looping
            player.setVolume(30, 30);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                }
            });
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    public void onStart(Intent intent, int startId) {}

    public IBinder onUnBind(Intent arg0) {
        return null;
    }

    public void onStop() {}

    public void onPause() {}

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
    }

    @Override
    public void onLowMemory() {
        player.stop();
        player.release();
    }
}
