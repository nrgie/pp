package com.blueobject.peripatosapp.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueobject.peripatosapp.App;
import com.blueobject.peripatosapp.MainActivity;
import com.blueobject.peripatosapp.R;
import com.blueobject.peripatosapp.models.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nrgie on 2018.03.10..
 */

public class GridElementAdapter extends RecyclerView.Adapter<GridElementAdapter.SimpleViewHolder>{

    private MainActivity context;
    private List<String> elements;
    private ArrayList<Card> cards;

    private String type = "cat";

    /*
    public GridElementAdapter(Context context){
        this.context = context;
        this.elements = new ArrayList<String>();
        // Fill dummy list
        for(int i = 0; i < 40 ; i++){
            this.elements.add(i, "Position : " + i);
        }
    }
    */

    public GridElementAdapter(MainActivity context, ArrayList<Card> cards, String type) {
        this.context = context;
        this.type = type;
        this.elements = new ArrayList<String>();
        this.cards = cards;
        int i = 0;
        for(Card c : cards) {
            this.elements.add(i, c.getName());
            i++;
        }

    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView button;
        public final ImageView bg;

        public SimpleViewHolder(View view) {
            super(view);
            button = (TextView) view.findViewById(R.id.nameTxt);
            bg = (ImageView) view.findViewById(R.id.bg);
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.categ_card, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {

        new App.DownloadImageTask(holder.bg).execute(cards.get(position).getImageUrl());

        holder.button.setText(elements.get(position));
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type.equals("cat"))
                    context.showSelectedCat(cards.get(position));
                else
                    context.showSelectedCourse(cards.get(position));

            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.elements.size();
    }
}
