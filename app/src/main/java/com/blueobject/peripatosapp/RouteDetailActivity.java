package com.blueobject.peripatosapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueobject.peripatosapp.models.Tours;
import com.blueobject.peripatosapp.ui.animation.GuillotineAnimation;

public class RouteDetailActivity extends AppCompatActivity {

    Context context;
    Toolbar toolbar;
    CoordinatorLayout root;
    View contentHamburger;
    View recyclerView;
    GuillotineAnimation menu;

    WebView webview;
    Tours.TourItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        context = this;
        mItem = Tours.ITEM_MAP.get(getIntent().getStringExtra("item"));


        if(mItem == null) {
            finish();
        }

        webview = (WebView) findViewById(R.id.route_detail);

        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                App.currentTourAnswers.clear();
                App.shownRoute = null;

                Bundle b = new Bundle();
                b.putString("routes", App.gson.toJson(mItem.routes));
                b.putString("tour", App.gson.toJson(mItem));
                b.putBoolean("intro", true);
                Intent i = new Intent(context, GuideActivity.class);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtras(b);
                startActivity(i);
            }
        });


        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView tripname = findViewById(R.id.tripname);

        tripname.setText(Html.fromHtml(mItem.title).toString());

        webview.loadData("about:blank", "text/html", "UTF-8");
        webview.loadData(mItem.content, "text/html", "UTF-8");

        new App.DownloadImageTask((ImageView) findViewById(R.id.headerimage)).execute(mItem.image);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, RouteListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
