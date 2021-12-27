package com.salvatierra.vlcmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.salvatierra.vlcmedia.model.VideControllerVLC;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements VideControllerVLC.MediaPlayerControl {
    private VideControllerVLC controller;
    private MediaPlayer player;
    private LibVLC mLibVLC;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        initPlayer();

        initController();

    }

    private void initController(){
        controller = new VideControllerVLC (MainActivity.this);
        controller.setMediaPlayer(MainActivity.this);
        controller.setAnchorView(((FrameLayout) findViewById(R.id.videoSurfaceContainer)));
    }

    private void initPlayer() {
        ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");

        mLibVLC = new LibVLC(MainActivity.this, args);
        player = new MediaPlayer(mLibVLC);
        Media media = new Media(mLibVLC, Uri.parse("example.com"));
        player.setMedia(media);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        controller.setOnClickListener(v -> {
            if (controller.isShowing())
                controller.hide();
            else
                controller.show(5000);
        });

        return false;
    }

    @Override
    public void start() {
        this.player.play();
        initSubtitule();
    }

    @Override
    public void pause() {
        this.player.pause();
    }

    @Override
    public int getDuration() {
        return (int) this.player.getLength();
    }

    @Override
    public int getCurrentPosition() {
        return (int) this.player.getTime();
    }

    @Override
    public void seekTo(int pos) {
        player.setTime(pos);
    }

    @Override
    public void setSubName(String[] tracks, int idSub[]) {
        int i = 0;
        for (MediaPlayer.TrackDescription uniqueTrack : player.getSpuTracks()){
            tracks[i] = uniqueTrack.name;
            idSub[i] = uniqueTrack.id;
            i++;
        }
    }

    @Override
    public void setAudioName(String[] tracks, int idAudio[]) {
        int i = 0;
        for (MediaPlayer.TrackDescription uniqueTrack : player.getAudioTracks()){
            tracks[i] = uniqueTrack.name;
            idAudio[i] = uniqueTrack.id;
            i++;
        }
    }

    @Override
    public void changeSub(int option) {
        if (player.setSpuTrack(option))
            Log.d("Track sub", "ok");

    }

    @Override
    public void changeAudio(int option) {
        if (player.setAudioTrack(option))
            Log.d("Track audio", "ok");
    }

    @Override
    public void finishLayout() {
        player.stop();
        finish();
    }

    @Override
    public boolean isPlaying() {
        return this.player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int subLenght() {
        return player.getSpuTracksCount();
    }

    @Override
    public int audioLenght() {
        return player.getAudioTracksCount();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean isSubtitulabe() {
        if (player.getSpuTracksCount() > 0)
            return true;

        return false;
    }

    @Override
    public boolean isLanguageChangePosible() {
        if (player.getAudioTracksCount() > 0)
            return true;

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.player.pause();
    }

    private static int init = 0;

    @Override
    protected void onResume() {
        super.onResume();
        player.attachViews(((VLCVideoLayout) findViewById(R.id.vlcmediaView)), null, true, false);
        player.play();
    }

    private void initSubtitule() {
        if (init == 0) {
            controller.subtitulesEnable();
            init++;
        }
    }

    @Override
    public void onBackPressed() {
        player.stop();
        finish();
    }
}