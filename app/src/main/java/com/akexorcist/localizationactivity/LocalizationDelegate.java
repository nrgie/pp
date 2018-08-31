package com.akexorcist.localizationactivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Aleksander Mielczarek on 03.04.2016.
 */
public class LocalizationDelegate {

    private static final String KEY_ACTIVITY_LOCALE_CHANGED = "activity_locale_changed";

    // Boolean flag to check that activity was recreated from locale changed.
    private boolean isLocalizationChanged = false;

    // Prepare default language.
    private Locale currentLanguage = LanguageSetting.getDefaultLanguage();

    private final Activity activity;
    private final List<OnLocaleChangedListener> localeChangedListeners = new ArrayList<>();

    public LocalizationDelegate(Activity activity) {
        this.activity = activity;
    }

    public void addOnLocaleChangedListener(OnLocaleChangedListener onLocaleChangedListener) {
        localeChangedListeners.add(onLocaleChangedListener);
    }

    public void onCreate(Bundle savedInstanceState) {
        setupLanguage();
        checkBeforeLocaleChanging();
    }

    // Provide method to set application language by country name.
    public final void setLanguage(String language) {
        Locale locale = new Locale(language);
        setLanguage(locale);
    }

    public final void setLanguage(String language, String country) {
        Locale locale = new Locale(language, country);
        setLanguage(locale);
    }

    public final void setLanguage(Locale locale) {
        if (!isCurrentLanguageSetting(locale)) {
            LanguageSetting.setLanguage(activity, locale);
            notifyLanguageChanged();
        }
    }

    public final void setDefaultLanguage(String language) {
        Locale locale = new Locale(language);
        setDefaultLanguage(locale);
    }

    public final void setDefaultLanguage(String language, String country) {
        Locale locale = new Locale(language, country);
        setDefaultLanguage(locale);
    }

    public final void setDefaultLanguage(Locale locale) {
        LanguageSetting.setDefaultLanguage(locale);
    }

    // Get current language
    public final Locale getLanguage() {
        return LanguageSetting.getCurrentLanguage();
    }

    // Check that bundle come from locale change.
    // If yes, bundle will obe remove and set boolean flag to "true".
    private void checkBeforeLocaleChanging() {
        boolean isLocalizationChanged = activity.getIntent().getBooleanExtra(KEY_ACTIVITY_LOCALE_CHANGED, false);
        if (isLocalizationChanged) {
            this.isLocalizationChanged = true;
            activity.getIntent().removeExtra(KEY_ACTIVITY_LOCALE_CHANGED);
        }
    }

    // Setup language to locale and language preference.
    // This method will called before onCreate.
    private void setupLanguage() {
        Locale locale = LanguageSetting.getLanguage(activity);
        setupLocale(locale);
        currentLanguage = locale;
        LanguageSetting.setLanguage(activity, locale);
    }

    // Set locale configuration.
    private void setupLocale(Locale locale) {
        updateLocaleConfiguration(activity, locale);
    }

    private void updateLocaleConfiguration(Context context, Locale locale) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            config.locale = locale;
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            context.getResources().updateConfiguration(config, dm);
        }
    }

    public Context attachBaseContext(Context context) {
        Locale locale = LanguageSetting.getLanguage(context);
        Configuration config = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            return context.createConfigurationContext(config);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            return context;
        }
    }

    // Avoid duplicated setup
    private boolean isCurrentLanguageSetting(Locale locale) {
        return locale.toString().equals(LanguageSetting.getCurrentLanguage().toString());
    }

    // Let's take it change! (Using recreate method that available on API 11 or more.
    private void notifyLanguageChanged() {
        sendOnBeforeLocaleChangedEvent();
        activity.getIntent().putExtra(KEY_ACTIVITY_LOCALE_CHANGED, true);
        callDummyActivity();
        activity.recreate();
    }

    // If activity is run to back stack. So we have to check if this activity is resume working.
    public void onResume() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                checkLocaleChange();
                checkAfterLocaleChanging();
            }
        });
    }

    // Check if locale has change while this activity was run to back stack.
    private void checkLocaleChange() {
        if (!isCurrentLanguageSetting(currentLanguage)) {
            sendOnBeforeLocaleChangedEvent();
            isLocalizationChanged = true;
            callDummyActivity();
            activity.recreate();
        }
    }

    // Call override method if local is really changed
    private void checkAfterLocaleChanging() {
        if (isLocalizationChanged) {
            sendOnAfterLocaleChangedEvent();
            isLocalizationChanged = false;
        }
    }

    private void sendOnBeforeLocaleChangedEvent() {
        for (OnLocaleChangedListener changedListener : localeChangedListeners) {
            changedListener.onBeforeLocaleChanged();
        }
    }

    private void sendOnAfterLocaleChangedEvent() {
        for (OnLocaleChangedListener listener : localeChangedListeners) {
            listener.onAfterLocaleChanged();
        }
    }

    private void callDummyActivity() {
        activity.startActivity(new Intent(activity, BlankDummyActivity.class));
    }
}
