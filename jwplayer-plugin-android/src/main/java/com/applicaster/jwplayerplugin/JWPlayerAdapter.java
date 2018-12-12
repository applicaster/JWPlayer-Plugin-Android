package com.applicaster.jwplayerplugin;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.applicaster.controller.PlayerLoader;
import com.applicaster.player.PlayerLoaderI;
import com.applicaster.player.defaultplayer.BasePlayer;
import com.applicaster.plugin_manager.login.LoginContract;
import com.applicaster.plugin_manager.login.LoginManager;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.PlayableConfiguration;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.fullscreen.FullscreenHandler;
import com.longtailvideo.jwplayer.media.ads.AdSource;
import com.longtailvideo.jwplayer.media.ads.VMAPAdvertising;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JWPlayerAdapter extends BasePlayer implements FullscreenHandler {

    private static final String LICENSE_KEY = "LICENSE_KEY";


    // Properties
    private JWPlayerContainer jwPlayerContainer;
    private JWPlayerView jwPlayerView;
    private String licenseKey;

    /**
     * Optional initialization for the PlayerContract - will be called in the App's onCreate
     */
    @Override
    public void init(@NonNull Context appContext) {
        super.init(appContext);
    }

    /**
     * initialization of the player instance with a playable item
     *
     * @param playable
     */
    @Override
    public void init(@NonNull Playable playable, @NonNull Context context) {
        super.init(playable, context);

        JWPlayerView.setLicenseKey(context, licenseKey);
    }

    /**
     * initialization of the player instance with  multiple playable items
     *
     * @param playableList
     */
    @Override
    public void init(@NonNull List<Playable> playableList, @NonNull Context context) {
        super.init(playableList, context);

    }

    /**
     * initialization of the player plugin configuration with a Map params.
     * the params taken from res/raw/plugin_configurations.json
     *
     * @param params
     */
    @Override
    public void setPluginConfigurationParams(Map params) {
        super.setPluginConfigurationParams(params);
        licenseKey = params.get(LICENSE_KEY).toString();
    }

    /**
     * return the player type
     *
     * @return PlayerItem.Type type of the player
     */
    @Override
    public PlayerType getPlayerType() {
        return PlayerType.Default;
    }

    /**
     * Optional method - best to implement but in case you can't it will still just return false.
     *
     * @return the playing state of the player
     */
    @Override
    public boolean isPlayerPlaying() {
        return jwPlayerView.getState() == PlayerState.PLAYING;
    }


    /**
     * start the player in fullscreen with configuration.
     *
     * @param configuration player configuration.
     * @param requestCode   request code if needed - if not send NO_REQUEST_CODE instead.
     * @param context
     */
    @Override
    public void playInFullscreen(PlayableConfiguration configuration, int requestCode, @NonNull Context context) {
        openLoginPluginIfNeeded(false);
    }


    //---------- Inline ----------//
    /**
     * Add the player into the given container.
     *
     * @param videoContainerView The container to add the player to.
     */
    @Override
    public void attachInline(@NonNull ViewGroup videoContainerView) {
        jwPlayerContainer =new JWPlayerContainer(videoContainerView.getContext());
        jwPlayerView = jwPlayerContainer.getJWPlayerView();
        jwPlayerView.setFullscreenHandler(this);

        ViewGroup.LayoutParams playerContainerLayoutParams
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        videoContainerView.addView(jwPlayerContainer, playerContainerLayoutParams);

        openLoginPluginIfNeeded(true);
    }

    /**
     * Remove the player from it's container.
     *
     * @param videoContainerView The container that contains the player.
     */
    @Override
    public void removeInline(@NonNull ViewGroup videoContainerView) {
        if(videoContainerView.indexOfChild(jwPlayerContainer) >= 0) {
            videoContainerView.removeView(jwPlayerContainer);
        }
    }

    /**
     * Start the player in inline with configuration.
     *
     * @param configuration player configuration.
     */
    @Override
    public void playInline(PlayableConfiguration configuration) {
        jwPlayerView.play();
    }

    /**
     * Stops playing the inline player.
     */
    @Override
    public void stopInline() {
        jwPlayerView.stop();
    }

    /**
     * Pauses playing the inline player
     */
    @Override
    public void pauseInline() {
        jwPlayerView.pause();
    }
    /**
     * Resumes playing the inline player.
     */
    @Override
    public void resumeInline() {
        jwPlayerView.play();
    }

    private void openLoginPluginIfNeeded(final boolean isInline){
        /**
         * if item is not locked continue to play, otherwise call login with playable item.
         */
        final LoginContract loginPlugin = LoginManager.getLoginPlugin();
        if (loginPlugin != null ){

            loginPlugin.isItemLocked(getContext(), getFirstPlayable(), new LoginContract.Callback() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        loginPlugin.login(getContext(), getFirstPlayable(), null, new LoginContract.Callback() {
                            @Override
                            public void onResult(boolean result) {
                                if (result) {
                                    PlayerLoader applicasterPlayerLoader = new PlayerLoader(new ApplicaterPlayerLoaderListener(isInline));
                                    applicasterPlayerLoader.loadItem();
                                }
                            }
                        });
                    } else {
                        PlayerLoader applicasterPlayerLoader = new PlayerLoader(new ApplicaterPlayerLoaderListener(isInline));
                        applicasterPlayerLoader.loadItem();
                    }
                }
            });
        } else {
            PlayerLoader applicasterPlayerLoader = new PlayerLoader(new ApplicaterPlayerLoaderListener(isInline));
            applicasterPlayerLoader.loadItem();
        }

    }

    private void displayVideo(boolean isInline){

        if (isInline){
            jwPlayerView.load( JWPlayerUtil.getPlaylistItem(getFirstPlayable()) );
            jwPlayerView.play();
        }else {
            JWPlayerActivity.startPlayerActivity(getContext(), getFirstPlayable(), getPluginConfigurationParams());
        }
    }

    /************************** FullscreenHandler implementation ********************/

    @Override
    public void onFullscreenRequested() {
        displayVideo(false);
    }

    @Override
    public void onFullscreenExitRequested() {
        jwPlayerView.play();
    }

    @Override
    public void onResume() {
        jwPlayerView.play();
    }

    @Override
    public void onPause() {
        jwPlayerView.pause();
    }

    @Override
    public void onDestroy() { }

    @Override
    public void onAllowRotationChanged(boolean b) { }

    @Override
    public void updateLayoutParams(ViewGroup.LayoutParams layoutParams) { }

    @Override
    public void setUseFullscreenLayoutFlags(boolean b) { }


    /************************** PlayerLoaderI ********************/
    private class ApplicaterPlayerLoaderListener implements PlayerLoaderI {
        private boolean isInline;

        public ApplicaterPlayerLoaderListener(boolean isInline) {
            this.isInline=isInline;
        }

        @Override
        public String getItemId() {
            return getPlayable().getPlayableId();
        }


        @Override
        public Playable getPlayable() {
            return getFirstPlayable();
        }

        @Override
        public void onItemLoaded(Playable playable) {
            init(playable, getContext());
            displayVideo(isInline);
        }

        @Override
        public boolean isFinishing() {
            return ((Activity) getContext()).isFinishing();
        }

        @Override
        public void showMediaErroDialog() {

        }
    }
}
