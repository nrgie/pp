package com.blueobject.peripatosapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.akexorcist.localizationactivity.LocalizationActivity;
import com.blueobject.peripatosapp.helper.PermissionUtils;
import com.blueobject.peripatosapp.models.TourState;
import com.blueobject.peripatosapp.models.Tours;
import com.blueobject.peripatosapp.models.Word;
import com.blueobject.peripatosapp.models.answerItem;
import com.blueobject.peripatosapp.models.routeListItem;
import com.blueobject.peripatosapp.service.BackgroundMusicPlayer;
import com.blueobject.peripatosapp.service.ILocation;
import com.blueobject.peripatosapp.service.RouteService;
import com.blueobject.peripatosapp.ui.AnswersAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.RoadsApi;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.SpeedLimit;

import com.google.gson.reflect.TypeToken;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.RouteListener;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.ramotion.fluidslider.FluidSlider;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by nrgie on 2018.01.31..
 */


public class GuideActivity extends LocalizationActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ILocation, OnNavigationReadyCallback,
        NavigationListener, RouteListener, ProgressChangeListener {

    Context context;
    Tours.TourItem tour;
    ArrayList<Tours.RouteItem> routes;
    ArrayList<Word> words = new ArrayList<>();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private GoogleMap mMap;
    public SharedPreferences shared;

    NavigationRoute.Builder builder;
    MapboxNavigation navigation;

    private boolean started = false;
    private android.os.Handler handler = new android.os.Handler();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = GuideActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private double latitudeValue = 0.0;
    private double longitudeValue = 0.0;

    boolean filled = false;
    boolean speak = true;

    boolean addedlocationlistener = false;

    public TourState tourstate;

    public ArrayList<PModel> markers = new ArrayList<PModel>();

    LinearLayout list;
    LayoutInflater inflater;
    WebView webview;

    TextView title;


    Tours.RouteItem currentRoute;
    PModel currentPM;

    String currentQuiz = "";

    String currentShownRoute = "";

    PModel prevShownRoute;

    GuideActivity act;

    private NavigationView navigationView;
    private boolean dropoffDialogShown;
    private Location lastKnownLocation;

    private List<Point> points = new ArrayList<>();


    @Override
    public void handleLocation(double lat, double lng) {

        latitudeValue = lat;
        longitudeValue = lng;

        Log.e("GUIDEACT", "get pos: " +lat + " - " + lng);

        fillMap();
        fetchRoutes();

    }


    public class PModel {
        public Marker marker;
        public LatLng latlng;
        public Location location;
        public Tours.RouteItem route;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);

        //init last locations
        latitudeValue = App.lat;
        longitudeValue = App.lng;

        enableMyLocation();
        fillMap();

        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setBuildingsEnabled(true);

        if(tour.radius <= 30) {
            mMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID);
        }

        fetchRoutes();

    }


    public void prepareQuiz(){

        // szóval, akkor kellene kitenni amikor váltunk, s az előzőre ! ha nem válaszolta még meg.
        // a currentQuiz akkor legyen mindig a váltás utáni előző tourpoint

        if(currentQuiz.equals("")) {
            findViewById(R.id.togglequiz).setVisibility(View.GONE);
            findViewById(R.id.quiztoolbar).setVisibility(View.GONE);
            findViewById(R.id.quiztoolunderline).setVisibility(View.GONE);
        } else {
            findViewById(R.id.togglequiz).setVisibility(View.VISIBLE);
            findViewById(R.id.quiztoolbar).setVisibility(View.VISIBLE);
            findViewById(R.id.quiztoolunderline).setVisibility(View.VISIBLE);
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoibnJnaWUiLCJhIjoiY2psaWN1enlkMXJscDNrczUzeWh4bzNqciJ9.lR9D6LONve3rboGr3r827Q");
        navigation = new MapboxNavigation(this, "pk.eyJ1IjoibnJnaWUiLCJhIjoiY2psaWN1enlkMXJscDNrczUzeWh4bzNqciJ9.lR9D6LONve3rboGr3r827Q");

        setContentView(R.layout.activity_guide);


        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);





        // welcome modal!
        // play audio if available.

        context = this;
        act = this;

        tourstate = new TourState();

        Bundle input = getIntent().getExtras();



        tour = App.gson.fromJson(input.getString("tour"), Tours.TourItem.class);

        routes = tour.routes;

        Log.e("POINTNUM", ""+routes.size());


        //routes = App.gson.fromJson(input.getString("routes"), new TypeToken<ArrayList<Tours.RouteItem>>(){}.getType());

        fixNotification(tour.title);

        if(tour.lang.equals("hu")) { words = App.gson.fromJson(App.shared.getString("huwords", "{}"), new TypeToken<ArrayList<Word>>(){}.getType());}
        if(tour.lang.equals("en")) { words = App.gson.fromJson(App.shared.getString("enwords", "{}"), new TypeToken<ArrayList<Word>>(){}.getType()); }
        if(tour.lang.equals("de")) { words = App.gson.fromJson(App.shared.getString("dewords", "{}"), new TypeToken<ArrayList<Word>>(){}.getType()); }
        if(tour.lang.equals("sk")) { words = App.gson.fromJson(App.shared.getString("skwords", "{}"), new TypeToken<ArrayList<Word>>(){}.getType()); }

        App.shared.setServiceState(true);
        startService(new Intent(this, RouteService.class));

        Log.e("MUSIC", "-> "+"https://peripatos-app.com/"+tour.audio);
        if(!tour.audio.equals("")) {

            App.currentMusic = "https://peripatos-app.com/" + tour.audio;
            if(App.music) {
                startService(new Intent(this, BackgroundMusicPlayer.class));
            } else {
                ImageView tmusic = findViewById(R.id.togglemusic);
                tmusic.setAlpha((float) 0.3);
            }
        }


        int size=0;
        for(Tours.RouteItem ri : routes) {
            if(ri.latitude != null && ri.longitude != null && !ri.latitude.equals("") && !ri.longitude.equals("")) {
                size++;
                points.add(Point.fromLngLat(Double.valueOf(ri.longitude), Double.valueOf(ri.latitude)));
            }
        }



        ArrayList<routeListItem> mTitles = new ArrayList<routeListItem>(size);

        int i=0;
        for(Tours.RouteItem ri : routes) {
            if(ri.latitude != null && ri.longitude != null && !ri.latitude.equals("") && !ri.longitude.equals("")) {
                routeListItem rli = new routeListItem();
                rli.name = ri.title;
                mTitles.add(rli);
                i++;
            }
        }

        Log.e("APP", ""+routes.size());

        webview = (WebView) findViewById(R.id.route_detail);

        mDrawerList = (ListView) findViewById(R.id.list);

        mDrawerList.setAdapter(new RouteListAdapter<String>(this, mTitles));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        webview.getSettings().setLoadsImagesAutomatically(true);
        //webview.getSettings().setJavaScriptEnabled(true);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        title = (TextView) findViewById(R.id.title);

        final ImageView tspeed = findViewById(R.id.togglespeed);
        tspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.dialog_rating, null);

                final TextView textView = v.findViewById(R.id.textView);

                final int max = 30;
                final int min = 1;
                final int total = max - min;

                final FluidSlider slider = v.findViewById(R.id.fluidSlider);
                slider.setBeginTrackingListener(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        textView.setVisibility(View.INVISIBLE);
                        return Unit.INSTANCE;
                    }
                });

                slider.setEndTrackingListener(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        textView.setVisibility(View.VISIBLE);
                        return Unit.INSTANCE;
                    }
                });

                // Java 8 lambda
                slider.setPositionListener(pos -> {
                    final String value = String.valueOf( (int) (min + total * pos) );
                    slider.setBubbleText(value);

                    App.TTS.getTTS().stop();
                    App.TTS.setSpeechRate((float)(((min + total * pos))/10));
                    if(currentPM != null)
                        speakRoute(currentPM);
                    return Unit.INSTANCE;
                });


                slider.setPosition(0.33f);
                slider.setBubbleText("10");
                slider.setStartText(String.valueOf(min));
                slider.setEndText(String.valueOf(max));

                dialogBuilder.setView(v);
                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setNegativeButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });


        final ImageView tmusic = findViewById(R.id.togglemusic);
        tmusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyServiceRunning(BackgroundMusicPlayer.class)) {
                    App.music = false;
                    stopService(new Intent(context, BackgroundMusicPlayer.class));
                    tmusic.setAlpha((float) 0.3);
                } else {
                    App.music = true;
                    startService(new Intent(context, BackgroundMusicPlayer.class));
                    tmusic.setAlpha((float) 0.85);
                }
            }
        });

        final ImageView speakbtn = (ImageView) findViewById(R.id.speakbtn);
        speakbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(App.speak) {
                    App.speak = false;
                    speakbtn.setImageDrawable(getResources().getDrawable(R.drawable.if_mute_172512));

                    if(App.TTS.getTTS() != null)
                        App.TTS.getTTS().stop();

                } else {
                    App.speak = true;
                    speakbtn.setImageDrawable(getResources().getDrawable(R.drawable.if_high_volume_172479));

                    if(currentPM != null)
                        speakRoute(currentPM);
                }

            }
        });

        if(App.speak) {
            speakbtn.setImageDrawable(getResources().getDrawable(R.drawable.if_high_volume_172479));
        } else {
            speakbtn.setImageDrawable(getResources().getDrawable(R.drawable.if_mute_172512));
        }

        final ImageView exit = (ImageView) findViewById(R.id.exit);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.dialog_suspendtour, null);

                TextView title = v.findViewById(R.id.title);
                title.setText("Saving tour");

                WebView wv = v.findViewById(R.id.webv);
                wv.loadData("Are you really want to end the trip? You can continue this tour later.", "text/html", "UTF-8");

                dialogBuilder.setView(v);
                final AlertDialog alertDialog = dialogBuilder.create();
                v.findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        App.TTS.getTTS().stop();
                        App.shared.setServiceState(false);
                        //stopService(new Intent(context, RouteService.class));
                        stopService(new Intent(context, BackgroundMusicPlayer.class));
                        RouteService.getInstance().stop();
                        App.shared.putString("state-"+tour.id, "");
                        removeNotification(); finish();
                    }
                });

                v.findViewById(R.id.btn_cont).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // save tourstat.
                        saveTour();
                        App.TTS.getTTS().stop();
                        App.shared.setServiceState(false);
                        //stopService(new Intent(context, RouteService.class));
                        stopService(new Intent(context, BackgroundMusicPlayer.class));
                        RouteService.getInstance().stop();
                        removeNotification(); finish();
                    }
                });

                alertDialog.show();
            }
        });

        inflater = LayoutInflater.from(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.togglelist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideToTop(findViewById(R.id.pager), 500);
            }
        });

        findViewById(R.id.closepager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideToBottom(findViewById(R.id.pager), 500);
            }
        });

        slideToBottom(findViewById(R.id.pager), 100);

        View.OnClickListener showQuiz = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

                //Log.e("Quiz", currentRoute.quiz);

                if(!currentQuiz.equals("")) {

                    LayoutInflater inflater = getLayoutInflater();
                    View v = inflater.inflate(R.layout.dialog_quiz, null);
                    dialogBuilder.setView(v);

                    JsonParser parser = new JsonParser();
                    JsonObject qjson = (JsonObject) parser.parse(currentQuiz);

                    String Question = "";
                    ArrayList<answerItem> Answers = new ArrayList<answerItem>();

                    try {
                        Question = qjson.get("q").getAsString();
                        JsonArray ans = qjson.get("a").getAsJsonArray();

                        for(int i=0; i<ans.size(); i++) {
                            JsonObject as = (JsonObject) ans.get(i);
                            answerItem ai = new answerItem();
                            ai.a = as.get("a").getAsString();
                            ai.c = as.get("c").getAsString();
                            Answers.add(ai);
                        }

                        VerticalGridView ansgrid = v.findViewById(R.id.answers);
                        AnswersAdapter quizAdapter=new AnswersAdapter(act, Answers, currentRoute.ID);

                        ansgrid.setAdapter(quizAdapter);

                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    TextView question = v.findViewById(R.id.question);
                    question.setText(Question);

                    dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();

                            List<NameValuePair> data = new ArrayList<NameValuePair>(1);
                            data.add(new BasicNameValuePair("wpid", App.user.wpid));
                            data.add(new BasicNameValuePair("quiz", App.gson.toJson(App.currentTourAnswers)));

                            currentQuiz =  "";
                            new App.postJson("https://peripatos-app.com/app/app.php", data, null);

                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                } else {
                    prepareQuiz();
                }
                //{"q":"Ez a kérdés ?","a":[{"a":"Válasz 1","c":"1"},{"a":"Válasz 2","c":"1"},{"a":"Válasz 3","c":"0"}]}
            }
        };

        findViewById(R.id.togglequiz).setOnClickListener(showQuiz);


        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://peripatos-app.com/tour/content.php?id="+tour.id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        if(input.getBoolean("intro")) {

            // check tourstat
            boolean hasTour = true;

            if(hasTour) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.dialog_conttour, null);

                TextView title = v.findViewById(R.id.title);
                title.setText("Start tour");

                WebView wv = v.findViewById(R.id.webv);
                wv.loadData("You had a suspended tour. Would you like to continue?", "text/html", "UTF-8");

                dialogBuilder.setView(v);
                final AlertDialog alertDialog = dialogBuilder.create();
                v.findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                v.findViewById(R.id.btn_cont).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // load tourstat
                        restoreTour();
                        // go to last point
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            } else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.dialog_starttour, null);

                TextView title = v.findViewById(R.id.title);
                title.setText("Start tour");

                WebView wv = v.findViewById(R.id.webv);
                wv.loadData(tour.excerpt, "text/html", "UTF-8");

                dialogBuilder.setView(v);
                final AlertDialog alertDialog = dialogBuilder.create();
                v.findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        }



    }

    public void speakRoute(PModel pm) {
        String text = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            text = Html.fromHtml(pm.route.content, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            text = Html.fromHtml(pm.route.content).toString();
        }

        if(App.speak && pm != null) {
            if(App.TTS.getTTS() != null) {
                App.TTS.getTTS().stop();
            }
        }

        if(tour.lang.equals("hu")) {
            App.TTSLANG = new Locale("hu_HU");
        }

        else if(tour.lang.equals("en")) {
            App.TTSLANG = new Locale("en_US");
        }

        else if(tour.lang.equals("de")) {
            App.TTSLANG = new Locale("de_DE");
        }

        else if(tour.lang.equals("sk")) {
            App.TTSLANG = new Locale("sk_SK");
        }

        else {
            App.TTSLANG = new Locale("en_US");
        }


        for(Word w: words) {
           text = text.replaceAll(w.word, w.swap);
        }


        App.TTS.speak(text);
    }

    public void zoomto(PModel pm) {
        if(tour.radius <= 30) {
            CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(pm.latlng, 16);
            mMap.animateCamera(loc);
        } else {
            CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(pm.latlng, 16);
            mMap.animateCamera(loc);
        }
    }

    public void fetchRoutes() {

        Location current = new Location("current");

        current.setLatitude(latitudeValue);
        current.setLongitude(longitudeValue);
        current.setTime(new Date().getTime());




        float min = 11111111;
        String log = "";

        int pos = 0;
        int currpos = 0;

        for(PModel pm : markers) {

            // def icon
            if(tourstate.visited.get(pm.route.ID) != null) {
                if(pm.marker != null)
                    pm.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            } else {
                if(pm.marker != null)
                    pm.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }

            float dist = current.distanceTo(pm.location);

            if(dist < min) {
                min = dist;
                log = pm.location.toString();
            }

            if(dist < tour.radius) {

                fetchRoute(Point.fromLngLat(Double.valueOf(currentRoute.latitude), Double.valueOf(currentRoute.longitude)),
                        Point.fromLngLat(Double.valueOf(pm.route.latitude), Double.valueOf(pm.route.longitude)));

                currentRoute = pm.route;
                //currentQuiz = pm.route.quiz;
                prepareQuiz();
                currentPM = pm;
                currpos = pos;


                tourstate.visited.put(pm.route.ID, pm.route.ID);
                tourstate.current = pm.route.ID;
                if(tourstate.firstpoint.equals("")) {
                    tourstate.firstpoint = pm.route.ID;
                }

                if(pm.marker != null)
                    pm.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));




            };
            pos++;

            if(tourstate.firstpoint.equals(pm.route.ID)) {
                if(pm.marker != null)
                    pm.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }

        }
        //Log.e("LOC", min + " -> " + log);

        if(currentRoute == null && App.shownRoute != null) {
            pos = 0;
            currpos = 0;
            try {
                for (PModel pm : markers) {
                    if (pm == App.shownRoute) {
                        currentRoute = pm.route;
                        //currentQuiz = pm.route.quiz;
                        prepareQuiz();
                        currentPM = pm;
                        currpos = pos;
                    }
                    pos++;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        if(currentRoute != null) {

            if(!currentShownRoute.equals(currentRoute.ID)) {

                // ekkor van váltás.

                // változott a pm, ekkor e a prevból a quiz-t feltankoljuk, és indítjuk a show-t

                if (prevShownRoute != null) {
                    currentQuiz = prevShownRoute.route.quiz;
                    prepareQuiz();
                }

                currentShownRoute = currentRoute.ID;

                mDrawerList.setItemChecked(currpos, true);
                ((RouteListAdapter) mDrawerList.getAdapter()).putSelected(currpos);
                ((RouteListAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();

                String cont = "";
                if (!currentRoute.image.equals("")) {
                    Log.e("RouteIMG", currentRoute.image);
                    cont = currentRoute.content + "<img style='width:100%;height:auto;padding-bottom:30px;' src='" + currentRoute.image + "' />";
                }

                webview.loadData("about:blank", "text/html", "UTF-8");
                webview.loadData(cont, "text/html", "UTF-8");

                title.setText(currentRoute.title);

                zoomto(currentPM);

                if (App.showed.get(currentRoute.ID) == null) {
                    App.showed.put(currentRoute.ID, currentPM);
                    speakRoute(currentPM);
                }

            } else {
                // ugyanaz a pm, eltesszük prev-be mindig
                prevShownRoute = currentPM;
                App.shownRoute = currentPM;

            }
        } else {

            webview.loadData("about:blank", "text/html", "UTF-8");

            double currdist = 100000;
            // no poi, azaz, melyik van a legközelebb hozzánk.

            if(App.current_location != null && markers.size() > 0) {
                currdist = App.current_location.distanceTo(markers.get(0).location);
                PModel nearest = null;

                for (PModel m : markers) {
                    double dist = App.current_location.distanceTo(m.location);
                    if (dist < currdist) {
                        currdist = dist;
                        nearest = m;
                    }
                }
            }

            if(mMap != null) {
                CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeValue, longitudeValue), 14);
                mMap.animateCamera(loc);
            }

            if(currdist > 1000) {
                title.setText("You reach the next place in: " + ((int)(currdist/1000)) + " km");
            } else {
                title.setText("You reach the next place in: " + ((int)currdist) + " meters");
            }

            webview.loadData("<div style='display:block;width:100%;height:200px;overflow:hidden;margin:0 auto;text-align:center;'><img src='https://horizonguide.net/img/if_search.png' style='margin:60px auto 0;width:100px;height:100px;opacity:0.25;' /></div>",
                    "text/html", "UTF-8");

            prepareQuiz();

        }

    }

    public void fillMap() {

        refreshMap(mMap);

        List<LatLng> pos = new ArrayList<LatLng>();

        for(PModel pm : markers) {
            if(pm.marker != null) pm.marker.remove();
        }

        markers.clear();

        for(Tours.RouteItem route : routes) {

            final PModel pm = new PModel();

            if(route.latitude != null && route.longitude != null && !route.latitude.equals("") && !route.longitude.equals("")) {

                pm.latlng = new LatLng(Double.parseDouble(route.latitude), Double.parseDouble(route.longitude));

                if(mMap != null) {

                    MarkerOptions mo = new MarkerOptions().position(pm.latlng).title(route.title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    pm.marker = mMap.addMarker(mo);
                }

                pm.location = new Location(route.title);
                pm.location.setLatitude(Double.parseDouble(route.latitude));
                pm.location.setLongitude(Double.parseDouble(route.longitude));
                pm.route = route;

                markers.add(pm);

                pos.add(pm.latlng);

            }
        }

    }

    private class RouteListAdapter<S> extends ArrayAdapter {

        private Context mContext;
        private ArrayList<routeListItem> rList = new ArrayList<routeListItem>();
        private int sel = 0;

        public RouteListAdapter(@NonNull Context context, ArrayList<routeListItem> list) {
            super(context, 0 , list);
            mContext = context;
            rList = list;
        }

        public void putSelected(int pos) {
            sel = pos;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(position==0)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.route_detail_first,parent,false);
            else if(position == (rList.size()-1))
                listItem = LayoutInflater.from(mContext).inflate(R.layout.route_detail_last,parent,false);
            else
                listItem = LayoutInflater.from(mContext).inflate(R.layout.route_detail,parent,false);

            String currentname = rList.get(position).name;
            TextView name = listItem.findViewById(R.id.route_detail);

            if(sel!=position)
                listItem.findViewById(R.id.view1).setBackground(getResources().getDrawable(R.drawable.dot));
            else
                listItem.findViewById(R.id.view1).setBackground(getResources().getDrawable(R.drawable.dotfull));

            name.setText(currentname);

            return listItem;
        }

    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        ((RouteListAdapter) mDrawerList.getAdapter()).putSelected(position);
        ((RouteListAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();

        mDrawerList.setItemChecked(position, true);

        if(currentRoute != null) {
            fetchRoute(Point.fromLngLat(Double.valueOf(currentRoute.latitude), Double.valueOf(currentRoute.longitude)),
                    Point.fromLngLat(Double.valueOf(markers.get(position).route.latitude), Double.valueOf(markers.get(position).route.longitude)));
        } else {
            if(mLastLocation != null)
                fetchRoute(Point.fromLngLat(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    Point.fromLngLat(Double.valueOf(markers.get(position).route.latitude), Double.valueOf(markers.get(position).route.longitude)));
        }

        currentRoute = markers.get(position).route;
        currentPM = markers.get(position);

        currentQuiz = currentRoute.quiz;
        prepareQuiz();

        tourstate.visited.put(currentRoute.ID, currentRoute.ID);
        tourstate.current = currentRoute.ID;
        if(tourstate.firstpoint.equals("")) {
            tourstate.firstpoint = currentRoute.ID;
        }

        for(PModel pm : markers) {
            if(currentPM.marker != null) {
                if(pm == currentPM)
                    pm.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                else
                    pm.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }
        }

        Log.e("Quiz", currentRoute.quiz);

        webview.loadData("about:blank", "text/html", "UTF-8");

        String cont = "";
        if (!currentRoute.image.equals("")) {
            Log.e("RouteIMG", currentRoute.image);
            cont = currentRoute.content + "<img style='width:100%;height:auto;padding-bottom:30px;' src='" + currentRoute.image + "' />";
        }


        webview.loadData(cont, "text/html", "UTF-8");
        title.setText(currentRoute.title);

        zoomto(currentPM);

        //findViewById(R.id.ratingtoolbar).setVisibility(View.VISIBLE);

        if (App.showed.get(currentRoute.ID) == null) {
            App.showed.put(currentRoute.ID, currentPM);

            speakRoute(currentPM);
        }

        slideToBottom(findViewById(R.id.pager), 200);

    }

    @Override
    public void setTitle(CharSequence title) {

    }


    public void slideToBottom(final View view, int dur){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(dur);
        animate.setFillAfter(true);
        view.startAnimation(animate);

        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                findViewById(R.id.list).setVisibility(View.GONE);
                findViewById(R.id.closepager).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public void slideToTop(View view, int dur){
        TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
        animate.setDuration(dur);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.list).setVisibility(View.VISIBLE);
                findViewById(R.id.closepager).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }



    private void enableMyLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {


            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAltitudeRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
            criteria.setBearingRequired(false);
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng coordinate = new LatLng(latitude, longitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 16);

                mMap.animateCamera(yourLocation);
            }

        }
    }

    private void refreshMap(GoogleMap mapInstance){
        if(mapInstance != null)
            mapInstance.clear();
    }

    @Override
    public void onResume() {
        RouteService.setIListener(this);
        Log.e(TAG, "onresume");
        super.onResume();
        navigationView.onResume();
        restoreTour();
        fixNotification(tour.title);
        fillMap();
        fetchRoutes();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTour();
        navigationView.onPause();
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onstart");
        //mGoogleApiClient.connect();
        RouteService.setIListener(this);
        navigationView.onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onstop");
        //mGoogleApiClient.disconnect();
        //RouteService.setIListener(null);
        super.onStop();
        navigationView.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        enableMyLocation();
        return false;
    }


    @Override
    protected void onResumeFragments() {
        Log.e(TAG, "onresumefrags");
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onBackPressed(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_suspendtour, null);

        TextView title = v.findViewById(R.id.title);
        title.setText("Saving tour");

        WebView wv = v.findViewById(R.id.webv);
        wv.loadData("Are you really want to end the trip? You can continue this tour later.", "text/html", "UTF-8");

        dialogBuilder.setView(v);
        final AlertDialog alertDialog = dialogBuilder.create();
        v.findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.TTS.getTTS().stop();
                App.shared.setServiceState(false);
                //stopService(new Intent(context, RouteService.class));
                stopService(new Intent(context, BackgroundMusicPlayer.class));
                RouteService.getInstance().stop();
                App.shared.putString("state-"+tour.id, "");
                removeNotification(); finish();

            }
        });

        v.findViewById(R.id.btn_cont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save tourstat.
                saveTour();

                App.TTS.getTTS().stop();
                App.shared.setServiceState(false);
                //stopService(new Intent(context, RouteService.class));
                stopService(new Intent(context, BackgroundMusicPlayer.class));
                RouteService.getInstance().stop();
                removeNotification(); finish();

            }
        });

        alertDialog.show();

    }

    public void saveTour() {
        App.shared.putString("state-"+tour.id, App.gson.toJson(tourstate));
    }

    public void restoreTour() {
        String state = App.shared.getString("state-"+tour.id, "");
        if(!state.equals("")) {
            tourstate = App.gson.fromJson(state, TourState.class);
        }
    }

    public void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(GuideActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "ondestroy");

        super.onDestroy();
        navigationView.onDestroy();
    }

    public void removeNotification() {

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(9999);

    }

    public void fixNotification(String name) {


        Intent intent;
        intent = new Intent(this, GuideActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle b = new Bundle();
        b.putString("routes", getIntent().getStringExtra("routes"));
        b.putString("tour", getIntent().getStringExtra("tour"));
        intent.putExtras(b);

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.peripatoslogo);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setLargeIcon(bm)
                .setContentTitle("Tour active")
                .setContentText(name)
                .setAutoCancel(false)
                .setOngoing(true)
                //.setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(9999, notificationBuilder.build());

    }


    /*-----------------------------------------------------*/


    @Override
    public void onNavigationReady(boolean ready) {
        fetchRoute(points.remove(0), points.remove(0));
    }

    @Override
    public void onCancelNavigation() {
// Navigation canceled, finish the activity
        finish();
    }

    @Override
    public void onNavigationFinished() {
// Intentionally empty
    }

    @Override
    public void onNavigationRunning() {
// Intentionally empty
    }

    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return true;
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {

    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onFailedReroute(String errorMessage) {

    }

    @Override
    public void onArrival() {
        if (!dropoffDialogShown && !points.isEmpty()) {
            showDropoffDialog();
            dropoffDialogShown = true; // Accounts for multiple arrival events
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        lastKnownLocation = location;
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationViewOptions navigationViewOptions = setupOptions(directionsRoute);
        navigationView.startNavigation(navigationViewOptions);
    }

    private void showDropoffDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.dropoff_dialog_text));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dropoff_dialog_positive_text),
                (dialogInterface, in) -> fetchRoute(getLastKnownLocation(), points.remove(0)));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dropoff_dialog_negative_text),
                (dialogInterface, in) -> {
// Do nothing
                });

        alertDialog.show();
    }

    private void fetchRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .alternatives(true)
                .build()
                .getRoute(new Callback() {

                    @Override
                    public void onResponse(Call call, Response response) {
                        startNavigation( ((DirectionsResponse)response.body()).routes().get(0));
                    }

                    @Override
                    public void onFailure(Call call, Throwable t) {

                    }
                });
    }

    private NavigationViewOptions setupOptions(DirectionsRoute directionsRoute) {
        dropoffDialogShown = false;

        NavigationViewOptions.Builder options = NavigationViewOptions.builder();
        options.directionsRoute(directionsRoute)
                .navigationListener(this)
                .progressChangeListener(this)
                .routeListener(this)
                .shouldSimulateRoute(true);
        return options.build();
    }

    private Point getLastKnownLocation() {
        return Point.fromLngLat(lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude());
    }


}

