package com.brentvatne.exoplayer;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.brentvatne.react.R;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

// Fullscreen related code taken from Android Studio blueprint
public class FullscreenVideoActivity extends AppCompatActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of
            // API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private String mPlayerId;
    private boolean enablePIP = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        mContentView = findViewById(R.id.enclosing_layout);
        mPlayerId = getIntent().getStringExtra(ReactExoplayerViewManager.PLAYER_UUID);

        PlayerView playerView = findViewById(R.id.player_view);
        // Set the fullscreen button to "close fullscreen" icon
        ImageView fullscreenIcon = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenIcon.setImageResource(R.drawable.ic_fullscreen_close);

        playerView.findViewById(R.id.exo_fullscreen_icon)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

    }

    @Override
    public void onResume() {
        super.onResume();
        PlayerView playerView = findViewById(R.id.player_view);
        ReactExoplayerView reactExoplayerView = ReactExoplayerViewManager.getInstance(mPlayerId);
        reactExoplayerView.prepareExoPlayer(this, playerView);
        reactExoplayerView.goToForeground();
        enablePIP = reactExoplayerView.isEnablePIP();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT >= 24 && isInPictureInPictureMode()) {
            ReactExoplayerView reactExoplayerView = ReactExoplayerViewManager.getInstance(mPlayerId);
            reactExoplayerView.setControls(false);
            reactExoplayerView.setPausedModifier(false);
        }
        else {
            ReactExoplayerViewManager.getInstance(mPlayerId).goToBackground();
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }



    @Override
    public void onUserLeaveHint () {
        if (enablePIP && Util.SDK_INT >= 24) {
            enterPictureInPictureMode();
        }
    }

    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        ReactExoplayerView reactExoplayerView = ReactExoplayerViewManager.getInstance(mPlayerId);
        if (isInPictureInPictureMode) {
            reactExoplayerView.setControls(false);
        }
        else {
            reactExoplayerView.setControls(true);
            finish();
            startActivity(getIntent());
        }
    }

    private void hide() {
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }
}
