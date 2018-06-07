package com.example.matthijs.libstreamingexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;

public class MainActivity extends Activity implements View.OnClickListener, Session.Callback, SurfaceHolder.Callback, RtspServer.CallbackListener {

    private final static String TAG = "MainActivity";

    private Button mButton1, mButton2;
    private SurfaceView mSurfaceView;
    private EditText mEditText;
    private Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mEditText = (EditText) findViewById(R.id.editText1);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
        editor.commit();


        mSession = SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView((net.majorkernelpanic.streaming.gl.SurfaceView) mSurfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320, 240, 20, 500000))
                .build();

        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);

        mSurfaceView.getHolder().addCallback(this);

        ((net.majorkernelpanic.streaming.gl.SurfaceView) mSurfaceView).setAspectRatioMode(net.majorkernelpanic.streaming.gl.SurfaceView.ASPECT_RATIO_PREVIEW);
        String ip, port, path;

        this.startService(new Intent(this, RtspServer.class));

        Log.d("test", "1");

        mSession.startPreview();
        mSession.start();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mSession.isStreaming()){
            mButton1.setText("stop");
        } else {
            mButton1.setText("start");
        }
    }

    public void onDestroy(){
        super.onDestroy();
        mSession.release();
    }


    @Override
    public void onClick(View v){
        if(v.getId() == R.id.button1){
            // start / stop streaming
            mSession.setDestination(mEditText.getText().toString());
            if (!mSession.isStreaming()){
                mSession.configure();
            } else {
                mSession.stop();
            }
            mButton1.setEnabled(false);
        } else {
            // switching cameras
            mSession.switchCamera();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSession.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSession.stop();
    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG, "Bitrate: "+bitrate);
    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        mButton1.setEnabled(true);
        if (e != null){
            logError(e.getMessage());
        }
    }

    private void logError(final String message) {
        final String error = (message == null) ? "Error unknown" : message;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(error).setPositiveButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){}
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onPreviewStarted() {
    Log.d(TAG, "Preview started.");
    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG, "Preview configured");
        Log.d(TAG, mSession.getSessionDescription());
        mSession.start();
    }

    @Override
    public void onSessionStarted() {
        Log.d(TAG, "Session started");
        mButton1.setEnabled(true);
        mButton1.setText("Stop!");
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "Session stopped");
        mButton1.setEnabled(true);
        mButton1.setText("Start!");
    }

    @Override
    public void onError(RtspServer rtspServer, Exception e, int i) {

    }

    @Override
    public void onMessage(RtspServer rtspServer, int i) {

    }
}
