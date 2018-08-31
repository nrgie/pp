package com.blueobject.peripatosapp;

import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blueobject.peripatosapp.models.Tours;


public class RouteDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    private Tours.TourItem mItem;

    public CollapsingToolbarLayout appBarLayout;
    public RouteDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = Tours.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_detail, container, false);

        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.route_detail)).setText(mItem.description);
        }

        return rootView;
    }

}
