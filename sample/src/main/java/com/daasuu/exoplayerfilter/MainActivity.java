package com.daasuu.exoplayerfilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.daasuu.epf.EPlayerView;
import com.daasuu.epf.PlayerScaleType;
import com.daasuu.epf.filter.GlFilter;
import com.daasuu.epf.filter.GlLookUpTableFilter;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;
import com.pushd.colorpal.ColorCorrector;


public class MainActivity extends AppCompatActivity {

    private EPlayerView ePlayerView;
    private SimpleExoPlayer player;
    private Button button;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;
    private boolean filterIsActive;
    private GlFilter lutFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setUpViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpSimpleExoPlayer();
        setUoGlPlayerView();
        //setUpTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }


    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void setUpSimpleExoPlayer() {


        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "yourApplicationName"));

        DefaultRenderersFactory factory = new DefaultRenderersFactory(this);
        factory.experimentalSetAsynchronousBufferQueueingEnabled(true);
        factory.experimentalSetForceAsyncQueueingSynchronizationWorkaround(true);
        factory.experimentalSetSynchronizeCodecInteractionsWithQueueingEnabled(true);

        // SimpleExoPlayer
        player = new SimpleExoPlayer.Builder(this, factory)
                .setMediaSourceFactory(new ProgressiveMediaSource.Factory(dataSourceFactory))
                .build();
        //player.addMediaItem(MediaItem.fromUri(Constant.STREAM_URL_MP4_VOD_SHORT));
        player.addMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(R.raw.land_017)));
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.prepare();
        player.setPlayWhenReady(true);
    }


    private void setUoGlPlayerView() {
        ePlayerView = new EPlayerView(this);
        ePlayerView.setSimpleExoPlayer(player);
        ePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ePlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_NONE);
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(ePlayerView);
        ColorCorrector corrector = new ColorCorrector();
        corrector.loadDisplayICCProfile("/private/calibration/latestDisplayProfile.icc");
        Bitmap lut = BitmapFactory.decodeResource(getResources(), R.drawable.lut_reference_flipped);
        corrector.correctBitmap(lut);
        lutFilter = new GlLookUpTableFilter(lut);
    //    lutFilter = new GlLargeLookupTableFilter(lut);
        ePlayerView.setGlFilter(lutFilter);

        ePlayerView.onResume();
    }


    private void setUpTimer() {
        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {
                long position = player.getCurrentPosition();
                long duration = player.getDuration();

                if (duration <= 0) return;

                seekBar.setMax((int) duration / 1000);
                seekBar.setProgress((int) position / 1000);
            }
        });
        playerTimer.start();
    }


    private void releasePlayer() {
        ePlayerView.onPause();
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).removeAllViews();
        ePlayerView = null;
        player.stop();
        player.release();
        player = null;
    }


}