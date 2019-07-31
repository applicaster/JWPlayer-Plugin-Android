package com.applicaster.jwplayerplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.applicaster.player.Player;
import com.applicaster.player.PlayerLoaderI;
import com.applicaster.player.defaultplayer.gmf.layeredvideo.VideoPlayer;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.internal.PlayersManager;
import com.google.android.exoplayer2.PlayerMessage;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.io.Serializable;
import java.util.Map;

public class JWPlayerActivity extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener {

    private PowerManager.WakeLock mWakeLock = null;
    private PowerManager.WakeLock mDimLock = null;

    private static final String PLAYABLE_KEY = "playable_key";
    private static final String PLUGIN_CONFIGURATION_KEY = "plugin_configuration_key";
    /**
     * Reference to the {@link JWPlayerView}
     */
    private JWPlayerView mPlayerView;
    JWPlayerContainer jwPlayerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayer);
        aqquireWakeLocks();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        jwPlayerContainer = findViewById(R.id.playerView);
        mPlayerView = jwPlayerContainer.getJWPlayerView();
        mPlayerView.addOnFullscreenListener(this);

//        // Keep the screen on during playback
//        new KeepScreenOnHandler(mPlayerView, getWindow());
//
//        // Instantiate the JW Player event handler class
//        mEventHandler = new JWEventHandler(mPlayerView, outputTextView);

        Playable playable = (Playable) getIntent().getSerializableExtra(PLAYABLE_KEY);

        // Load a media source
        mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, PlayersManager.getCurrentPlayer().getPluginConfigurationParams()));
        mPlayerView.play();

        // Get a reference to the CastManager
//        mCastManager = CastManager.getInstance();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Set fullscreen when the device is rotated to landscape
        mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        super.onResume();

        //If the video was paused, resume & play.
        if (mPlayerView.getState().name().equals("PAUSED")) {
            mPlayerView.onResume();
            mPlayerView.play();
        }

    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        super.onDestroy();
        dismissWakeLocks();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
     *
     * @param fullscreenEvent true if the player is fullscreen
     */
    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {


        if (fullscreenEvent.getFullscreen()) {
//                LinearLayout.LayoutParams toFullscreen = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
//                mPlayerView.setLayoutParams(toFullscreen);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
//                LinearLayout.LayoutParams toMinimize = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
//                mPlayerView.setLayoutParams(toMinimize);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // When going to Fullscreen we want to set fitsSystemWindows="false"
        jwPlayerContainer.setFitsSystemWindows(!fullscreenEvent.getFullscreen());
    }

    public static void startPlayerActivity(Context context, Playable playable, Map<String, String> params) {
        Intent intent = new Intent(context, JWPlayerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYABLE_KEY, playable);
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    private void dismissWakeLocks() {
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        if(mDimLock != null) {
            mDimLock.release();
            mDimLock = null;
        }
    }

    private void aqquireWakeLocks() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Player");
        mDimLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "NexPlayer");

        mWakeLock.acquire();
        mDimLock.acquire();
    }

}