package com.samramakrishnan.campusbustracker;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.samramakrishnan.campusbustracker.models.Route;

import java.util.ArrayList;
import java.util.List;

// Adapter for a list of routes
public class RouteAdapter extends ArrayAdapter<Route> {

    private Context mContext;
    private List<Route> routeList = new ArrayList<>();

    public RouteAdapter(@NonNull Context context, ArrayList<Route> list) {
        super(context, 0 , list);
        mContext = context;
        routeList = list;
    }

    @Override
    public void setDropDownViewResource(int resource) {
        super.setDropDownViewResource(resource);
    }

    @Override
    public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
        super.setDropDownViewTheme(theme);
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return super.getDropDownViewTheme();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
        tv.setText(routeList.get(position).getShortName());
        return tv;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        Route currentRoute = routeList.get(position);


        TextView name = (TextView) listItem.findViewById(R.id.textView_name);
        name.setText(currentRoute.getShortName());


        return listItem;
    }
}