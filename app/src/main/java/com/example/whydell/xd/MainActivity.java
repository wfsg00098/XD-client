package com.example.whydell.xd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Button mButton;
    private Button mButton2;
    private ImageView mImageView;//用于显示照片
    private File mPhotoFile;
    private String mPhotoPath;
    public final static int CAMERA_RESULT = 1;
    private static final int IMAGE = 2;
    private ProgressDialog pd;
    @SuppressLint("HandlerLeak")
    class NewWindow extends Handler{
        @Override
        public void handleMessage(Message msg){
            try {
                Main2Activity series = new Main2Activity();
                Intent it = new Intent(getApplicationContext(), series.getClass());
                Bundle bundle = new Bundle();
                bundle.putString("link", (String) msg.obj);
                it.putExtras(bundle);
                startActivity(it);
                pd.cancel();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG + 5).show();
            }
        }
    }
    Handler newWindow = new NewWindow();

    @SuppressLint("HandlerLeak")
    class handler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
        }
    }

    Handler myhandler = new handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton.setOnClickListener(new ButtonOnClickListener());
        mButton2.setOnClickListener(new ButtonOnClickListener());
        mImageView = (ImageView) findViewById(R.id.imageView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.a));
        pd = new ProgressDialog(MainActivity.this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle("上传中");
        pd.setIndeterminate(false);
        pd.setCancelable(false);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请权限，REQUEST_TAKE_PHOTO_PERMISSION是自定义的常量
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},123);
        }


    }

    private class ButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                switch (v.getId()) {
                    case R.id.button1:
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");//开始拍照
                        mPhotoPath = getSDPath() + "/" + getPhotoFileName();//设置图片文件路径，getSDPath()和getPhotoFileName()具体实现在下面
                        mPhotoFile = new File(mPhotoPath);
                        if (!mPhotoFile.exists()) {
                            mPhotoFile.createNewFile();//创建新文件
                        }
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,//Intent有了图片的信息
                                Uri.fromFile(mPhotoFile));
                        startActivityForResult(intent, CAMERA_RESULT);//跳转界面传回拍照所得数据
                        Intent intent1 = new Intent();
                        intent1.setAction(Intent.ACTION_GET_CONTENT);
                        intent1.addCategory(Intent.CATEGORY_OPENABLE);
                        break;
                    case R.id.button2:
                        Intent intent2 = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent2, IMAGE);
                        break;
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();

    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            assert selectedImage != null;
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            assert c != null;
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            //showImage(imagePath);
            Bitmap bm = BitmapFactory.decodeFile(imagePath);
            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bm);
            pd.show();
            new newthread(bm).start();
            c.close();
        } else if (requestCode == CAMERA_RESULT && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath, null);
            mImageView.setImageBitmap(bitmap);
            pd.show();
            new newthread(bitmap).start();
        }

    }

    //加载图片
    private void showImage(String imaePath) {
        Bitmap bm = BitmapFactory.decodeFile(imaePath);
        ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bm);
    }


    class newthread extends Thread {
        Bitmap bitmap;

        newthread(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            byte[] result = output.toByteArray();
            try {
                Socket socket = new Socket("103.214.142.107", 10404);
                socket.getOutputStream().write(result);
                socket.shutdownOutput();
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String link = br.readLine();
                Message msg = new Message();
                msg.obj = link;
                newWindow.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                myhandler.sendMessage(msg);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}




