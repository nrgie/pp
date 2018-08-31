package com.blueobject.peripatosapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.blueobject.peripatosapp.helper.ASFRequestListener;
import com.google.gson.JsonObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class SignActivity extends AppCompatActivity {

    Context context;

    public void setupSignin() {

        setContentView(R.layout.signin);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = ((TextView) findViewById(R.id.username)).getText().toString();
                final String password = ((TextView) findViewById(R.id.pass)).getText().toString();

                List<NameValuePair> data = new ArrayList<NameValuePair>(1);
                data.add(new BasicNameValuePair("user", username));
                data.add(new BasicNameValuePair("pass", password));

                new App.postJson("https://peripatos-app.com/?json=signin", data, new ASFRequestListener(){
                    @Override
                    public void onSuccess(Object response) {
                        JsonObject r = (JsonObject) response;
                        Log.e("RESPONSE", r.toString());
                        Log.e("RESPONSE succ", "" + r.get("success").getAsBoolean());

                        if(r.get("success").getAsBoolean()) {

                            //"user":{"data":{"ID":"7","user_login":"nrgie11"
                            JsonObject user = r.get("user").getAsJsonObject();
                            JsonObject userdata = user.get("data").getAsJsonObject();
                            App.user.wpid = userdata.get("ID").getAsString();
                            App.user.wpname = userdata.get("user_login").getAsString();
                            App.user.wpemail = userdata.get("user_email").getAsString();
                            App.user.wppass = password;

                            if(user.has("caps")) {
                                JsonObject caps = user.get("caps").getAsJsonObject();
                                if (caps.has("administrator"))
                                    App.user.wpadmin = caps.get("administrator").getAsBoolean();
                            }
                            App.storeUser();

                            Intent intent = new Intent(App.appContext, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        } else {

                            showDialog("Username or password is wrong.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int i) {
                                    d.dismiss();
                                }
                            });

                            //dialog from error
                        }

                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("RESPONSEFAIL", e.toString());
                    }
                }).execute();

            }
        });

        findViewById(R.id.gosignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSignup();
            }
        });

    }

    public void setupSignup() {

        setContentView(R.layout.signup);

        findViewById(R.id.signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = ((TextView) findViewById(R.id.username)).getText().toString();
                String email = ((TextView) findViewById(R.id.email)).getText().toString();
                final String password = ((TextView) findViewById(R.id.pass)).getText().toString();

                List<NameValuePair> data = new ArrayList<NameValuePair>(1);
                data.add(new BasicNameValuePair("user", username));
                data.add(new BasicNameValuePair("email", email));
                data.add(new BasicNameValuePair("pass", password));

                new App.postJson("https://peripatos-app.com/?json=signup", data, new ASFRequestListener(){
                    @Override
                    public void onSuccess(Object response) {

                        JsonObject r = (JsonObject) response;

                        Log.e("RESPONSE", r.toString());

                        Log.e("RESPONSE succ", "" + r.get("success").getAsBoolean());

                        if(r.get("success").getAsBoolean()) {

                            JsonObject user = r.get("user").getAsJsonObject();
                            JsonObject userdata = user.get("data").getAsJsonObject();
                            App.user.wpid = userdata.get("ID").getAsString();
                            App.user.wpname = userdata.get("user_login").getAsString();
                            App.user.wpemail = userdata.get("user_email").getAsString();
                            App.user.wppass = password;


                            if(userdata.has("caps")) {
                                JsonObject caps = userdata.get("caps").getAsJsonObject();
                                if (caps.has("administrator"))
                                    App.user.wpadmin = caps.get("administrator").getAsBoolean();
                            }

                            App.storeUser();

                            Intent intent = new Intent(App.appContext, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        } else {

                            showDialog("Email or username is already registered.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int i) {
                                    d.dismiss();
                                }
                            });

                            //dialog from error
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("RESPONSEFAIL", e.toString());
                    }
                }).execute();


            }
        });

        findViewById(R.id.gosignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSignin();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setupSignin();
    }

    public void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SignActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

}
