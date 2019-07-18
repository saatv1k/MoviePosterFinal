package com.example.movieposterfinal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ArrayList<String> titleIDs;
    ArrayList<String> titles;
    ArrayList<String> posterPaths;
    ListView listView;
    ArrayList<Movie> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_list);

        listView=findViewById(R.id.id_listview);

        titleIDs = getIntent().getStringArrayListExtra("IDs");
        titles = getIntent().getStringArrayListExtra("Titles");
        posterPaths = getIntent().getStringArrayListExtra("Poster Paths");

        movieList = new ArrayList<>();
        for(int i=0; i<titles.size(); i++){

            titles.get(i);
            titleIDs.get(i);
            posterPaths.get(i);

            movieList.add(new Movie(titles.get(i),titleIDs.get(i),posterPaths.get(i)));
        }

        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.custom_layout, movieList);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    JSONObject j = new JSONObject();
                    j.put("Movie: ",movieList.get(position).getMovieTitle());

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("movies.json", Context.MODE_PRIVATE));
                    outputStreamWriter.write(j.toString());
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(ListActivity.this,InfoActivity.class);
                intent.putExtra("ID",movieList.get(position).getMovieID());
                intent.putExtra("Title",movieList.get(position).getMovieTitle());
                startActivity(intent);
            }
        });
    }

    public class CustomAdapter extends ArrayAdapter<Movie>{

        Context context;
        int resource;
        List<Movie> list;


        public CustomAdapter( @NonNull Context context, int resource, @NonNull List<Movie> objects) {
            super(context, resource, objects);

            this.context=context;
            this.resource=resource;
            this.list=objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View adapterLayout = layoutInflater.inflate(resource,null);

            TextView title = adapterLayout.findViewById(R.id.id_title);
            ImageView poster = adapterLayout.findViewById(R.id.id_posterImage);

            title.setText(movieList.get(position).getMovieTitle());
            Picasso.with(ListActivity.this).load("http://image.tmdb.org/t/p/w92/"+movieList.get(position).getPosterPath()).into(poster);


            return adapterLayout;
        }
    }

    public class Movie implements Serializable {

        String movieID;
        String movieTitle;
        String posterPath;


        public Movie(String movieTitle, String movieID, String posterPath){

            this.movieID=movieID;
            this.movieTitle=movieTitle;
            this.posterPath=posterPath;

        }

        public String getMovieTitle(){
            return movieTitle;
        }

        public String getMovieID() {
            return movieID;
        }

        public String getPosterPath() {
            return posterPath;
        }
    }




}
