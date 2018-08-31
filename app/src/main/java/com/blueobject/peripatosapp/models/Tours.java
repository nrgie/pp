package com.blueobject.peripatosapp.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.blueobject.peripatosapp.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tours {

    public static final List<TourItem> ITEMS = new ArrayList<TourItem>();

    public static final Map<String, TourItem> ITEM_MAP = new HashMap<String, TourItem>();

    private static void addItem(TourItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }


    public static class RouteItem  implements Comparable {
        public String ID = "";
        public String parent = "";
        public String audio = "";
        public String content = "";
        public String excerpt = "";
        public String image = "";
        public String latitude = "";
        public String longitude = "";
        public String location = "";
        public String title = "";
        public String quiz = "";
        public String order = "";


        @Override
        public int compareTo(@NonNull Object o) {
            int comp = Integer.parseInt(((RouteItem)o).order);
            return (Integer.parseInt(this.order) - comp);
        }

    }

    public static class CategoryItem {
        public String id = "";
        public String title = "";
        public String slug = "";
        public String image = "";
        public String description = "";
    }

    public static class CourseItem {
        public String id = "";
        public String title = "";
        public String slug = "";
        public String image = "";
        public String description = "";
    }


    public static class TourItem implements Comparable {
        public String id;
        public String description = "";

        public String slug = "";
        public String imageurl = "";
        public String terms = "";
        public String course = "";
        public String audio = "";
        public String content = "";
        public String excerpt = "";
        public String image = "";
        public String lang = "";
        public int radius = 30;
        public String latitude = "";
        public String longitude = "";
        public String location = "";
        public String title = "";

        public double dist = 0;
        public String distance = "";

        public boolean showempty = false;



        public ArrayList<RouteItem> routes = new ArrayList<RouteItem>();

        public Bitmap bitmap;

        public void setRoutes(List<RouteItem> arr) {

            ArrayList<RouteItem> list = new ArrayList<RouteItem>();

            /*
            Gson gson = new Gson();
            for (int i=0 ; i<jsonArray.size() ; i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                RouteItem route = gson.fromJson(jsonObject, RouteItem.class);
                list.add(route);
            }
            */

            for(RouteItem i: arr) {
                if(i.parent.equals(this.id)) {
                  list.add(i);
                }
            }

            Collections.sort(list);

            this.routes = list;

        }

        @Override
        public String toString() { return App.gson.toJson(this); }

        @Override
        public int compareTo(@NonNull Object o) {
            double compareage = ((TourItem)o).dist;
            return (int)(this.dist-compareage);
        }
    }
}
