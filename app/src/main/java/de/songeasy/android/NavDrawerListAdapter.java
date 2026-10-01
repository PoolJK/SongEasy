/*
 * Copyright (C) 2015 Joerg Krein
 */
package de.songeasy.android;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;


    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getGroupCount() {
        return navDrawerItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return navDrawerItems.get(groupPosition).getChildNumber();
    }

    @Override
    public Object getGroup(int groupPosition) {

        return navDrawerItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        return navDrawerItems.get(groupPosition).getChild(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView indicator = (ImageView) convertView.findViewById(R.id.indicator);

        if (getChildrenCount(groupPosition) == 0) {
            indicator.setVisibility(View.INVISIBLE);
        } else {
            indicator.setVisibility(View.VISIBLE);
            indicator.setImageResource(isExpanded ? R.drawable.ic_action_collapse : R.drawable.ic_action_expand);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        imgIcon.setImageResource(navDrawerItems.get(groupPosition).getIcon());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_child_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        imgIcon.setImageResource(navDrawerItems.get(groupPosition).getChild(childPosition).getIcon());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
}
