package com.blueobject.peripatosapp.ui;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.blueobject.peripatosapp.App;
import com.blueobject.peripatosapp.GuideActivity;
import com.blueobject.peripatosapp.R;
import com.blueobject.peripatosapp.models.answerItem;

import java.util.ArrayList;

/**
 * Created by nrgie on 2018.03.10..
 */

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.SimpleViewHolder>{

    private GuideActivity context;
    private ArrayList<answerItem> elements;
    private String routeid = "";

    public AnswersAdapter(GuideActivity context, ArrayList<answerItem> list, String routeid) {
        this.context = context;
        this.routeid = routeid;
        this.elements = list;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView button;
        public final CheckBox ck;

        public SimpleViewHolder(View view) {
            super(view);
            button = (TextView) view.findViewById(R.id.nameTxt);
            ck = (CheckBox) view.findViewById(R.id.checkBox);
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.answer_card, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        holder.button.setText(elements.get(position).a);

        if(elements.get(position).u.equals("1")) {
            holder.ck.setChecked(true);
        } else {
            holder.ck.setChecked(false);
        }

        holder.ck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(answerItem e: elements) {
                    e.u = "0";
                }

                if(holder.ck.isChecked()) {
                    elements.get(position).u = "1";
                }

                App.currentTourAnswers.put(routeid, elements);

                Log.e("ANSWERS", App.gson.toJson(App.currentTourAnswers));

                notifyDataSetChanged();

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
