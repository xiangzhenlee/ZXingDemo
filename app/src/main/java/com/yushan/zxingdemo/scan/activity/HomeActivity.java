package com.yushan.zxingdemo.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yushan.zxingdemo.R;
import com.yushan.zxingdemo.scan.decoding.CaptureActivityHandler;
import com.yushan.zxingdemo.scan.encoding.EncodingHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class HomeActivity extends Activity implements View.OnClickListener {

    private Button btn_generate;
    private Button btn_analysis;
    private ImageView iv_client_ewm;
    private TextView tv_show;
    private Bitmap qrCodeBitmap;
    private JSONObject json;
    private CaptureActivityHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        btn_generate = (Button) findViewById(R.id.btn_generate);
        btn_generate.setOnClickListener(this);
        btn_analysis = (Button) findViewById(R.id.btn_analysis);
        btn_analysis.setOnClickListener(this);
        iv_client_ewm = (ImageView) findViewById(R.id.iv_client_ewm);
        tv_show = (TextView) findViewById(R.id.tv_show);
    }

    private void initData() {

        String filePath = Environment.getDataDirectory().getPath() + File.separator + "data" + File.separator;
        String fileName = "zxing.png";
        json = new JSONObject();
        try {
            json.put("url", "https://github.com/zxing/zxing");
            json.put("author", "yushan");
            json.put("filepath", filePath);
            json.put("filename", fileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_generate:
                EncodingHelper encodingHelper = new EncodingHelper(HomeActivity.this, json, 350);
                Bitmap bitmap = encodingHelper.getBitmapWithSingOrBK(R.drawable.ewm_hulu, true);
                tv_show.setVisibility(View.GONE);
                iv_client_ewm.setVisibility(View.VISIBLE);
                iv_client_ewm.setBackgroundDrawable(new BitmapDrawable(bitmap));
                break;
            case R.id.btn_analysis:
                Intent intent = new Intent(this, ScanActivity.class);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 111);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            String msg = data.getStringExtra("resultString");
            iv_client_ewm.setVisibility(View.GONE);
            tv_show.setVisibility(View.VISIBLE);
            tv_show.setText(msg);
        }
    }
}
