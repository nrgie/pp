package com.blueobject.peripatosapp.models;

/**
 * Created by nrgie on 2018.03.10..
 */

public class Card {


    public int image;
    public String imageUrl;
    public String id;
    public String name,propellant;


    public int getImage() {
        return image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getID() {
        return id;
    }

    public void setID(String i) {
        this.id = i;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setImageUrl(String image) {
        this.imageUrl = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropellant() {
        return propellant;
    }

    public void setPropellant(String propellant) {
        this.propellant = propellant;
    }

}
