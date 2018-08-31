package com.blueobject.peripatosapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AdminActivity extends AppCompatActivity {

    Context context;
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        context = this;

        webview = (WebView) findViewById(R.id.route_detail);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; // as mentioned in below notes, for your case., you do 'return false'
            }
        });

        webview.loadUrl("https://peripatos-app.com/tourseditor?tours=list&usr="+App.user.wpname+"&pas="+App.user.wppass);

        findViewById(R.id.backfromadmin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    public void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AdminActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack())
            webview.goBack();
        else
            finish();
    }

}
