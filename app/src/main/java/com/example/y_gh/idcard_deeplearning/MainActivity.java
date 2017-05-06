package com.example.y_gh.idcard_deeplearning;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements Runnable{
//    private static String url="http://10.103.240.70:8888/upload";
    private static String url="http://10.103.241.119:8880/upload";
    private Uri fileUri;
    private Uri cropUri;
    private static final int REQUEST_IMAGE_CAMERA = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    private static final int RESULT_REQUEST_CODE = 300;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Bitmap bmp;
    private static final String LOG_TAG = "MainActivity";
    private ProgressDialog dialog;
    String imgPath;
    String result;
    private ImageView imageView;
    private TextView name,sex,nation,birth,address,idcard,textView1;
    IdCardBean bean = new IdCardBean();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        final FloatingActionsMenu floatMenu  = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        FloatingActionButton camera = (FloatingActionButton) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                startActivityForResult(i, REQUEST_IMAGE_CAMERA);
                floatMenu.toggle();
            }
        });
        FloatingActionButton select = (FloatingActionButton) findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_SELECT);
                floatMenu.toggle();
            }
        });

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = (ImageView) findViewById(R.id.imageView);
        name = (TextView) findViewById(R.id.name);
        sex = (TextView) findViewById(R.id.sex);
        nation = (TextView) findViewById(R.id.nation);
        birth = (TextView) findViewById(R.id.birth);
        address = (TextView) findViewById(R.id.address);
        idcard = (TextView) findViewById(R.id.idcard);
        textView1 = (TextView) findViewById(R.id.textView1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            switch (requestCode){
                case REQUEST_IMAGE_CAMERA:
                    startPhotoZoom(fileUri);
                    break;
                case REQUEST_IMAGE_SELECT:
                    Uri selectedImage = data.getData();
                    startPhotoZoom(selectedImage);
                    break;
                case RESULT_REQUEST_CODE:
                    imgPath = cropUri.getPath();

                    bmp = BitmapFactory.decodeFile(imgPath);
                    Log.e(LOG_TAG, imgPath);
                    Log.d(LOG_TAG, String.valueOf(bmp.getHeight()));
                    Log.d(LOG_TAG, String.valueOf(bmp.getWidth()));
                    /**
                     * loading...
                     */

                    if (dialog == null) {
                        dialog = new ProgressDialog(this);
                    }
                    dialog.setMessage("识别中...");
                    dialog.setCancelable(false);
                    dialog.show();
                    new Thread(this).start();
                    break;
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void run() {
        try {
            PostFile(imgPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件
     * @param imgPath
     * @throws IOException
     * @throws JSONException
     */
    private void PostFile(String imgPath) throws IOException, JSONException {
        File file = new File(imgPath);
        if (!file.exists())
        {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = new MultipartBuilder()
                .addFormDataPart("file",imgPath , RequestBody.create(MediaType.parse("media/type"), new File(imgPath)))
                .type(MultipartBuilder.FORM)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String tempResponse =  response.body().string();
            Log.e("==返回結果==","---"+tempResponse);
            JSONArray arr = new JSONArray(tempResponse);
            String responsew = arr.getString(0);
            JSONObject obj = new JSONObject(responsew);
            result = obj.getString("result");
            Log.e("==输出==","--"+result);
            if(result.equals("sucess")){
                JSONObject obj2 = new JSONObject(obj.getString("response"));
                Log.e("==response==","------"+obj.getString("response"));
                String day = obj2.getString("day");
                bean.setName(obj2.getString("name").substring(2,obj2.getString("name").length()-2));
                bean.setSex(obj2.getString("sex").substring(2,obj2.getString("sex").length()-2));
                bean.setAddress(obj2.getString("address").substring(1,obj2.getString("address").length()-1).replaceAll("\"|,",""));
                bean.setNation(obj2.getString("minzu").substring(2,obj2.getString("minzu").length()-2));
                bean.setIdcard(obj2.getString("id").substring(2,obj2.getString("id").length()-2));
                bean.setBirth_year(obj2.getString("year").substring(2,obj2.getString("year").length()-2));
                bean.setBirth_month(obj2.getString("month").substring(2,obj2.getString("month").length()-2));
                bean.setBirth_day(obj2.getString("day").substring(2,obj2.getString("day").length()-2));

                Log.e("---day---","==>"+day);
                mHandler.sendEmptyMessage(0);
            }else if(result.equals("error")){
                Log.e("==返回==","---出错---");
                mHandler.sendEmptyMessage(1);
            }
        } else {
            dialog.cancel();
            Toast.makeText(MainActivity.this, "请求错误", Toast.LENGTH_SHORT).show();
            throw new IOException("Unexpected code " + response);
        }


    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case 0:
                    dialog.cancel();
                    name.setText(bean.getName());
                    sex.setText(bean.getSex());
                    nation.setText(bean.getNation());
                    birth.setText(bean.getBirth_year()+"年"+bean.getBirth_month()+"月"+bean.getBirth_day()+"日");
                    address.setText(bean.getAddress());
                    idcard.setText(bean.getIdcard());
                    imageView.setImageBitmap(bmp);
                    textView1.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    dialog.cancel();
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                    break;
            }
        };
    };


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


    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "IdCard_DeepLearning");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String name = "img_idcard";
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath()  + File.separator + name + ".jpg");
            Log.e("----文件路径----","====="+mediaFile);
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * 裁剪图片
     */

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");//发起剪切动作
        intent.setDataAndType(uri, "image/*");//设置剪切图片的uri和类型
        intent.putExtra("crop", "true");//剪切动作的信号
        intent.putExtra("aspectX", 1.6);//x和y是否等比缩放
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 316);//剪切后图片的尺寸
        intent.putExtra("return-data", true);//是否把剪切后的图片通过data返回
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());//图片的输出格式
        intent.putExtra("noFaceDetection", true);  //关闭面部识别
        //设置剪切的图片保存位置
        cropUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,cropUri);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }
}
