package com.example.movieposterfinal;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class InfoActivity extends YouTubeBaseActivity{
    String movieID;
    String title;
    JSONObject movieInfo;


    TextView movieTitle;
    TextView genres;
    TextView runtime;
    TextView releaseDate;
    TextView plot;
    TextView cast;
    TextView crew;
    TextView infoTitle;
    TextView info;
    TextView rating;

    String plotInfo;
    ArrayList<String> actors = new ArrayList<>();
    ArrayList<String> crewMembers = new ArrayList<>();



    YouTubePlayerView youTubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_info);

        movieTitle = findViewById(R.id.id_movieTitle);
        genres = findViewById(R.id.id_genre);
        runtime = findViewById(R.id.id_runtime);
        releaseDate = findViewById(R.id.id_releaseDate);
        plot = findViewById(R.id.id_plot);
        cast = findViewById(R.id.id_cast);
        crew = findViewById(R.id.id_crew);
        infoTitle = findViewById(R.id.id_infoTitle);
        info = findViewById(R.id.id_info);
        rating = findViewById(R.id.id_rating);

        youTubePlayerView = findViewById(R.id.id_youtube_player_view);


        movieID = getIntent().getStringExtra("ID");
        title = getIntent().getStringExtra("Title");

        movieTitle.setText(title);

        new getMovieData().execute();
        new getYoutubeVideo().execute();
        new getCastData().execute();

        plot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plot.setBackgroundColor(Color.rgb(165,165,165));
                plot.setTextColor(Color.BLACK);
                cast.setBackgroundResource(R.drawable.sub_gradient_background);
                cast.setTextColor(Color.WHITE);
                crew.setBackgroundResource(R.drawable.sub_gradient_background);
                crew.setTextColor(Color.WHITE);
                infoTitle.setText("Plot Summary");

                info.setText(plotInfo);
            }
        });
        cast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plot.setBackgroundResource(R.drawable.sub_gradient_background);
                plot.setTextColor(Color.WHITE);
                cast.setBackgroundColor(Color.rgb(165,165,165));
                cast.setTextColor(Color.BLACK);
                crew.setBackgroundResource(R.drawable.sub_gradient_background);
                crew.setTextColor(Color.WHITE);
                infoTitle.setText("Cast of "+title);

                String str="";
                for(int i=0; i<actors.size(); i++){
                    str+=actors.get(i)+"\n";
                }
                info.setText(str);
            }
        });
        crew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plot.setBackgroundResource(R.drawable.sub_gradient_background);
                plot.setTextColor(Color.WHITE);
                cast.setBackgroundResource(R.drawable.sub_gradient_background);
                cast.setTextColor(Color.WHITE);
                crew.setBackgroundColor(Color.rgb(165,165,165));
                crew.setTextColor(Color.BLACK);
                infoTitle.setText("Crew of "+title);

                String str="";
                for(int i=0; i<crewMembers.size(); i++){
                    str+=crewMembers.get(i)+"\n";
                }
                info.setText(str);
            }
        });
    }

    public class getMovieData extends AsyncTask<Void,Void,Void>{
        String homepage;

        JSONArray genreJSONs;
        String genreStrings;
        String runtimeString;

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                URL url = new URL("https://api.themoviedb.org/3/movie/"+movieID+"?api_key=13a7c960b4b265f6e81982f7fcdc3bac");
                URLConnection urlConnection = url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(inputStreamReader);
                String jsonString = "";
                String current;

                while ((current = in.readLine()) != null) {
                    jsonString += current;
                }
                movieInfo = new JSONObject(jsonString);
            }catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                homepage = movieInfo.getString("homepage");
                plotInfo = movieInfo.getString("overview");
                rating.setText(movieInfo.getString("vote_average")+"/10 TMDB Rating");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            movieTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(AnimationUtils.loadAnimation(InfoActivity.this,R.anim.image_click));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(homepage));
                    startActivity(browserIntent);
                }
            });

            try {

                genreJSONs = new JSONArray(movieInfo.getString("genres"));
                for(int i=0; i<genreJSONs.length(); i++){
                    JSONObject j = new JSONObject(genreJSONs.getString(i));
                    if(i!=genreJSONs.length()-1)
                        genreStrings+=(j.getString("name"))+", ";
                    else
                        genreStrings+=j.getString("name");
                }
                genres.setText(genreStrings.substring(4));
                runtimeString = movieInfo.getString("runtime")+" minutes";
                runtime.setText(runtimeString);
                releaseDate.setText(movieInfo.getString("release_date"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class getYoutubeVideo extends AsyncTask<Void,Void,Void>{

        JSONObject trailerJSON;
        JSONArray trailers;
        JSONObject trailer;
        String youtubeKey;

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                URL url = new URL("https://api.themoviedb.org/3/movie/"+movieID+"/videos?api_key=13a7c960b4b265f6e81982f7fcdc3bac");
                URLConnection urlConnection = url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(inputStreamReader);
                String jsonString = "";
                String current;

                while ((current = in.readLine()) != null) {
                    jsonString += current;
                }
                trailerJSON = new JSONObject(jsonString);
                trailers = new JSONArray(trailerJSON.getString("results"));
                trailer = new JSONObject(trailers.getString(0));
                youtubeKey = trailer.getString("key");

                onInitializedListener = new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        youTubePlayer.loadVideo(youtubeKey);
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                    }

                };


            }catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            youTubePlayerView.initialize("AIzaSyBNISPb_yrEe2qw9AOF1SI3bcHaToqikU4",onInitializedListener);
        }
    }

    public class getCastData extends AsyncTask<Void,Void,Void>{

        JSONObject total;
        JSONArray castJSON;
        JSONArray crewJSON;



        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("https://api.themoviedb.org/3/movie/"+movieID+"/credits?api_key=13a7c960b4b265f6e81982f7fcdc3bac");
                URLConnection urlConnection = url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(inputStreamReader);
                String jsonString = "";
                String current;

                while ((current = in.readLine()) != null) {
                    jsonString += current;
                }

                total = new JSONObject(jsonString);
                castJSON = new JSONArray(total.getString("cast"));
                for(int i=0; i<7; i++){
                    JSONObject j = new JSONObject(castJSON.getString(i));
                    actors.add(j.getString("character")+": "+j.getString("name"));
                }
                crewJSON = new JSONArray(total.getString("crew"));
                for(int i=0; i<5; i++){
                    JSONObject j = new JSONObject(crewJSON.getString(i));
                    crewMembers.add(j.getString("job")+": "+j.getString("name"));
                }


            }catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
