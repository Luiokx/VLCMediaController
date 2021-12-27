package com.salvatierra.vlcmedia.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import org.videolan.libvlc.MediaPlayer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.salvatierra.vlcmedia.R;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class VideControllerVLC extends FrameLayout {
    private MediaPlayerControl mediaPlayer;
    private View LayoutController;
    private ViewGroup mainAnchor;
    private Context mainContext;
    private boolean mainShowing, mFromXml, mUseFastForward, mainDragging;
    public Handler mainHandler = new VideControllerVLC.MessageHandler(this);
    private ImageView fastForward, backForward, pause, language;
    private ProgressBar progressBar;
    private TextView currentTime, endTime;
    private int optionMenuLang;

    public VideControllerVLC(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainContext = context;
        mUseFastForward = true;
        mFromXml = true;
    }

    public VideControllerVLC(Context context, boolean useFastForward){
        super(context);
        mainContext = context;
        mUseFastForward = useFastForward;
    }

    public VideControllerVLC(Context context){
        this(context, true);
        mainContext = context;
    }

    public void setMediaPlayer(MediaPlayerControl player){
        mediaPlayer = player;
        updatePlayingButton();
    }

    private void updatePlayingButton(){
        if (LayoutController == null || pause == null || mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying())
            ((ImageView) LayoutController.findViewById(R.id.pause)).setImageResource(R.drawable.ic_pause);
        else
            ((ImageView) LayoutController.findViewById(R.id.pause)).setImageResource(R.drawable.ic_play);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (LayoutController != null)
            initControllerView();
    }

    private void initControllerView(){
        //Pause and play button
        pause = (ImageView) LayoutController.findViewById(R.id.pause);

        if (pause != null) {
            pause.requestFocus();
            pause.setOnClickListener(this::pauseListener);
        }

        backForward = (ImageView) LayoutController.findViewById(R.id.video_bfs);

        if (backForward != null){
            backForward.setOnClickListener(this::backForward);
            if (!mFromXml) {
                backForward.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        fastForward = (ImageView) LayoutController.findViewById(R.id.video_ff);

        if (fastForward != null){
            fastForward.setOnClickListener(this::fastForward);
            if (!mFromXml){
                fastForward.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        progressBar = (ProgressBar) LayoutController.findViewById(R.id.progreess_video);

        if (progressBar != null){
            if (progressBar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) progressBar;
                seeker.setOnSeekBarChangeListener(seekListener);
            }
            progressBar.setMax(1000);
        }

        language = (ImageView) LayoutController.findViewById(R.id.subtitle_video);
        language.setOnClickListener(this::languageAlert);

        endTime = (TextView) LayoutController.findViewById(R.id.video_duration_text);
        currentTime = (TextView) LayoutController.findViewById(R.id.video_current_position);

        ((ImageView) LayoutController.findViewById(R.id.go_back_video)).setOnClickListener(v -> {
            mediaPlayer.finishLayout();
        });

    }

    public void subtitulesEnable(){
        if (mediaPlayer.isSubtitulabe() || mediaPlayer.isLanguageChangePosible())
            language.setVisibility(View.VISIBLE);
        else
            language.setVisibility(View.GONE);
    }

    private void languageAlert(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext, 4);

        if (mediaPlayer.isSubtitulabe() && mediaPlayer.isLanguageChangePosible()) {

            int[] idSub = new int[mediaPlayer.subLenght()], idAudio = new int[mediaPlayer.audioLenght()];
            String subNames[] = new String[mediaPlayer.subLenght()], audioNames[] = new String[mediaPlayer.audioLenght()];
            mediaPlayer.setSubName(subNames, idSub);
            mediaPlayer.setAudioName(audioNames, idAudio);

            alertDialog.setTitle("Selecciona una opcion");
            alertDialog.setSingleChoiceItems(new String[]{"Subtitle", "Audio"}, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    optionMenuLang = which;
                }
            });
            alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (optionMenuLang){
                        case 0:
                            subtitulesDialog(subNames, idSub);
                            break;
                        case 1:
                            audioDialog(audioNames, idAudio);
                            break;
                    }

                }
            });
            alertDialog.setNegativeButton("Cancel", null);
            alertDialog.show();

        } else if (!mediaPlayer.isSubtitulabe() && mediaPlayer.isLanguageChangePosible()) {
            int[] idAudio = new int[mediaPlayer.audioLenght()];
            String audioNames[] = new String[mediaPlayer.audioLenght()];

            mediaPlayer.setAudioName(audioNames, idAudio);

            alertDialog.setTitle("Selecciona una opcion");
            alertDialog.setSingleChoiceItems(new String[]{"Audio"}, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    optionMenuLang = which;
                }
            });
            alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    audioDialog(audioNames, idAudio);
                }
            });
            alertDialog.setNegativeButton("Cancel", null);
            alertDialog.show();

        } else {
            int[] idSub = new int[mediaPlayer.subLenght()];
            String subNames[] = new String[mediaPlayer.subLenght()];

            mediaPlayer.setSubName(subNames, idSub);

            alertDialog.setTitle("Selecciona una opcion");
            alertDialog.setSingleChoiceItems(new String[]{"Subtitle"}, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    optionMenuLang = which;
                }
            });
            alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    subtitulesDialog(subNames, idSub);
                }
            });
            alertDialog.setNegativeButton("Cancel", null);
            alertDialog.show();

        }

    }

    private void subtitulesDialog(String[] subNames, int[] idSub) {
        AlertDialog.Builder subDialog = new AlertDialog.Builder(mainContext);
        subDialog.setSingleChoiceItems(subNames, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                optionMenuLang = which;
            }
        });
        subDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.changeSub(idSub[optionMenuLang]);
                optionMenuLang = 0;
            }
        });
        subDialog.setNegativeButton("Cancelar", null);
        subDialog.show();
    }

    private void audioDialog(String[] audioNames, int[] idAudio){
        AlertDialog.Builder subDialog = new AlertDialog.Builder(mainContext);

        subDialog.setSingleChoiceItems(audioNames, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                optionMenuLang = which;
            }
        });

        subDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.changeAudio(idAudio[optionMenuLang]);
                optionMenuLang = 0;
            }
        });
        subDialog.setNegativeButton("Cancelar", null);
        subDialog.show();
    }

    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mainDragging = true;

            mainHandler.removeMessages(2);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mediaPlayer == null || !fromuser)
                return;

            long duration = mediaPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;

            mediaPlayer.seekTo((int) newPosition);
            if (currentTime != null)
                currentTime.setText(stringForTime((int) newPosition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mainDragging = false;
            setProgress();
            updatePlayingButton();
            show(3000);
            mainHandler.sendEmptyMessage(2);
        }
    };

    public void show(){
        show(3000);
    }

    public void hide(){
        if (mainAnchor == null)
            return;

        try {
            mainAnchor.removeView(this);
            mainHandler.removeMessages(2);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }

        mainShowing = false;
    }

    private void backForward(View view){
        if (mediaPlayer == null)
            return;

        int currentPos = mediaPlayer.getCurrentPosition();
        int pos = 0;

        if (currentPos - 10000 >= 10000) {
            pos = currentPos - 10000;
        }

        mediaPlayer.seekTo(pos);

        setProgress();

        show(3000);
    }

    private void fastForward(View view){
        if (mediaPlayer == null)
            return;

        int currentPos = mediaPlayer.getCurrentPosition();

        if (currentPos + 10000 >= mediaPlayer.getDuration())
            return;

        mediaPlayer.seekTo(currentPos + 10000);

        setProgress();

        show(3000);
    }

    private void pauseListener(View view){

        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();

        updatePause();

        show(3000);
    }

    private void pauseResume(){

        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();

        updatePause();

        show(3000);
    }

    private void updatePause(){
        if (mediaPlayer.isPlaying())
            ((ImageView) LayoutController.findViewById(R.id.pause)).setImageResource(R.drawable.ic_pause);
        else
            ((ImageView) LayoutController.findViewById(R.id.pause)).setImageResource(R.drawable.ic_play);
    }

    public void show(int timeout){
        if (!mainShowing && mainAnchor != null){
            setProgress();
            if (pause != null) {
                pause.requestFocus();
            }

            disableUnsupportedButtons();

            FrameLayout.LayoutParams frame = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            mainAnchor.addView(this, frame);
            mainShowing = true;
        }

        updatePause();

        mainHandler.sendEmptyMessage(2);

        Message msg = mainHandler.obtainMessage(1);

         if (timeout != 0) {
            mainHandler.removeMessages(1);
            mainHandler.sendMessageDelayed(msg, timeout);
        }

    }

    private int setProgress(){
        if (mediaPlayer == null || mainDragging){
            return 0;
        }

        int position = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();

        if (progressBar != null) {
            if (mediaPlayer.getDuration() > 0) {
                long pos = 1000L * position / duration;
                progressBar.setProgress((int) pos);
            }

            int percent = mediaPlayer.getBufferPercentage();
            progressBar.setSecondaryProgress(percent * 10);
        }

        if (endTime != null)
            endTime.setText(stringForTime(duration));

        if (currentTime != null)
            currentTime.setText(stringForTime(position));


        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        formatBuilder.setLength(0);

        if (hours > 0)
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        else
            return formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(3000);
        return false;
    }

    public boolean isShowing(){
        return mainShowing;
    }

    public void setAnchorView(ViewGroup view){
        mainAnchor = view;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        addView(makeControllerView(), frameParams);
    }

    protected View makeControllerView(){
        LayoutInflater inflate = (LayoutInflater) mainContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LayoutController = inflate.inflate(R.layout.media_controller, null);

        initControllerView();

        return LayoutController;
    }

    private void disableUnsupportedButtons(){
        if (mediaPlayer == null)
            return;

        try {
            if (pause != null && !mediaPlayer.canPause()) {
                pause.setEnabled(false);
            }
            if (backForward != null && !mediaPlayer.canSeekBackward()) {
                backForward.setEnabled(false);
            }
            if (fastForward != null && !mediaPlayer.canSeekForward()) {
                fastForward.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            Log.d("Buttons unsupported", ex.getMessage());
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mediaPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                pauseResume();
                show(3000);
                if (pause != null) {
                    pause.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updatePause();
                show(3000);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                updatePause();
                show(3000);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(3000);
        return super.dispatchKeyEvent(event);
    }

    public interface MediaPlayerControl {
        void start();
        void pause();
        void seekTo(int position);
        void setSubName(String tracks[], int[] idSub);
        void setAudioName(String tracks[], int[] idAudio);
        void changeSub(int option);
        void changeAudio(int option);
        void finishLayout();
        int getDuration();
        int getCurrentPosition();
        int getBufferPercentage();
        int subLenght();
        int audioLenght();
        boolean canPause();
        boolean isPlaying();
        boolean canSeekBackward();
        boolean canSeekForward();
        boolean isSubtitulabe();
        boolean isLanguageChangePosible();
    }

    public static class MessageHandler extends Handler {
        private final WeakReference<VideControllerVLC> mView;

        MessageHandler(VideControllerVLC view) {
            mView = new WeakReference<VideControllerVLC>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            VideControllerVLC view = mView.get();
            if (view == null || view.mediaPlayer == null) {
                return;
            }

            switch (msg.what) {
                case 1:
                    view.hide();
                    break;
                case 2:
                    if (!view.mainDragging && view.mainShowing && view.mediaPlayer.isPlaying()) {
                        sendMessageDelayed(obtainMessage(2), 1000 - (view.setProgress() % 1000));
                    }
                    break;
            }
        }
    }
}
