package com.blueobject.peripatosapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nrgie on 2018.03.09..
 */

public class OnBoardActivity extends AhoyOnboarderActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AhoyOnboarderCard ahoyOnboarderCard1 = new AhoyOnboarderCard("Peripatos", "Welcome to Peripatos application!", R.drawable.peripatoslogo);
        ahoyOnboarderCard1.setBackgroundColor(R.color.black_transparent);
        ahoyOnboarderCard1.setTitleColor(R.color.white);
        ahoyOnboarderCard1.setDescriptionColor(R.color.grey_200);
        ahoyOnboarderCard1.setTitleTextSize(dpToPixels(14, this));
        ahoyOnboarderCard1.setDescriptionTextSize(dpToPixels(10, this));
        ahoyOnboarderCard1.setIconLayoutParams(300, 300, 100, 10, 10, 10);

        AhoyOnboarderCard ahoyOnboarderCard2 = new AhoyOnboarderCard("Tour with smile", "Ecudation is much fun with a guide!", R.drawable.backpack);
        ahoyOnboarderCard2.setBackgroundColor(R.color.black_transparent);
        ahoyOnboarderCard2.setTitleColor(R.color.white);
        ahoyOnboarderCard2.setDescriptionColor(R.color.grey_200);
        ahoyOnboarderCard2.setTitleTextSize(dpToPixels(14, this));
        ahoyOnboarderCard2.setDescriptionTextSize(dpToPixels(10, this));
        ahoyOnboarderCard2.setIconLayoutParams(250, 250, 100, 10, 10, 10);

        AhoyOnboarderCard ahoyOnboarderCard3 = new AhoyOnboarderCard("Learn with ease", "Accelerate learning curve by read, hear and see tour places", R.drawable.chalk);
        ahoyOnboarderCard3.setBackgroundColor(R.color.black_transparent);
        ahoyOnboarderCard3.setTitleColor(R.color.white);
        ahoyOnboarderCard3.setDescriptionColor(R.color.grey_200);
        ahoyOnboarderCard3.setTitleTextSize(dpToPixels(14, this));
        ahoyOnboarderCard3.setDescriptionTextSize(dpToPixels(10, this));
        ahoyOnboarderCard3.setIconLayoutParams(250, 250, 80, 10, 10, 10);

        setGradientBackground();
        setFinishButtonTitle("Get Started");

        List<AhoyOnboarderCard> pages = new ArrayList<>();
        pages.add(ahoyOnboarderCard1);
        pages.add(ahoyOnboarderCard2);
        pages.add(ahoyOnboarderCard3);
        setOnboardPages(pages);

    }

    @Override
    public void onFinishButtonPressed() {
        /*
        Intent intent = new Intent(App.appContext, SignActivity.class);
        startActivity(intent);
        finish();
        */
        Intent intent = new Intent(App.appContext, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
