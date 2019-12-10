package com.applicaster.jwplayerplugin;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.applicaster.player.wrappers.ActivityLifecycleI;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class JWPlayerContainer extends FrameLayout{

    private JWPlayerView jwPlayerView;

    public JWPlayerContainer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public JWPlayerContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public JWPlayerContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public JWPlayerView getJWPlayerView() {
        return jwPlayerView;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        // The spinner relies on a measure + layout pass happening after it calls requestLayout().
        // Without this, the widget never actually changes the selection and doesn't call the
        // appropriate listeners. Since we override onLayout in our ViewGroups, a layout pass never
        // happens after a call to requestLayout, so we simulate one here.
        post(new Runnable() {
            @Override
            public void run() {
                measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
                layout(getLeft(), getTop(), getRight(), getBottom());
            }
        });
    }

    private void init(Context context) {
        jwPlayerView = new JWPlayerView(context, new PlayerConfig.Builder().build());
        addView(jwPlayerView);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            jwPlayerView.onResume();//onResume called
        } else{
            jwPlayerView.onPause();// onPause() called
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus){
            jwPlayerView.onResume();//onresume() called
        }
        else {
            jwPlayerView.onPause();// onPause() called
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // onDestroy() called
        if(jwPlayerView != null)
            jwPlayerView.onDestroy();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // onCreate() called
    }

}
