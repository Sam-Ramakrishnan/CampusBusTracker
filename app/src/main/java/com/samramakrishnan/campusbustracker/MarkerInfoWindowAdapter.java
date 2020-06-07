package com.samramakrishnan.campusbustracker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.samramakrishnan.campusbustracker.models.MarkerData;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Context context;
    LayoutInflater inflater;
    public MarkerInfoWindowAdapter(Context ctx) {
        this.context = ctx;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_info_window, null);
        TextView tv_stop_name = v.findViewById(R.id.tv_stop_name);
        TextView tv_eta = v.findViewById(R.id.tv_eta);
        Button btn = v.findViewById(R.id.btn_see_on_map);
        Object obj = marker.getTag();
        if(obj instanceof MarkerData){
           String name = ((MarkerData) obj).getStop().getName();
           String eta  = ((MarkerData) obj).getTimeEstimate().getEta();
           tv_stop_name.setText(name);
           if(eta.equals("ETA Not Available")){
               btn.setVisibility(View.INVISIBLE);
           }
           else{
               btn.setVisibility(View.VISIBLE);
           }
           tv_eta.setText(eta);
        }
        return v;
    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }



}