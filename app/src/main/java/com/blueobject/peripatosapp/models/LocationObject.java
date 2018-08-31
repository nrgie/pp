package com.blueobject.peripatosapp.models;

/**
 * Created by nrgie on 2017.08.31..
 */

public class LocationObject {

    public long lat;
    public long lng;

    public long getLatitude(){
        return this.lat;
    }

    public long getLongitude(){
        return this.lng;
    }

    public void setLatitude(long lat){
        this.lat = lat;
    }

    public void setLongitude(long lng){
        this.lng = lng;
    }

}
