package com.blueobject.peripatosapp.helper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by nrgie on 2018.02.02..
 */

public class TypeIcon extends android.support.v7.widget.AppCompatImageView {


    Context c;

    public TypeIcon(Context context) {
        super(context);
        c = context;
    }

    public TypeIcon(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        c = context;

    }

    public TypeIcon(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        c = context;
    }

    public void setup() {



    }

}
