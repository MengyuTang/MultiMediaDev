package com.tang.alex.multimediadev;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tang.alex.multimediadev.utils.H264Encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class VideoToMp4Activity extends AppCompatActivity implements SurfaceHolder.Callback,Camera.PreviewCallback{

    private Unbinder unbinder;

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    private Camera camera;

    private SurfaceHolder holder;

    private H264Encoder encoder;

    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    /**
     * 被用户拒绝的权限列表
     */
    private List<String> mPermissionList = new ArrayList<>();

    private static final int MY_PERMISSIONS_REQUEST = 1001;

    int width = 1280;
    int height = 720;
    int framerate = 30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_to_mp4);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        checkPermissions();
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        if (supportH264Codec()) {
            Log.e("MainActivity", "support H264 hard codec");
        } else {
            Log.e("MainActivity", "not support H264 hard codec");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != unbinder){
            unbinder.unbind();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (encoder != null) {
            encoder.putData(data);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(1280, 720);
        try {
            camera.setParameters(parameters);
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        encoder = new H264Encoder(width, height, framerate);
        encoder.startEncoder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w("MainActivity", "enter surfaceDestroyed method");

        // 停止预览并释放资源
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera = null;
        }

        if (encoder != null) {
            encoder.stopEncoder();
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

    private boolean supportH264Codec() {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
