package com.blueobject.peripatosapp;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.Manifest;
import android.widget.ImageView;

import com.blueobject.peripatosapp.dbpreferences.DatabaseBasedSharedPreferences;
import com.blueobject.peripatosapp.helper.ASFRequestListener;
import com.blueobject.peripatosapp.models.Tours;
import com.blueobject.peripatosapp.models.UserModel;

import com.blueobject.peripatosapp.models.Word;
import com.blueobject.peripatosapp.models.answerItem;
import com.blueobject.peripatosapp.translation_engine.translators.TextToSpechConvertor;
import com.facebook.FacebookSdk;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nrgie on 2018.01.28..
 */

public class App extends MultiDexApplication {

    private static App instance;

    public static SQLiteDatabase db;
    public static boolean run = false;
    public static DatabaseBasedSharedPreferences shared;

    public static Context appContext;

    public static UserModel user;
    public static Gson gson = new Gson();

    public static double lat = 0.0;
    public static double lng = 0.0;

    public static float speechRate = 1f;


    public static boolean speak = true;
    public static boolean music = true;

    public static TextToSpechConvertor TTS;
    public static Locale TTSLANG = new Locale("en_US");

    public static String deviceID = "";
    public static String currentMusic = "";

    public static GuideActivity.PModel shownRoute;

    public static HashMap<String, Bitmap> imagecache;

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    public static HashMap<String, ArrayList<answerItem>> currentTourAnswers = new HashMap<String, ArrayList<answerItem>>();

    public static HashMap<String, GuideActivity.PModel> showed = new HashMap<String, GuideActivity.PModel>();
    public static Location current_location;

    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    private static final String CANARO_EXTRA_BOLD_PATH = "fonts/canaro_extra_bold.otf";
    public static Typeface canaroExtraBold;

    private void initTypeface() {
        canaroExtraBold = Typeface.createFromAsset(getAssets(), CANARO_EXTRA_BOLD_PATH);
    }

    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initTypeface();

        sAnalytics = GoogleAnalytics.getInstance(this);

        appContext = getApplicationContext();
        shared = new DatabaseBasedSharedPreferences(getApplicationContext());

        imagecache = new HashMap<String, Bitmap>();

        String u = shared.getString("user", "");
        if (u.equals("")) {
            user = new UserModel();
        } else {
            user = (UserModel) gson.fromJson(shared.getString("user", ""), UserModel.class);
        }

        shared.setServiceState(true);

        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        deviceID = "";

        final String tmDevice, tmSerial, androidId;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        deviceID = deviceUuid.toString();

        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        Fabric.with(this, new Crashlytics());

        App.TTS = new TextToSpechConvertor().initialize(appContext);

    }

    @Override
    public void onTerminate() {
        if (App.TTS != null) {
            App.TTS.finish();
        }
        super.onTerminate();
    }

    public static void saveUser() {

        App.user.firebaseid = FirebaseInstanceId.getInstance().getToken();
        App.user.deviceID = deviceID;

        String userjson = gson.toJson(user);
        shared.putString("user", userjson);
        new SendUser().execute();

        Log.e("SAVE", "SAVING USER!");

    }

    public static void storeUser() {

        App.user.firebaseid = FirebaseInstanceId.getInstance().getToken();
        App.user.deviceID = deviceID;

        String userjson = gson.toJson(user);
        shared.putString("user", userjson);

    }


    private static class SendUser extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... data) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://peripatos-app.com/app/app.php");
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("user", App.shared.getString("user", "")));
                nameValuePairs.add(new BasicNameValuePair("fcm", App.user.firebaseid));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String d = reader.readLine();

            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return "";
        }
    }

    public static class CustomHostnameVerifier implements org.apache.http.conn.ssl.X509HostnameVerifier {

        @Override
        public boolean verify(String host, SSLSession session) {
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            return hv.verify(host, session);
        }

        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {
        }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException {

        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

        }
    }
    public static class CustomHttpClient extends DefaultHttpClient {

        public CustomHttpClient() {
            super();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier(new CustomHostnameVerifier());
            Scheme scheme = (new Scheme("https", socketFactory, 443));
            getConnectionManager().getSchemeRegistry().register(scheme);
        }

    }



    public static class getJson extends AsyncTask<String, String, String> {


        public ASFRequestListener<JsonObject> listener;
        public String url;
        static JsonParser parser = new JsonParser();

        public getJson(String s, ASFRequestListener<JsonObject> listener) {
            this.url = s;
            this.listener = listener;
        }

        public static HashMap<String, Object> createHashMapFromJsonString(String json) {

            JsonObject object = (JsonObject) parser.parse(json);
            Set<Map.Entry<String, JsonElement>> set = object.entrySet();
            Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
            HashMap<String, Object> map = new HashMap<String, Object>();

            while (iterator.hasNext()) {

                Map.Entry<String, JsonElement> entry = iterator.next();
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (null != value) {
                    if (!value.isJsonPrimitive()) {
                        if (value.isJsonObject()) {

                            map.put(key, createHashMapFromJsonString(value.toString()));
                        } else if (value.isJsonArray() && value.toString().contains(":")) {

                            List<HashMap<String, Object>> list = new ArrayList<>();
                            JsonArray array = value.getAsJsonArray();
                            if (null != array) {
                                for (JsonElement element : array) {
                                    list.add(createHashMapFromJsonString(element.toString()));
                                }
                                map.put(key, list);
                            }
                        } else if (value.isJsonArray() && !value.toString().contains(":")) {
                            map.put(key, value.getAsJsonArray());
                        }
                    } else {
                        map.put(key, value.getAsString());
                    }
                }
            }
            return map;
        }


        @Override
        protected String doInBackground(String... data) {
            CustomHttpClient httpclient = new CustomHttpClient();
            HttpGet httpget = new HttpGet(url);

            Log.e("APP_URL", url);

            try {
                //List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                //nameValuePairs.add(new BasicNameValuePair("user", App.shared.getString("user", "")));
                //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

                HttpResponse response = httpclient.execute(httpget);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

                StringBuilder sb  = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                String d = sb.toString();



                Log.e("APP", d);

                return d;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!s.equals("")) {
                JsonObject object = (JsonObject) parser.parse(s);
                listener.onSuccess(object);
            }
        }


    }

    public static class postJson extends AsyncTask<String, String, String> {


        public ASFRequestListener<JsonObject> listener;
        public String url;
        List<NameValuePair> data = new ArrayList<NameValuePair>(1);
        static JsonParser parser = new JsonParser();

        public postJson(String s, List<NameValuePair> data,  ASFRequestListener<JsonObject> listener) {
            this.url = s;
            this.data = data;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... str) {
            CustomHttpClient httpclient = new CustomHttpClient();
            HttpPost httppost = new HttpPost(url);
            try {

                //data.add(new BasicNameValuePair("user", App.shared.getString("user", "")));
                httppost.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

                StringBuilder sb  = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                String d = sb.toString();

                Log.e("APP", d);

                return d;

            } catch (Exception e) {

            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!s.equals("")) {
                JsonObject object = (JsonObject) parser.parse(s);
                if(listener != null)
                    listener.onSuccess(object);
            }
        }


    }


    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        String url;
        ImageView bmImage;

        public DownloadImageTask() {}

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {

            this.url = urls[0];
            if(this.url.equals("false")) return null;
            if(this.url.equals("")) return null;

            Bitmap mIcon11 = null;

            if(imagecache.get(url) != null) {
                return imagecache.get(url);
            }

            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(imagecache.get(url) == null) {
                if(result != null)
                    imagecache.put(url, result);
            }
            if(bmImage != null)
                bmImage.setImageBitmap(result);
        }
    }



    public static void fetchData() {

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
                    //new App.DownloadImageTask().execute(cat.image);
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

                    //new App.DownloadImageTask().execute(ti.image);

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



            }

            @Override
            public void onFailure(Exception e) {}

        }).execute();


    }





}
