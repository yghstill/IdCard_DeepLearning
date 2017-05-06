package network;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.y_gh.idcard_deeplearning.R;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;

import java.io.File;

/**
 * Created by Y-GH on 2017/3/31.
 */

public class volleyRequest {
    private static ProgressDialog dialog;
    public static String url="http://192.168.191.1";
    private String httpurl1 = url+":8080/Graduation_practic/joinmatch.action";
    private String httpurl2 = url+":8080/Graduation_practic/joinmatch.action";

    public void Send_Picture(String filePartName, File file, final Context context){


        /**
         * loading...
         */

        if (dialog == null) {
            dialog = new ProgressDialog(context);
        }
        dialog.setMessage("识别中...");
        dialog.setCancelable(false);
        dialog.show();

        /**
         * 访问网络
         */
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());

        MultipartRequest multipartRequest = new MultipartRequest(
                "http://yourhost.com", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("", "### response : " + response);
            }

        });

        // 添加header
        multipartRequest.addHeader("header-name", "value");

// 通过MultipartEntity来设置参数
        MultipartEntity multi = multipartRequest.getMultiPartEntity();
// 上传文件
//        multi.addPart("imgfile", (ContentBody) new File("storage/emulated/0/test.jpg"));
// 将请求添加到队列中
        queue.add(multipartRequest);

    }
}
