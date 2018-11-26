package com.applicaster.jwplayerplugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.applicaster.player.Player;
import com.applicaster.player.PlayerLoaderI;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.Map;

public class JWPlayerActivity extends AppCompatActivity{

    private static final String PLAYABLE_KEY = "playable_key";
    /**
     * Reference to the {@link JWPlayerView}
     */
    private JWPlayerView mPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayer);
        mPlayerView = findViewById(R.id.playerView);

//        // Keep the screen on during playback
//        new KeepScreenOnHandler(mPlayerView, getWindow());
//
//        // Instantiate the JW Player event handler class
//        mEventHandler = new JWEventHandler(mPlayerView, outputTextView);

        Playable playable = (Playable) getIntent().getSerializableExtra(PLAYABLE_KEY);

        // Load a media source
        mPlayerView.load( JWPlayerUtil.getPlaylistItem(playable) );
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
        mPlayerView.onResume();
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
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // Exit fullscreen when the user pressed the Back button
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mPlayerView.getFullscreen()) {
//                mPlayerView.setFullscreen(false, true);
//                return false;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    public static void startPlayerActivity(Context context, Playable playable, Map<String,String> params) {
        Intent intent = new Intent(context, JWPlayerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYABLE_KEY, playable);
        intent.putExtras(bundle);

        context.startActivity(intent);
    }
}