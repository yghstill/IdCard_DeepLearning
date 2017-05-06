package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SurfaceView surfaceview;
    private Camera camera;
    private Button take;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 没有标题 必须在设置布局之前找到调用
        setContentView(R.layout.activity_main);
/*
getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, // 设置全屏显示
WindowManager.LayoutParams.FLAG_FULLSCREEN);
*/
        take = (Button)findViewById(R.id.take);
        surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder holder = surfaceview.getHolder();
        holder.setFixedSize(176, 155);// 设置分辨率
        holder.setKeepScreenOn(true);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
// SurfaceView只有当activity显示到了前台，该控件才会被创建 因此需要监听surfaceview的创建
        holder.addCallback(new MySurfaceCallback());
//拍照按钮
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takepicture();
            }
        });
    }
    //点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//对焦
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                camera.cancelAutoFocus();
            }
        });
        return super.onTouchEvent(event);
    }
    /**
     * 监听surfaceview的创建
     * @author Administrator
     * Surfaceview只有当activity显示到前台，该空间才会被创建
     */
    private final class MySurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
// TODO Auto-generated method stub
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
// TODO Auto-generated method stub
            try {
// 当surfaceview创建就去打开相机
                camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
// Log.i("i", params.flatten());
//                params.setJpegQuality(80); // 设置照片的质量
//                params.setPictureSize(1024, 768);
//                params.setPreviewFrameRate(5); // 预览帧率
//                camera.setParameters(params); // 将参数设置给相机
//右旋90度，将预览调正
                camera.setDisplayOrientation(90);
// 设置预览显示
                camera.setPreviewDisplay(surfaceview.getHolder());
// 开启预览
                camera.startPreview();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
// TODO Auto-generated method stub
            if(camera != null){
                camera.release();
                camera = null;
            }
        }
    }
    //拍照的函数
    public void takepicture(){
/*
* shutter:快门被按下
* raw:相机所捕获的原始数据
* jpeg:相机处理的数据
*/
        camera.takePicture(null, null, new MyPictureCallback());
    }
    //byte转Bitmap
    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
    //bitmap转byte
    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    //照片回调函数，其实是处理照片的
    private final class MyPictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
// TODO Auto-generated method stub
            try {
                Bitmap bitmap = Bytes2Bimap(data);
                Matrix m = new Matrix(); int width = bitmap.getWidth(); int height = bitmap.getHeight(); m.setRotate(90);
//将照片右旋90度
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m,
                        true);
                Log.d("TAG","width "+width);
                Log.d("TAG","height "+height);
//截取透明框内照片
                bitmap = Bitmap.createBitmap(bitmap,50,250,650,500);
                data = Bitmap2Bytes(bitmap);
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "IdCard_Camera");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e("MyCameraApp", "failed to create directory");
                    }
                }
                File mediaFile = new File(mediaStorageDir.getPath()  + File.separator + "img_idcard" + ".jpg");
                Log.e("----文件路径----","====="+mediaFile);
                FileOutputStream fos = new FileOutputStream(mediaFile);
                fos.write(data);
// 在拍照的时候相机是被占用的,拍照之后需要重新预览
                camera.startPreview();
            } catch (Exception e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
