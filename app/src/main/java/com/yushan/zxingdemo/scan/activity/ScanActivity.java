package com.yushan.zxingdemo.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.yushan.zxingdemo.R;
import com.yushan.zxingdemo.scan.camera.CameraManager;
import com.yushan.zxingdemo.scan.decoding.CaptureActivityHandler;
import com.yushan.zxingdemo.scan.decoding.InactivityTimer;
import com.yushan.zxingdemo.scan.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * Initial the camera scan
 *
 * @author yushan
 */

public class ScanActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private CaptureActivityHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_camera);

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        viewfinderView.setShowInfoType(ViewfinderView.TEXTCONTENTTYPE_SCAN);
        CameraManager.frameSize = 2;
        CameraManager.init(getApplication());

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        decodeFormats = null;
        characterSet = null;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            Toast.makeText(this, "请授予相应权限，再使用扫一扫！", Toast.LENGTH_SHORT).show();
            finish();// 获取系统相机事件被拒绝后，直接finish（）掉本activity
        } catch (RuntimeException e) {
            Toast.makeText(this, "请授予相应权限，再使用扫一扫！", Toast.LENGTH_SHORT).show();
            finish();// 获取系统相机事件被拒绝后，直接finish（）掉本activity
        }

        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        finish();
    }

    /**
     * 扫描返回数据
     *
     * @param resultString
     */
    public void handleDecode(String resultString) {
        inactivityTimer.onActivity();

        Log.e("yushan", resultString);

        Intent intent = this.getIntent();
        intent.putExtra("resultString", resultString);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }
}