package com.tang.alex.multimediadev;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tang.alex.multimediadev.utils.PcmToWavUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tang.alex.multimediadev.utils.GlobalConfig.SAMPLE_RATE_INHZ;
import static com.tang.alex.multimediadev.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.tang.alex.multimediadev.utils.GlobalConfig.AUDIO_FORMAT;

public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private Unbinder unbinder;
    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    /**
     * 被用户拒绝的权限列表
     */
    private List<String> mPermissionList = new ArrayList<>();

    private boolean isRecording;

    private AudioRecord audioRecord = null;

    private AudioTrack audioTrack;
    private byte[] audioData;
    private FileInputStream fileInputStream;

    @BindView(R.id.btn_record)
    Button btnRecord;
    @BindView(R.id.btn_change)
    Button btnChange;
    @BindView(R.id.btn_play)
    Button btnPlay;

    private static final int MY_PERMISSIONS_REQUEST = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        unbinder = ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != unbinder){
            unbinder.unbind();
        }
    }

    private void init() {
        btnRecord.setOnClickListener(this);
        btnChange.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        checkPermissions();
    }

    private void startRecord() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ,CHANNEL_CONFIG,AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE_INHZ,CHANNEL_CONFIG,AUDIO_FORMAT,minBufferSize);
        final byte[] data = new byte[minBufferSize];
        File storageDirectory = Environment.getExternalStorageDirectory();
        String filePath = storageDirectory.getAbsolutePath()+"/record/";
        File mFile=new File(filePath);
        if (!mFile.exists()&& !mFile.isDirectory()){
            mFile.mkdirs();
        }

        final File file = new File((filePath), "test.pcm");
        audioRecord.startRecording();
        isRecording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (null != os){
                    while(isRecording){
                        int read = audioRecord.read(data,0,minBufferSize);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read){
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i("audioRecord", "run: close file output stream !");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopRecord() {
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            //recordingThread = null;
        }
    }

    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("audioRecord", permissions[i] + " 权限被用户禁止！");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_record:
                if (btnRecord.getText().toString().equals("录音")){
                    btnRecord.setText("停止");
                    startRecord();
                }else{
                    btnRecord.setText("录音");
                    stopRecord();
                }
                break;

            case R.id.btn_change:
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                File storageDirectory = Environment.getExternalStorageDirectory();
                String filePath = storageDirectory.getAbsolutePath()+"/record/";
                File mFile=new File(filePath);
                if (!mFile.exists()&& !mFile.isDirectory()){
                    mFile.mkdirs();
                }
                final File pcmFile = new File((filePath), "test.pcm");
                final File wavFile = new File((filePath), "test.wav");
                if (!wavFile.mkdirs()) {
                    Log.e("audioRecord", "wavFile Directory not created");
                }
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
                break;

            case R.id.btn_play:
                if (btnPlay.getText().toString().equals("播放")){
                    btnPlay.setText("暂停");
                    startPlay();
                }else{
                    btnPlay.setText("播放");
                    pause();
                }
                break;
        }
    }
    private void pause(){
        if (null != audioTrack) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    /**
     * 播放，使用stream模式
     */
    private void startPlay() {
        /*
         * SAMPLE_RATE_INHZ 对应pcm音频的采样率
         * channelConfig 对应pcm音频的声道
         * AUDIO_FORMAT 对应pcm音频的格式
         * */
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, channelConfig, AUDIO_FORMAT);
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(channelConfig)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        audioTrack.play();
        File storageDirectory = Environment.getExternalStorageDirectory();
        String filePath = storageDirectory.getAbsolutePath()+"/record/";
        File file = new File((filePath), "test.pcm");
        if(!file.exists()){
            Log.e("audioRecord","文件不存在");
            return;
        }
        try {
            fileInputStream = new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] tempBuffer = new byte[minBufferSize];
                        while (fileInputStream.available() > 0) {
                            int readCount = fileInputStream.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                audioTrack.write(tempBuffer, 0, readCount);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放，使用static模式
     */
    @SuppressLint("StaticFieldLeak")
    private void playInModeStatic() {
        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    File storageDirectory = Environment.getExternalStorageDirectory();
                    String filePath = storageDirectory.getAbsolutePath()+"/record/";
                    File file = new File((filePath), "test.pcm");
                    InputStream in = new FileInputStream(file);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        for (int b; (b = in.read()) != -1; ) {
                            out.write(b);
                        }
                        audioData = out.toByteArray();
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.wtf("audioRecord", "Failed to read", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                audioTrack = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        new AudioFormat.Builder().setSampleRate(22050)
                                .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        audioData.length,
                        AudioTrack.MODE_STATIC,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
                audioTrack.write(audioData, 0, audioData.length);
                audioTrack.play();
            }
        }.execute();
    }
}
