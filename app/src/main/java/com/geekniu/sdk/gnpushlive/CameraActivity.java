package com.geekniu.sdk.gnpushlive;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.geekniu.geekniupushlive.GnPushConstants;
import com.geekniu.geekniupushlive.GnPushLive;
import com.ksy.recordlib.service.stats.OnLogEventListener;
import com.ksy.recordlib.service.streamer.OnStatusListener;
import com.ksy.recordlib.service.util.audio.OnAudioRawDataListener;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";


    private GLSurfaceView mCameraPreview;


    private Handler mHandler;


    private final ButtonObserver mObserverButton = new ButtonObserver();

    private Chronometer chronometer;
    private View mDeleteView;
    private View mSwitchCameraView;
    private View mFlashView;
    private CheckBox enable_beauty;
    private CheckBox mMuteAudio;
    private TextView mShootingText;
    private boolean recording = false;
    private boolean isFlashOpened = false;
    private boolean startAuto = false;
    private boolean mute_audio = false;
    private boolean earMirror = false;
    private boolean landscape = false;
    private static final String START_STRING = "开始直播";
    private static final String STOP_STRING = "停止直播";

    Timer timer;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private GnPushLive gnPushLive = GnPushLive.getInstance();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.camera_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCameraPreview = (GLSurfaceView) findViewById(R.id.camera_preview);
        enable_beauty = (CheckBox) findViewById(R.id.click_to_switch_beauty);
        mMuteAudio = (CheckBox) findViewById(R.id.mute);

        mute_audio = mMuteAudio.isChecked();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg != null && msg.obj != null) {
                    String content = msg.obj.toString();
                    switch (msg.what) {
                        case GnPushConstants.GNVIDEO_CONNECT_FAILED:
                        case GnPushConstants.GNVIDEO_ENCODED_FRAMES_FAILED:
                        case GnPushConstants.GNVIDEO_CONNECT_BREAK:
                            Toast.makeText(CameraActivity.this, content,
                                    Toast.LENGTH_LONG).show();
                            chronometer.stop();
                            mShootingText.setText(START_STRING);
                            mShootingText.postInvalidate();
                            break;
                        case GnPushConstants.GNVIDEO_OPEN_STREAM_SUCC:
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            // 开始计时
                            chronometer.start();
                            mShootingText.setText(STOP_STRING);
                            mShootingText.postInvalidate();
                            break;
                        case GnPushConstants.GNVIDEO_ENCODED_FRAMES_THRESHOLD:
                            chronometer.stop();
                            recording = false;
                            mShootingText.setText(START_STRING);
                            mShootingText.postInvalidate();
                            Toast.makeText(CameraActivity.this, content,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case GnPushConstants.GNVIDEO_INIT_DONE:
                            if (mShootingText != null)
                                mShootingText.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "初始化完成", Toast.LENGTH_SHORT).show();

                            if (startAuto && gnPushLive.startStream()) {
                                mShootingText.setText(STOP_STRING);
                                mShootingText.postInvalidate();
                                recording = true;
                            }
                            break;

                        default:
                            Toast.makeText(CameraActivity.this, content,
                                    Toast.LENGTH_SHORT).show();
                    }
                }
            }

        };

        gnPushLive.init(this,"92265d07ae579dde4d675184ccd80711","1607150000000204");



        gnPushLive.setFrameRate(15);

        //设置最高码率，即目标码率
        gnPushLive.setMaxAverageVideoBitrate(800);
        //设置最低码率
        gnPushLive.setMinAverageVideoBitrate(800 * 2 / 8);
        //设置初始码率
        gnPushLive.setInitAverageVideoBitrate(800 * 5 / 8);
        gnPushLive.setAudioBitrate(32);
        gnPushLive.setVideoResolution(GnPushConstants.VIDEO_RESOLUTION_540P);
        gnPushLive.setEncodeMethod(GnPushConstants.SOFTWARE);
        gnPushLive.setSampleAudioRateInHz(44100);
        gnPushLive.setEnableStreamStatModule(true);
        landscape = true;
        gnPushLive.setDefaultLandscape(landscape);
        if (landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        gnPushLive.setFrontCameraMirror(true);



        gnPushLive.setConfig();
        gnPushLive.setDisplayPreview(mCameraPreview);
        gnPushLive.setOnStatusListener(mOnErrorListener);
        gnPushLive.setOnLogListener(mOnLogListener);
        gnPushLive.setOnAudioRawDataListener(mOnAudioRawDataListener);
        gnPushLive.enableDebugLog(true);
        gnPushLive.setMuteAudio(mute_audio);
        gnPushLive.setEnableEarMirror(earMirror);
        gnPushLive.setBeautyFilter(GnPushConstants.FILTER_BEAUTY_DENOISE);


        enable_beauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    gnPushLive.setBeautyFilter(GnPushConstants.FILTER_BEAUTY_DENOISE);
                } else {
                    gnPushLive.setBeautyFilter(GnPushConstants.FILTER_BEAUTY_DISABLE);
                }
            }
        });


        mMuteAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                gnPushLive.setMuteAudio(isChecked);
            }
        });


        mShootingText = (TextView) findViewById(R.id.click_to_shoot);
        mShootingText.setClickable(true);
        mShootingText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (recording) {
                    if (gnPushLive.stopStream()) {
                        chronometer.stop();
                        mShootingText.setText(START_STRING);
                        mShootingText.postInvalidate();
                        recording = false;
                    } else {
                        Log.e(TAG, "操作太频繁");
                    }
                } else {
                    if (gnPushLive.startStream()) {
                        mShootingText.setText(STOP_STRING);
                        mShootingText.postInvalidate();
                        recording = true;

                        gnPushLive.setEnableReverb(true);
                        gnPushLive.setReverbLevel(4);
                    } else {
                        Log.e(TAG, "操作太频繁");
                    }
                }

            }
        });
        if (startAuto) {
            mShootingText.setEnabled(false);
        }

        mDeleteView = findViewById(R.id.backoff);
        mDeleteView.setOnClickListener(mObserverButton);
        mDeleteView.setEnabled(true);

        mSwitchCameraView = findViewById(R.id.switch_cam);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mSwitchCameraView.setEnabled(true);

        mFlashView = findViewById(R.id.flash);
        mFlashView.setOnClickListener(mObserverButton);
        mFlashView.setEnabled(true);
        chronometer = (Chronometer) this.findViewById(R.id.chronometer);


    }








    @Override
    public void onResume() {
        super.onResume();
        gnPushLive.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        gnPushLive.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                new AlertDialog.Builder(CameraActivity.this).setCancelable(true)
                        .setTitle("结束直播?")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                gnPushLive.stopStream(true);
                                chronometer.stop();
                                recording = false;
                                CameraActivity.this.finish();
                            }
                        }).show();
                break;

            default:
                break;
        }
        return true;
    }


    public OnStatusListener mOnErrorListener = new OnStatusListener() {
        @Override
        public void onStatus(int what, int arg1, int arg2, String msg) {
            // msg may be null
            switch (what) {
                case GnPushConstants.GNVIDEO_OPEN_STREAM_SUCC:
                    // 推流成功
                    mHandler.obtainMessage(what, "start stream succ")
                            .sendToTarget();
                    break;
                case GnPushConstants.GNVIDEO_ENCODED_FRAMES_FAILED:
                    //编码失败
                    break;
                case GnPushConstants.GNVIDEO_FRAME_DATA_SEND_SLOW:
                    //网络状况不佳
                    if (mHandler != null) {
                        mHandler.obtainMessage(what, "network not good").sendToTarget();
                    }
                    break;
                case GnPushConstants.GNVIDEO_EST_BW_DROP:
                    //编码码率下降状态通知
                    break;
                case GnPushConstants.GNVIDEO_EST_BW_RAISE:
                    //编码码率上升状态通知
                    break;
                case GnPushConstants.GNVIDEO_AUDIO_INIT_FAILED:
                    //音频录制初始化失败回调
                    break;
                case GnPushConstants.GNVIDEO_INIT_DONE:
                    mHandler.obtainMessage(what, "init done")
                            .sendToTarget();
                    break;
                case GnPushConstants.GN_PIP_EXCEPTION:
                    mHandler.obtainMessage(what, "pip exception")
                            .sendToTarget();
                    break;
                case GnPushConstants.GN_RENDER_EXCEPTION:
                    mHandler.obtainMessage(what, "renderer exception")
                            .sendToTarget();
                    break;



                default:

            }
        }

    };



    private OnLogEventListener mOnLogListener = new OnLogEventListener() {
        @Override
        public void onLogEvent(StringBuffer singleLogContent) {
            Log.d(TAG, "***onLogEvent : " + singleLogContent.toString());
        }
    };

    private OnAudioRawDataListener mOnAudioRawDataListener = new OnAudioRawDataListener() {
        @Override
        public short[] OnAudioRawData(short[] data, int count) {
            //audio pcm data
            return data;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        gnPushLive.onDestroy();
        executorService.shutdownNow();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (timer != null) {
            timer.cancel();
        }

    }

    private boolean clearState() {
        if (clearBackoff()) {
            return true;
        }
        return false;
    }

    private long lastClickTime = 0;

    private void onSwitchCamClick() {
        long curTime = System.currentTimeMillis();
        if (curTime - lastClickTime < 1000) {
            return;
        }
        lastClickTime = curTime;

        if (clearState()) {
            return;
        }
        gnPushLive.switchCamera();

    }

    private void onFlashClick() {
        if (isFlashOpened) {
            gnPushLive.toggleTorch(false);
            isFlashOpened = false;
        } else {
            gnPushLive.toggleTorch(true);
            isFlashOpened = true;
        }
    }

    private boolean clearBackoff() {
        if (mDeleteView.isSelected()) {
            mDeleteView.setSelected(false);
            return true;
        }
        return false;
    }

    private void onBackoffClick() {

        new AlertDialog.Builder(CameraActivity.this).setCancelable(true)
                .setTitle("结束直播?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        gnPushLive.stopStream(true);
                        chronometer.stop();
                        recording = false;
                        CameraActivity.this.finish();
                    }
                }).show();
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.switch_cam:
                    onSwitchCamClick();
                    break;
                case R.id.backoff:
                    onBackoffClick();
                    break;
                case R.id.flash:
                    onFlashClick();
                    break;
                default:
                    break;
            }
        }
    }


}
