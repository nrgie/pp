package com.blueobject.peripatosapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blueobject.peripatosapp.models.Card;
import com.blueobject.peripatosapp.models.Tours;
import com.blueobject.peripatosapp.models.UserModel;
import com.blueobject.peripatosapp.translation_engine.utils.ConversionCallback;
import com.blueobject.peripatosapp.ui.GridElementAdapter;
import com.blueobject.peripatosapp.ui.animation.GuillotineAnimation;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.SimpleOnSearchActionListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.blueobject.peripatosapp.App.imagecache;

public class MainActivity extends AppCompatActivity implements ConversionCallback {

    private boolean mTwoPane;

    private UtteranceProgressListener uteranceListener;
    private int TTS_DATA_CHECK_CODE = 0;
    private int RESULT_TALK_CODE = 1;
    private TextToSpeech myTTS;


    // UI Stuff
    private EditText speechEdit;
    private ImageButton talkToTextButton;
    private Button speechButton;
    private SeekBar speechRateSeekBar;
    private SeekBar pitchSeekBar;

    TextToSpeech tts;
    boolean ttsen = false;
    boolean ttsde = false;
    boolean ttshu = false;
    boolean ttssk = false;

    static AppCompatActivity context;


    String textFilter = "";
    String catFilter = "";
    String courseFilter = "";
    String langFilter = "all";

    Toolbar toolbar;
    CoordinatorLayout root;
    View contentHamburger;
    View recyclerView;
    GuillotineAnimation menu;

    public void createListData(){
        final String items = App.shared.getString("tours", "");

        Tours.ITEMS.clear();
        Tours.ITEM_MAP.clear();

        if (!items.equals("")) {

            ArrayList<Tours.TourItem> its = App.gson.fromJson(App.shared.getString("tours", ""), new TypeToken<ArrayList<Tours.TourItem>>() {}.getType());
            ArrayList<Tours.TourItem> temp = new ArrayList<>();
            ArrayList<Tours.TourItem> temp2 = new ArrayList<>();
            ArrayList<Tours.TourItem> temp3 = new ArrayList<>();
            ArrayList<Tours.TourItem> temp4 = new ArrayList<>();

            for(Tours.TourItem i : its) {

                if(!textFilter.equals("")) {

                    if(i.title.toLowerCase().contains(textFilter.toLowerCase()) || i.description.toLowerCase().contains(textFilter.toLowerCase())) {
                        temp.add(i);
                    }

                } else {
                    temp.add(i);
                }
            }

            Log.e("TEXTFILTER", "->" +textFilter);

            for(Tours.TourItem i : temp) {

                if(langFilter.equals("all")) {
                    temp2.add(i);
                }

                if(langFilter.equals("en") && i.lang.equals("en")) {
                    temp2.add(i);
                }

                if(langFilter.equals("hu") && i.lang.equals("hu")) {
                    temp2.add(i);
                }

                if(langFilter.equals("sk") && i.lang.equals("sk")) {
                    temp2.add(i);
                }

            }

            Log.e("LANGFILTER", "->" +langFilter);

            for(Tours.TourItem i : temp2) {
                if(!catFilter.equals("")) {
                    if (i.terms.contains(catFilter)) {
                        temp3.add(i);
                    }
                } else {
                    temp3.add(i);
                }
            }

            Log.e("CATFILTER", "->" +catFilter);

            for(Tours.TourItem i : temp3) {
                if(!courseFilter.equals("")) {
                    if (i.course.contains(courseFilter)) {
                        temp4.add(i);
                    }
                } else {
                    temp4.add(i);
                }
            }

            Log.e("COURSEFILTER", "->" +courseFilter);

            if(temp4.size() == 0) {
                Tours.TourItem item = new Tours.TourItem();
                item.title = "No tours found :( ";
                item.showempty = true;
                temp4.add(item);
            }

            for(Tours.TourItem i : temp4) {

                Location loc = new Location("point");

                if(i.routes.size() > 0) {

                    if(!i.routes.get(0).latitude.equals("") && !i.routes.get(0).longitude.equals("")) {

                        loc.setLatitude(Double.parseDouble(i.routes.get(0).latitude));
                        loc.setLongitude(Double.parseDouble(i.routes.get(0).longitude));

                        float dist = 0;
                        if (App.current_location != null) {
                            dist = App.current_location.distanceTo(loc);
                        }

                        i.dist = dist;
                        i.distance = "Distance: " + (int) dist / 1000 + " km";

                        Log.e("DIST", i.distance);
                    }
                }

                Tours.ITEMS.add(i);
                Tours.ITEM_MAP.put(i.id, i);
            }

        }

        ((SimpleItemRecyclerViewAdapter) ((RecyclerView) recyclerView).getAdapter()).flushItems(Tours.ITEMS);

    }

    public void showNearbyTours(){

        final String items = App.shared.getString("tours", "");
        ArrayList<Tours.TourItem> temp = new ArrayList<>();

        Tours.ITEMS.clear();
        Tours.ITEM_MAP.clear();


        ArrayList<Tours.TourItem> its = App.gson.fromJson(App.shared.getString("tours", ""), new TypeToken<ArrayList<Tours.TourItem>>() {}.getType());

        for(Tours.TourItem i : its) {

            Location loc = new Location("point");

            if(i.routes.size() > 0) {

                String lat = i.routes.get(0).latitude;
                String lng = i.routes.get(0).longitude;

                if(!lat.equals("") && !lng.equals("")) {

                    loc.setLatitude(Double.parseDouble(lat));
                    loc.setLongitude(Double.parseDouble(lng));

                    float dist = 0;
                    if (App.current_location != null) {
                        dist = App.current_location.distanceTo(loc);
                    }

                    i.dist = dist;
                    i.distance = "Distance: " + (int) dist / 1000 + " km";

                    Log.e("DIST", i.distance);
                }

            }

            Tours.ITEMS.add(i);
            Tours.ITEM_MAP.put(i.id, i);

        }

        Collections.sort(Tours.ITEMS);

        ((SimpleItemRecyclerViewAdapter) ((RecyclerView) recyclerView).getAdapter()).flushItems(Tours.ITEMS);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        Tours.ITEMS.clear();
        Tours.ITEM_MAP.clear();

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.route_list);

        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        //createListData();

        root = findViewById(R.id.root);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        contentHamburger = findViewById(R.id.content_hamburger);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FirebaseMessaging.getInstance().subscribeToTopic("news");

        //dump id.
        App.saveUser();


        final long RIPPLE_DURATION = 500;
        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.menu, null);
        root.addView(guillotineMenu);
        menu = new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.content_hamburger),  contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();

        contentHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!menu.isOpened()) {
                    menu.open();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.main).setVisibility(View.GONE);
                        }
                    }, 1000);

                }
            }
        });

        guillotineMenu.findViewById(R.id.content_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menu.isOpened()) {
                    menu.close();
                    findViewById(R.id.main).setVisibility(View.VISIBLE);
                }
            }
        });

        if (!App.user.wpadmin) {
            guillotineMenu.findViewById(R.id.admin_group).setVisibility(View.GONE);
        }

        guillotineMenu.findViewById(R.id.admin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (App.user.wpadmin) {
                    Intent intent = new Intent(App.appContext, AdminActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                //}
            }
        });

        guillotineMenu.findViewById(R.id.feed_group).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  showList();
                  if(menu.isOpened()) {
                      menu.close();
                      findViewById(R.id.main).setVisibility(View.VISIBLE);
                  }
              }
        });

        guillotineMenu.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.shared.putString("user", "");
                App.user = new UserModel();
                Intent intent = new Intent(App.appContext, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        Button findnearby = (Button) findViewById(R.id.findnear);
        findnearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                findViewById(R.id.cards).setVisibility(View.GONE);
                TextView title = findViewById(R.id.pagetitle);
                title.setText("Nearby Tours");
                findViewById(R.id.content_hamburger).setVisibility(View.GONE);
                findViewById(R.id.backfromlist).setVisibility(View.VISIBLE);
                findViewById(R.id.route_list).setVisibility(View.VISIBLE);


                catFilter = "";
                courseFilter = "";

                showNearbyTours();
            }
        });




        if (findViewById(R.id.route_detail_container) != null) {
            mTwoPane = false;
        }

        tts = new TextToSpeech(App.appContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {

                    int langen =  tts.setLanguage(new Locale("en_US"));
                    if(langen != TextToSpeech.LANG_MISSING_DATA) {
                        ttsen = true;
                    }

                    int langhu =  tts.setLanguage(new Locale("hu_HU"));
                    if(langen != TextToSpeech.LANG_MISSING_DATA) {
                        ttshu = true;
                    }

                    int langde =  tts.setLanguage(new Locale("de_DE"));
                    if(langde != TextToSpeech.LANG_MISSING_DATA) {
                        ttsde = true;
                    }


                    int langsk =  tts.setLanguage(new Locale("sk_SK"));
                    if(langen != TextToSpeech.LANG_MISSING_DATA) {
                        ttssk = true;
                    }

                    drawFlags();
                }
            }
        });

        App.TTSLANG = new Locale("en_US");
        //App.TTS.speak("Welcome to Peripatos!");

        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.langs);
        spinner.setItems("ALL", "EN", "HU", "SK", "DE");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                langFilter = item.toLowerCase();
                createListData();
                //Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
            }
        });

        final MaterialSearchBar searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);

        searchBar.setOnSearchActionListener(new SimpleOnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                super.onSearchStateChanged(enabled);
                textFilter = searchBar.getText();
                showList();
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                super.onSearchConfirmed(text);
                textFilter = searchBar.getText();
                showList();
            }
        });

        findViewById(R.id.backfromlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTiles();
            }
        });

        HorizontalGridView categsgrid = findViewById(R.id.cats);
        GridElementAdapter catAdapter=new GridElementAdapter(this, getCategoryCards(), "cat");
        categsgrid.setAdapter(catAdapter);

        HorizontalGridView coursesgrid = findViewById(R.id.courses);
        GridElementAdapter courseAdapter=new GridElementAdapter(this, getCoursesCards(), "course");
        coursesgrid.setAdapter(courseAdapter);

    }

    public ArrayList<Card> getCategoryCards() {
        ArrayList<Card> categs = new ArrayList<>();
        ArrayList<Tours.CategoryItem> categitems = App.gson.fromJson(App.shared.getString("cats", ""), new TypeToken<ArrayList<Tours.CategoryItem>>() {}.getType());
        for(Tours.CategoryItem ci : categitems) {
            Card s=new Card();
            s.setID(ci.id);
            s.setName(ci.title);
            s.setPropellant(ci.description);
            s.setImageUrl(ci.image);
            categs.add(s);
        }

        return categs;
    }

    public ArrayList<Card> getCoursesCards() {
        ArrayList<Card> categs = new ArrayList<>();
        ArrayList<Tours.CourseItem> categitems = App.gson.fromJson(App.shared.getString("courses", ""), new TypeToken<ArrayList<Tours.CourseItem>>() {}.getType());
        for(Tours.CourseItem ci : categitems) {
            Card s=new Card();
            s.setID(ci.id);
            s.setName(ci.title);
            s.setPropellant(ci.description);
            s.setImageUrl(ci.image);
            categs.add(s);
        }

        return categs;
    }

    public void showList(){
        // fadeout
        findViewById(R.id.cards).setVisibility(View.GONE);
        TextView title = findViewById(R.id.pagetitle);
        title.setText("Peripatos");
        findViewById(R.id.content_hamburger).setVisibility(View.GONE);
        findViewById(R.id.backfromlist).setVisibility(View.VISIBLE);
        findViewById(R.id.route_list).setVisibility(View.VISIBLE);

        catFilter = "";
        courseFilter = "";

        createListData();
    }

    public void showSelectedCat(Card card) {
        findViewById(R.id.cards).setVisibility(View.GONE);
        TextView title = findViewById(R.id.pagetitle);
        title.setText(card.getName());

        findViewById(R.id.content_hamburger).setVisibility(View.GONE);
        findViewById(R.id.backfromlist).setVisibility(View.VISIBLE);
        findViewById(R.id.route_list).setVisibility(View.VISIBLE);

        Log.e("cardJSON", App.gson.toJson(card));

        textFilter = "";
        catFilter = card.id;
        courseFilter = "";

        createListData();
    }

    public void showSelectedCourse(Card card) {
        findViewById(R.id.cards).setVisibility(View.GONE);
        TextView title = findViewById(R.id.pagetitle);
        title.setText(card.getName());

        findViewById(R.id.content_hamburger).setVisibility(View.GONE);
        findViewById(R.id.backfromlist).setVisibility(View.VISIBLE);
        findViewById(R.id.route_list).setVisibility(View.VISIBLE);

        Log.e("cardJSON", App.gson.toJson(card));

        textFilter = "";
        catFilter =  "";
        courseFilter = card.id;

        createListData();
    }

    public void resetTiles(){
        findViewById(R.id.cards).setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.pagetitle);
        title.setText("Peripatos");
        findViewById(R.id.content_hamburger).setVisibility(View.VISIBLE);
        findViewById(R.id.backfromlist).setVisibility(View.GONE);
        findViewById(R.id.route_list).setVisibility(View.GONE);
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, Tours.ITEMS, mTwoPane));

    }

    public void drawFlags() {
        Log.e("FLAGS", "en: " + ttsen + " hu: " + ttshu + " sk: " + ttssk);
    }

    // TTS CALLBACK
    @Override
    public void onSuccess(String result) {}
    @Override
    public void onCompletion() {}
    @Override
    public void onErrorOccured(String errorMessage) {}


    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final MainActivity mParentActivity;
        private List<Tours.TourItem> mValues;
        private final boolean mTwoPane;

        SimpleItemRecyclerViewAdapter(MainActivity parent, List<Tours.TourItem> items, boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        public void flushItems(List<Tours.TourItem> items){
            mValues = items;
            notifyDataSetChanged();
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Tours.TourItem item = (Tours.TourItem) view.getTag();

                /*
                if(item.lang.equals("en"))
                    App.TTSLANG = new Locale("en_US");
                else if(item.lang.equals("hu"))
                    App.TTSLANG = new Locale("hu_HU");
                else if(item.lang.equals("sk"))
                    App.TTSLANG = new Locale("sk_SK");
                else
                */
                    App.TTSLANG = new Locale("hu_HU");

                // App.TTS.speak(item.title);

                Context context = view.getContext();
                Intent intent = new Intent(context, RouteDetailActivity.class);
                intent.putExtra("item", item.id);
                context.startActivity(intent);

            }
        };



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            if(!mValues.get(position).showempty) {
                holder.mImageView.setVisibility(View.VISIBLE);
                holder.mEmptyImage.setVisibility(View.GONE);
                new DownloadImageTask(holder.mImageView).execute(mValues.get(position).image);
                holder.itemView.setTag(mValues.get(position));
                holder.itemView.setOnClickListener(mOnClickListener);
            } else {
                holder.mEmptyImage.setVisibility(View.VISIBLE);
                holder.mImageView.setVisibility(View.GONE);
            }


            holder.mTitleView.setText(mValues.get(position).title);
            holder.mLangView.setText(mValues.get(position).lang);

            if(mValues.get(position).dist > 0)
                holder.mDistView.setText(mValues.get(position).distance);
            else
                holder.mDistView.setText("");

            holder.mType.setImageDrawable(null);

            /*
            if(mValues.get(position).type.equals("train")) {
                holder.mType.setImageDrawable(getResources().getDrawable(R.drawable.if_train_white));
            } else if(mValues.get(position).type.equals("ship")) {
                holder.mType.setImageDrawable(getResources().getDrawable(R.drawable.if_cargo_ship_white));
            } else if(mValues.get(position).type.equals("air")) {
                holder.mType.setImageDrawable(getResources().getDrawable(R.drawable.if_airplane_white));
            } else {
                holder.mType.setImageDrawable(null);
            }
            */

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView mImageView;
            final ImageView mEmptyImage;
            final TextView mTitleView;
            final TextView mLangView;
            final ImageView mType;

            final TextView mDistView;

            ViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.routeimage);
                mEmptyImage = (ImageView) view.findViewById(R.id.emptyimage);
                mType = (ImageView) view.findViewById(R.id.routetype);
                mTitleView = (TextView) view.findViewById(R.id.title);
                mDistView = (TextView) view.findViewById(R.id.distance);
                mLangView = (TextView) view.findViewById(R.id.lang);
            }
        }

        public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

            ImageView bmImage;
            String url;

            public DownloadImageTask(ImageView bmImage) {
                this.bmImage = bmImage;
            }

            protected Bitmap doInBackground(String... urls) {
                this.url = urls[0];

                Bitmap mIcon11 = null;

                if(imagecache.get(url) != null) {
                    return imagecache.get(url);
                }

                Log.e("URL", ""+url);

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
                bmImage.setImageBitmap(result);
            }
        }

    }


    @Override
    public void onBackPressed(){
        if(menu.isOpened()) {
            menu.close();
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            showDialog("Are you really want to exit ?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
    }


    public void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onDestroy() {

        if (tts != null) {
            tts.shutdown();
        }

        super.onDestroy();
    }


}
