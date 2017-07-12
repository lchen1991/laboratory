package com.laboratory.chenlei;

import android.content.ComponentName;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.laboratory.chenlei.module.media.music.MusicService;


public class MainActivity extends AppCompatActivity {

    private MediaBrowserCompat mediaBrowser ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback, null);
    }

    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();

           MediaSessionCompat.Token token =  mediaBrowser.getSessionToken();
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
        }
    };
}
