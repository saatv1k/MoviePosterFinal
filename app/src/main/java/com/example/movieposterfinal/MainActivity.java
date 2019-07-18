package com.example.movieposterfinal;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class MainActivity extends AppCompatActivity {

    ImageView takePicture;
    ImageView uploadImage;
    ImageView imageView;

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private Bitmap bitmap;

    JSONObject webJSON;
    JSONArray responses;
    JSONObject webZero;
    JSONObject webDetection;
    JSONArray webEntities;
    ArrayList<String> descriptions = new ArrayList<>();
    ArrayList<String> titleIDs = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> posterPaths = new ArrayList<>();

    private static final String TARGET_URL =
            "https://vision.googleapis.com/v1/images:annotate?";
    private static final String API_KEY =
            "key=AIzaSyBNISPb_yrEe2qw9AOF1SI3bcHaToqikU4";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        takePicture=findViewById(R.id.id_camera);
        imageView=findViewById(R.id.id_imageView);
        uploadImage=findViewById(R.id.id_vision);


        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.image_click));

                takePictureFromCamera();
                for(int i=titles.size()-1; i>=0; i--)
                    titles.remove(i);
                for(int i=descriptions.size()-1; i>=0; i--)
                    descriptions.remove(i);
                for(int i=titleIDs.size()-1; i>=0; i--)
                    titleIDs.remove(i);
                for(int i=posterPaths.size()-1; i>=0; i--)
                    posterPaths.remove(i);
            }
        });


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.image_click));

                FirebaseStorage storage = FirebaseStorage.getInstance();

                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();

                // Create a reference to "poster.jpg"
                StorageReference posterRef = storageRef.child("poster.jpg");

                // Create a reference to 'images/poster.jpg'
                StorageReference mountainImagesRef = storageRef.child("images/poster.jpg");

                // While the file names are the same, the references point to different files
                posterRef.getName().equals(mountainImagesRef.getName());    // true
                posterRef.getPath().equals(mountainImagesRef.getPath());    // false

                // Get the data from an ImageView as bytes
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = posterRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                new webAsyncTask().execute();
            }
        });


    }

    public void takePictureFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture.setVisibility(View.VISIBLE);
        }
    }

    private int checkPermission(String camera) {
        return ContextCompat.checkSelfPermission(this, camera);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
    }

    public class webAsyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            getWebDetection();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                responses = new JSONArray(webJSON.getString("responses"));
                webZero = new JSONObject(responses.getString(0));
                webDetection = new JSONObject(webZero.getString("webDetection"));
                webEntities = new JSONArray(webDetection.getString("webEntities"));
                JSONObject j = new JSONObject(webEntities.getString(0));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(int i=0; i<webEntities.length(); i++){
                try {
                    JSONObject j = new JSONObject(webEntities.getString(i));
                    if(!j.getString("description").equals("Film")&&!j.getString("description").equals("Poster")
                            &&!j.getString("description").equals("Trailer")&&!j.getString("description").equals("Movie")
                            &&!j.getString("description").equals("Marvel Studios")&&!j.getString("description").equals("Podcast")
                            &&!j.getString("description").equals("Text")&&!j.getString("description").equals("Image")
                            &&!j.getString("description").equals("Cinema")&&!j.getString("description").equals("DVD")
                            &&!j.getString("description").equals("Music")&&!j.getString("description").equals("Liquid-crystal display")
                            &&!j.getString("description").equals("Blu-ray disc")&&!j.getString("description").equals("Television")
                            &&!j.getString("description").equals("Art")&&!j.getString("description").equals("Design")
                            &&!j.getString("description").equals("Graphic Design")&&!j.getString("description").equals("Sex")) {

                        descriptions.add(j.getString("description"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            descriptions = removeDuplicates(descriptions);
            Log.d("Descriptions",descriptions.toString());

            new queryAsync().execute();

        }



        public void getWebDetection(){
            try {
                URL serverUrl = new URL(TARGET_URL + API_KEY);
                URLConnection urlConnection = serverUrl.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection)urlConnection;

                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Content-Type", "application/json");
                httpConnection.setDoOutput(true);

                BufferedWriter httpRequestBodyWriter = new BufferedWriter(new
                        OutputStreamWriter(httpConnection.getOutputStream()));
                httpRequestBodyWriter.write
                        ("{\"requests\":  [{ \"features\":  [ {\"type\": \"WEB_DETECTION\""
                                +"}], \"image\": {\"source\": { \"imageUri\":"
                                +" \"https://firebasestorage.googleapis.com/v0/b/movie-poster-final.appspot.com/o/poster.jpg?alt=media&token=f19db868-fe10-4677-bca8-14b693776808\"}}}]}");
                httpRequestBodyWriter.close();

                if (httpConnection.getInputStream() == null) {
                    System.out.println("No stream");
                    return;
                }

                Scanner httpResponseScanner = new Scanner (httpConnection.getInputStream());
                String resp = "";
                while (httpResponseScanner.hasNext()) {
                    String line = httpResponseScanner.nextLine();
                    resp += line;
                }
                webJSON = new JSONObject(resp);
                httpResponseScanner.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public  ArrayList<String> removeDuplicates(ArrayList<String> list)
    {

        // Create a new ArrayList
        ArrayList<String> newList = new ArrayList<String>();

        // Traverse through the first list
        for (String element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }
        // return the new list
        return newList;
    }

    public class queryAsync extends AsyncTask<Void,Void,Void>{

        JSONObject queryJSON;

        @Override
        protected Void doInBackground(Void... voids) {

            for(int i =0; i<descriptions.size(); i++) {

                String query = descriptions.get(i);
                query=query.replaceAll(" ", "+");
                if(query.contains("The")){
                    query=query.substring(4);
                }
                Log.d("TAG",query);
                try {

                    URL url = new URL("https://api.themoviedb.org/3/search/movie?api_key=13a7c960b4b265f6e81982f7fcdc3bac&query=" + query);
                    URLConnection urlConnection = url.openConnection();
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader in = new BufferedReader(inputStreamReader);
                    String jsonString = "";
                    String current;

                    while ((current = in.readLine()) != null) {
                        jsonString += current;
                    }

                    queryJSON = new JSONObject(jsonString);
                    if(!queryJSON.getString("total_results").equals("0")){

                        JSONArray results = new JSONArray(queryJSON.getString("results"));
                        for (int k = 0; k < results.length(); k++) {
                            JSONObject j = new JSONObject(results.getString(k));
                            String popularity = j.getString("popularity");
                            double popDoub = Double.parseDouble(popularity);
                            if(popDoub>10) {
                                titleIDs.add(j.getString("id"));
                                titles.add(j.getString("title"));
                                posterPaths.add(j.getString("poster_path"));
                            }
                        }

                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            titles = removeDuplicates(titles);
            titleIDs = removeDuplicates(titleIDs);
            posterPaths = removeDuplicates(posterPaths);


            Log.d("Titles",titles+"");
            Log.d("IDS",titleIDs+"");
            Log.d("Url",posterPaths+"");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent intent = new Intent(MainActivity.this,ListActivity.class);
            intent.putExtra("IDs",titleIDs);
            intent.putExtra("Titles",titles);
            intent.putExtra("Poster Paths",posterPaths);
            startActivity(intent);
        }
    }



}
