package com.example.imagedownloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText txtURL;
    Button btnDownload;
    ImageView imgView;
    ProgressDialog PD;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtURL = findViewById(R.id.txtURL);
        btnDownload = findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imgView);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int permission = ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
                if( permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                } else {
                    AsyncTask backgroundTask = new DownloadTask();
                    String[] urls = new String[1];
                    urls[0] = txtURL.getText().toString();
                    backgroundTask.execute(urls);
                }


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_EXTERNAL_STORAGE) {
            if(grantResults.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                AsyncTask backgroundTask = new DownloadTask();
                String[] urls = new String[1];
                urls[0] = txtURL.getText().toString();
                backgroundTask.execute(urls);
            }
        }
    }


    void preview(String imagePath ){
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        float w = image.getWidth();
        float h = image.getHeight();
        int W = 400;
        int H = (int) ( (h*W)/w);
        Bitmap b = Bitmap.createScaledBitmap(image, W, H, false);
        imgView.setImageBitmap(b);
    }

    class DownloadTask extends AsyncTask<String, Integer, Bitmap> {

        private void downloadFile(String urlStr, String imagePath) {
            try {
                URL url = new URL(urlStr);
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(imagePath);
                byte data[] = new byte[1024];

                final int size = connection.getContentLength();
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                    total += count;

                    publishProgress((int)((total*100)/size));
                }
                output.flush();
                output.close();
                input.close();



            } catch (Exception e) {
                Log.d("roo", "count + ");
                e.printStackTrace();
            }

        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            String fileName = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS)).toString()
                    + "/" + fileName;
            downloadFile(strings[0],imagePath);

            Bitmap image = BitmapFactory.decodeFile(imagePath);

            float w = image.getWidth();
            float h = image.getHeight();
            int W = 400;
            int H = (int) ( (h*W)/w);
            Bitmap b = Bitmap.createScaledBitmap(image, W, H, false);
            return b;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imgView.setImageBitmap(bitmap);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PD = new ProgressDialog(MainActivity.this);
            PD.setMax(100);
            PD.setIndeterminate(false);
            PD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            PD.setTitle("Downloading");
            PD.setMessage("Please wait..");
            PD.show();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            PD.setProgress(progress[0]);


        }
    }
}