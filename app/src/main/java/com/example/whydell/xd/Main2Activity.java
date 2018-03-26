package com.example.whydell.xd;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class Main2Activity extends AppCompatActivity {

    private Button mbutton;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setTitle("景观介绍");
        Bundle bundle = this.getIntent().getExtras();
        assert bundle != null;
        String link = bundle.getString("link");
        mbutton = (Button) findViewById(R.id.button4);
        mbutton.setOnClickListener(new ButtonOnClickListener());
        pd = new ProgressDialog(Main2Activity.this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle("下载结果中");
        pd.setIndeterminate(false);
        pd.setCancelable(false);
        pd.show();
        new getImage(link).start();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    private Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(6000);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
        }
        return bmp;
    }

    @SuppressLint("HandlerLeak")
    private Handler ToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG + 5).show();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LinearLayout ll = findViewById(R.id.ll);
            ImageView img = new ImageView(getApplicationContext());
            img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setAdjustViewBounds(true);
            img.setImageBitmap((Bitmap) msg.obj);
            ll.addView(img);
            pd.cancel();
        }
    };


    final private class getImage extends Thread {
        String url;
        getImage(String url){
            this.url = url;
        }
        @Override
        public void run() {
            try {
                    Bitmap bmp = getURLimage(url);
                    Message msg = new Message();
                    msg.obj = bmp;
                    handler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                ToastHandler.sendMessage(msg);
            }finally {
                pd.cancel();
            }
        }
    }

    private class ButtonOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            try{
                switch (v.getId()){
                    case R.id.button4:
                        String path = "http://www.hfut.edu.cn/5457/list.htm";
                        Intent intent = new Intent();
                        // Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(path);
                        intent.setData(content_url);
                        startActivity(intent);
                }
            }
            catch(Exception e){
                Toast.makeText(Main2Activity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }

    }





}
