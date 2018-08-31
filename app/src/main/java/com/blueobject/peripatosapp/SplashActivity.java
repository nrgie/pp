package com.blueobject.peripatosapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.blueobject.peripatosapp.helper.ASFRequestListener;
import com.blueobject.peripatosapp.models.Tours;
import com.blueobject.peripatosapp.models.Word;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nrgie on 2018.02.02..
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            App.current_location = location;
                            App.lat = location.getLatitude();
                            App.lng = location.getLongitude();
                        }
                    }
                });





        new App.getJson("https://peripatos-app.com/?json=get_tours", new ASFRequestListener<JsonObject>() {
            @Override
            public void onSuccess(JsonObject response) {

                Gson g = new Gson();

                List<Tours.TourItem> TourList = new ArrayList<Tours.TourItem>();
                List<Tours.RouteItem> TourPointList = new ArrayList<Tours.RouteItem>();
                List<Tours.CategoryItem> CatList = new ArrayList<Tours.CategoryItem>();
                List<Tours.CourseItem> CourseList = new ArrayList<Tours.CourseItem>();

                List<Word> huwords = new ArrayList<>();
                List<Word> enwords = new ArrayList<>();
                List<Word> dewords = new ArrayList<>();
                List<Word> skwords = new ArrayList<>();

                JsonArray cats = response.getAsJsonArray("categories");
                JsonArray courses = response.getAsJsonArray("courses");
                JsonArray tours = response.getAsJsonArray("tours");
                JsonArray tourpoints = response.getAsJsonArray("tourpoints");

                JsonArray words = response.getAsJsonArray("words");

                for(int i=0; i<cats.size(); i++) {
                    JsonObject c = (JsonObject) cats.get(i);
                    Tours.CategoryItem cat = new Tours.CategoryItem();
                    cat.id = c.get("id").getAsString();
                    cat.title = c.get("title").getAsString();
                    cat.image = c.get("image").getAsString();
                    cat.description = c.get("description").getAsString();
                    cat.slug = c.get("slug").getAsString();

                    CatList.add(cat);
                    new App.DownloadImageTask().execute(cat.image);
                }

                for(int i=0; i<courses.size(); i++) {
                    JsonObject c = (JsonObject) courses.get(i);
                    Tours.CourseItem cat = new Tours.CourseItem();
                    cat.id = c.get("id").getAsString();
                    cat.title = c.get("title").getAsString();
                    cat.image = c.get("image").getAsString();
                    cat.description = c.get("description").getAsString();
                    cat.slug = c.get("slug").getAsString();

                    CourseList.add(cat);
                    new App.DownloadImageTask().execute(cat.image);
                }


                for(int i=0; i<tourpoints.size(); i++) {
                    JsonObject c = (JsonObject) tourpoints.get(i);

                    Tours.RouteItem ri = new Tours.RouteItem();

                    ri.ID = c.get("ID").getAsString();
                    ri.parent = c.get("parent").getAsString();
                    ri.title = c.get("title").getAsString();
                    ri.content = c.get("content").getAsString();
                    ri.excerpt = c.get("excerpt").getAsString();
                    ri.audio = c.get("audio").getAsString();

                    ri.image = c.get("image").getAsString();
                    ri.latitude = c.get("latitude").getAsString();
                    ri.longitude = c.get("longitude").getAsString();
                    ri.location = c.get("location").getAsString();

                    ri.quiz = c.get("quiz").getAsString();
                    ri.order = c.get("order").getAsString();

                    TourPointList.add(ri);

                }

                for(int i=0; i<tours.size(); i++) {
                    JsonObject c = (JsonObject) tours.get(i);

                    Tours.TourItem ti = new Tours.TourItem();

                    ti.id = c.get("ID").getAsString();
                    ti.title = c.get("title").getAsString();
                    ti.content = c.get("content").getAsString();
                    ti.excerpt = c.get("excerpt").getAsString();
                    ti.audio = c.get("audio").getAsString();
                    ti.terms = c.get("terms").getAsString();

                    ti.course = c.get("course").getAsString();

                    ti.lang = c.get("lang").getAsString();
                    ti.radius = Integer.parseInt(c.get("radius").getAsString());

                    ti.image = c.get("image").getAsString();
                    ti.latitude = c.get("latitude").getAsString();
                    ti.longitude = c.get("longitude").getAsString();
                    ti.location = c.get("location").getAsString();

                    ti.setRoutes(TourPointList);

                    TourList.add(ti);

                    Log.e("APP IMAGE", ti.image);

                    new App.DownloadImageTask().execute(ti.image);

                }


                for(int i=0; i<words.size(); i++) {
                    JsonObject c = (JsonObject) words.get(i);
                    String lang = c.get("lang").getAsString();
                    String word = c.get("word").getAsString();
                    String swap = c.get("swap").getAsString();

                    Word w = new Word();
                    w.word = word;
                    w.swap = swap;
                    w.lang = lang;

                    if(lang.equals("hu")) huwords.add(w);
                    if(lang.equals("en")) enwords.add(w);
                    if(lang.equals("de")) dewords.add(w);
                    if(lang.equals("sk")) skwords.add(w);

                }

                App.shared.putString("huwords", App.gson.toJson(huwords));
                App.shared.putString("enwords", App.gson.toJson(enwords));
                App.shared.putString("dewords", App.gson.toJson(dewords));
                App.shared.putString("skwords", App.gson.toJson(skwords));

                App.shared.putString("tours", App.gson.toJson(TourList));
                App.shared.putString("cats", App.gson.toJson(CatList));
                App.shared.putString("courses", App.gson.toJson(CourseList));


                // check user login && show depend on
                if(App.user.wpid.equals("")) {
                    // no user

                    if(!App.shared.getBoolean("firsttime", false)) {

                        findViewById(R.id.splash).setVisibility(View.GONE);
                        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

                        App.shared.putBoolean("firsttime", true);
                        Intent intent = new Intent(App.appContext, OnBoardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        /*
                        Intent intent = new Intent(App.appContext, SignActivity.class);
                        startActivity(intent);
                        finish();
                        */
                        Intent intent = new Intent(App.appContext, MainActivity.class);
                        //Intent intent = new Intent(App.appContext, OnBoardActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {

                    Intent intent = new Intent(App.appContext, MainActivity.class);
                    startActivity(intent);
                    finish();

                }

            }

            @Override
            public void onFailure(Exception e) {}

        }).execute();


    }
}
