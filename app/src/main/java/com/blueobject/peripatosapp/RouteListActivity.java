package com.blueobject.peripatosapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
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


import com.blueobject.peripatosapp.models.Tours;

import com.blueobject.peripatosapp.translation_engine.utils.ConversionCallback;
import com.blueobject.peripatosapp.ui.animation.GuillotineAnimation;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.blueobject.peripatosapp.App.imagecache;

public class RouteListActivity extends AppCompatActivity implements ConversionCallback {

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
    boolean ttshu = false;
    boolean ttssk = false;

    static AppCompatActivity context;


    String typeFilter = "";
    String langFilter = "en";

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

            for(Tours.TourItem i : its) {

                /*
                if(!typeFilter.equals("")) {

                    if(typeFilter.equals("train") && i.type.equals("train")) {
                        temp.add(i);
                    }

                    if(typeFilter.equals("ship") && i.type.equals("ship")) {
                        temp.add(i);
                    }

                    if(typeFilter.equals("air") && i.type.equals("air")) {
                        temp.add(i);
                    }

                } else {
                */
                    temp.add(i);
                //}
            }

            for(Tours.TourItem i : temp) {
                /*
                if(langFilter.equals("en") && i.lang.equals("en")) {
                        temp2.add(i);
                }

                if(langFilter.equals("hu") && i.lang.equals("hu")) {
                        temp2.add(i);
                }

                if(langFilter.equals("sk") && i.lang.equals("sk")) {
                     temp2.add(i);
                }
                */

                temp2.add(i);

            }

            for(Tours.TourItem i : temp2) {
                Tours.ITEMS.add(i);
                Tours.ITEM_MAP.put(i.id, i);
            }

        }

        ((SimpleItemRecyclerViewAdapter) ((RecyclerView) recyclerView).getAdapter()).flushItems(Tours.ITEMS);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        Tours.ITEMS.clear();
        Tours.ITEM_MAP.clear();

        setContentView(R.layout.activity_route_list);

        recyclerView = findViewById(R.id.route_list);



        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        createListData();



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
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        guillotineMenu.findViewById(R.id.content_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menu.isOpened()) {
                    menu.close();
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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

                    int langsk =  tts.setLanguage(new Locale("sk_SK"));
                    if(langen != TextToSpeech.LANG_MISSING_DATA) {
                        ttssk = true;
                    }

                    drawFlags();
                }
            }
        });

        App.TTSLANG = new Locale("en_US");
        App.TTS.speak("Welcome to Horizon Guide!");

        final View train = findViewById(R.id.train);
        final View ship = findViewById(R.id.ship);
        final View air = findViewById(R.id.air);

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear bg
                train.setBackgroundColor(Color.parseColor("#00000000"));
                ship.setBackgroundColor(Color.parseColor("#00000000"));
                air.setBackgroundColor(Color.parseColor("#00000000"));

                if(typeFilter.equals("")) {
                    typeFilter = "train";
                    train.setBackgroundColor(Color.parseColor("#33000000"));
                } else {
                    typeFilter = "";
                }

                createListData();

            }
        });

        ship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear bg
                train.setBackgroundColor(Color.parseColor("#00000000"));
                ship.setBackgroundColor(Color.parseColor("#00000000"));
                air.setBackgroundColor(Color.parseColor("#00000000"));

                if(typeFilter.equals("")) {
                    typeFilter = "ship";
                    ship.setBackgroundColor(Color.parseColor("#33000000"));
                } else {
                    typeFilter = "";
                }

                createListData();
            }
        });

        air.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear bg
                train.setBackgroundColor(Color.parseColor("#00000000"));
                ship.setBackgroundColor(Color.parseColor("#00000000"));
                air.setBackgroundColor(Color.parseColor("#00000000"));

                if(typeFilter.equals("")) {
                    typeFilter = "air";
                    air.setBackgroundColor(Color.parseColor("#33000000"));
                } else {
                    typeFilter = "";
                }
                createListData();
            }
        });

        final View fhu = findViewById(R.id.fhu);
        final View fen = findViewById(R.id.fen);
        final View fsk = findViewById(R.id.fsk);

        fen.setBackgroundColor(Color.parseColor("#33000000"));


        fhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fhu.setBackgroundColor(Color.parseColor("#00000000"));
                fen.setBackgroundColor(Color.parseColor("#00000000"));
                fsk.setBackgroundColor(Color.parseColor("#00000000"));

                langFilter = "hu";
                fhu.setBackgroundColor(Color.parseColor("#33000000"));

                createListData();
            }
        });

        fen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fhu.setBackgroundColor(Color.parseColor("#00000000"));
                fen.setBackgroundColor(Color.parseColor("#00000000"));
                fsk.setBackgroundColor(Color.parseColor("#00000000"));

                langFilter = "en";
                fen.setBackgroundColor(Color.parseColor("#33000000"));

                createListData();
            }
        });

        fsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fhu.setBackgroundColor(Color.parseColor("#00000000"));
                fen.setBackgroundColor(Color.parseColor("#00000000"));
                fsk.setBackgroundColor(Color.parseColor("#00000000"));

                langFilter = "sk";
                fsk.setBackgroundColor(Color.parseColor("#33000000"));

                createListData();
            }
        });


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

        private final RouteListActivity mParentActivity;
        private List<Tours.TourItem> mValues;
        private final boolean mTwoPane;

        SimpleItemRecyclerViewAdapter(RouteListActivity parent, List<Tours.TourItem> items, boolean twoPane) {
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

                App.TTS.speak(item.title);

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

            new DownloadImageTask(holder.mImageView).execute(mValues.get(position).imageurl);

            holder.mTitleView.setText(mValues.get(position).title);
            /*
            holder.mLangView.setText(mValues.get(position).lang);

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

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView mImageView;
            final TextView mTitleView;
            final TextView mLangView;
            final ImageView mType;

            ViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.routeimage);
                mType = (ImageView) view.findViewById(R.id.routetype);
                mTitleView = (TextView) view.findViewById(R.id.title);
                mLangView = (TextView) view.findViewById(R.id.lang);
            }
        }

        class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

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
        new AlertDialog.Builder(RouteListActivity.this)
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
