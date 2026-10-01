/*
 * Copyright (C) 2015 Joerg Krein
 */
package de.songeasy.android;

/**
 * Created by Sammar on 3/21/2015.
 */

import java.util.ArrayList;

import de.songeasy.android.score.Beam;
import de.songeasy.android.score.ScoreElement;

/**
 * Item for left edit drawer
 */
public class NavDrawerItem {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    // visible icon of item
    private int icon;
    // associated class of ELEMENT item
    private Class<? extends ScoreElement> elementClass;
    // associated object of FEATURE item
    private ScoreElement feature;
    // possible child items, for expandable listview
    private ArrayList<NavDrawerItem> childItems;


    // +++++++ getters and setters +++++++
    // +++++++++++++++++++++++++++++++++++

    /**
     * Get id of icon.
     *
     * @return resource id of icon
     */
    public int getIcon(){
        return this.icon;
    }

    /**
     * Set id of icon
     *
     * @param icon resource id of icon
     */
    public void setIcon(int icon){
        this.icon = icon;
    }

    /**
     * get number of possible child items
     * @return number of child items, zero if none
     */
    public int getChildNumber() {
        if(childItems == null) return 0;
        return childItems.size();
    }

    /**
     * Get child item at index
     * @param idx   index of child item
     * @return child NavDraweritem
     */
    public NavDrawerItem getChild(int idx) {
        if(childItems == null) return null;
        return childItems.get(idx);
    }

    /**
     * Get class of ELEMENT item.
     * @return class to add
     */
    public Class<? extends ScoreElement> getElementClass() {
        return elementClass;
    }

    /**
     * Get feature of FEATURE item
     * @return feature to toggle
     */
    public ScoreElement getFeature() {
        return feature;
    }


    /**
     * Constructor of new ELEMENT item
     @param icon Icon to be shown in item
     @param childItems   array of possible child items
     @param elementClass class to add if this is a ELEMENT item
     @param feature feature to add if this is a FEATURE item
     */
    public NavDrawerItem(int icon, ArrayList<NavDrawerItem> childItems, Class<? extends ScoreElement> elementClass, ScoreElement feature) {
        this.icon = icon;
        this.childItems = childItems;
        this.elementClass = elementClass;
        this.feature = feature;
    }
}
