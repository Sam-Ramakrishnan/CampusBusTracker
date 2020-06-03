package com.samramakrishnan.campusbustracker;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.samramakrishnan.campusbustracker.models.Position;
import com.samramakrishnan.campusbustracker.models.ResponseTripUpdate;
import com.samramakrishnan.campusbustracker.models.ResponseVehiclePosition;
import com.samramakrishnan.campusbustracker.models.Route;
import com.samramakrishnan.campusbustracker.models.Stop;
import com.samramakrishnan.campusbustracker.models.StopTimeUpdate;
import com.samramakrishnan.campusbustracker.models.TimeEstimate;
import com.samramakrishnan.campusbustracker.models.TripEntity;
import com.samramakrishnan.campusbustracker.models.UpdateEntity;
import com.samramakrishnan.campusbustracker.restapi.APICalls;
import com.samramakrishnan.campusbustracker.restapi.RetrofitHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<TripEntity> listTrips = new ArrayList<>();
    private ArrayList<TripEntity> listBus = new ArrayList<>();

    private Spinner spinner;
    private ArrayList<Route> listRoutes = new ArrayList<>();

    private int spinnerPosition;
    private String spinnerSelection;

    // Match route name to id from routes raw file
    private Multimap<String, String> matchRouteNameToId = ArrayListMultimap.create();

    // Match Bus to Stops using stoptimes raw file
    private Multimap<String, Stop> matchBusToStops = ArrayListMultimap.create();

    // Match stop id to stops from stops raw file
    private HashMap<String, Stop> matchStopIdToStops = new HashMap<>();

    private Multimap<Stop, TimeEstimate> stopTimeEstimates = ArrayListMultimap.create();

    private TextView tvInform; // Textview which informs user if no route is selected or unable to fetch data
    private ArrayList<String> listAdapter;
    private ArrayList<UpdateEntity> listUpdate = new ArrayList<>();
    private ArrayList<Marker> listStopMarkers = new ArrayList<>();
    private ArrayList<Marker> listBusMarkers = new ArrayList<>();
    private ArrayList<Stop>   listAllStops = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInform = findViewById(R.id.tv_inform);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(Utils.START_MAP_POSITION));
//

        CameraPosition mapCamera;

        mapCamera = CameraPosition.builder()
                    .target(Utils.START_MAP_POSITION)
                    .zoom(Utils.START_MAP_ZOOM)
                    .build();

        if(Utils.IS_TEST_VERSION) {
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mapCamera), 1, null);

        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
//        long period = Utils.MAP_REFRESH_RATE; // the period between successive executions
//        exec.scheduleAtFixedRate(new MyTask(), 0, period, TimeUnit.MICROSECONDS);
        long delay = Utils.MAP_REFRESH_RATE; //the delay between the termination of one execution and the commencement of the next
        exec.scheduleWithFixedDelay(new RefreshTask(), 0, delay, TimeUnit.SECONDS);
    }

    private void getVehiclePositions() {

        CompositeDisposable mCompositeDisposable = new CompositeDisposable();

        // Initialize the  endpoint
        APICalls mAPICalls = new RetrofitHelper().getAPICalls();

        mCompositeDisposable.add(mAPICalls.getVehiclePositions()
                .subscribeOn(Schedulers.io()) // "work" on io thread
                .observeOn(AndroidSchedulers.mainThread()) // "listen" on UIThread
                .map(new Function<ResponseVehiclePosition, ResponseVehiclePosition>() {
                    @Override
                    public ResponseVehiclePosition apply(ResponseVehiclePosition responseVehiclePosition) throws Exception {
                        // we want to have the geonames and not the wrapper object
                        return responseVehiclePosition;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
//                        mProgressDialog = new ProgressDialog(MainActivity.this);
//                        mProgressDialog.setMessage("Loading...Please wait..");
//                        mProgressDialog.show();
//                        mProgressDialog.setCancelable(false);
                    }
                })
                .doAfterSuccess(new Consumer<ResponseVehiclePosition>() {
                    @Override
                    public void accept(ResponseVehiclePosition responseVehiclePosition) throws Exception {
//                        if(mProgressDialog.isShowing()) {
//                            mProgressDialog.dismiss();
//                        }
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Utils.displayErrorDialog(MapsActivity.this, getResources().getString(R.string.server_down));
                        listTrips = new ArrayList<TripEntity>();

//                        if (eventListFragment == null) {
//                            eventListFragment = new EventListFragment();
//                        }
//                        currFragment = R.id.event_list_fragment;
//                        loadFragment(eventListFragment, F_TAG_LIST);

                    }
                })
                .subscribe(new Consumer<ResponseVehiclePosition>() {
                    @Override
                    public void accept(ResponseVehiclePosition responseVehiclePosition) throws Exception {

                        Log.d("consumee", responseVehiclePosition.toString());
                            listTrips = responseVehiclePosition.getEntity();
                            getTripUpdates();

                    }
                })
        );
    }

    private void getTripUpdates() {
        CompositeDisposable mCompositeDisposable = new CompositeDisposable();

        // Initialize the  endpoint
        APICalls mAPICalls = new RetrofitHelper().getAPICalls();

        mCompositeDisposable.add(mAPICalls.getTripUpdates()
                        .subscribeOn(Schedulers.io()) // "work" on io thread
                        .observeOn(AndroidSchedulers.mainThread()) // "listen" on UIThread
                        .map(new Function<ResponseTripUpdate, ResponseTripUpdate>() {
                            @Override
                            public ResponseTripUpdate apply(ResponseTripUpdate responseTripUpdate) throws Exception {
                                // we want to have the geonames and not the wrapper object
                                return responseTripUpdate;
                            }
                        }).doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
//                        mProgressDialog = new ProgressDialog(MainActivity.this);
//                        mProgressDialog.setMessage("Loading...Please wait..");
//                        mProgressDialog.show();
//                        mProgressDialog.setCancelable(false);
                            }
                        })
                        .doAfterSuccess(new Consumer<ResponseTripUpdate>() {
                            @Override
                            public void accept(ResponseTripUpdate responseTripUpdate) throws Exception {
//                        if(mProgressDialog.isShowing()) {
//                            mProgressDialog.dismiss();
//                        }
                            }
                        }).doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Utils.displayErrorDialog(MapsActivity.this, getResources().getString(R.string.server_down));
                                //listUpdate = new ArrayList<UpdateEntity>();

//                        if (eventListFragment == null) {
//                            eventListFragment = new EventListFragment();
//                        }
//                        currFragment = R.id.event_list_fragment;
//                        loadFragment(eventListFragment, F_TAG_LIST);

                            }
                        })
                        .subscribe(new Consumer<ResponseTripUpdate>() {
                            @Override
                            public void accept(ResponseTripUpdate responseTripUpdate) throws Exception {

                                Log.d("consumee", responseTripUpdate.toString());
                                listUpdate = responseTripUpdate.getEntity();

                                addVehicleMarkers();

                            }
                        })
        );
    }

    private void addVehicleMarkers() {
        mMap.clear();
        if(spinnerPosition==0){
            tvInform.setElevation(getResources().getDimension(R.dimen.elevation));
            tvInform.setText(getResources().getString(R.string.inform_no_route));
            return;
        }

        // Load the route id pertaining to the selected route
        Collection<String> selectedRoute = matchRouteNameToId.get(spinnerSelection);

        if(Utils.IS_TEST_VERSION){
            Log.d("selectedroute",selectedRoute.toString());
        }

        listBus.clear();
        for(int i=0; i<listTrips.size(); i++){
            if(selectedRoute.contains(listTrips.get(i).getVehicle().getTrip().getRoute_id())){ // If bus's route and selected route matches, add bus
                listBus.add(listTrips.get(i));                                                 //for display on map
            }
        }
        Log.d("bastd",listBus.toString());
        if(listBus.size()==0){
            tvInform.setText(getResources().getString(R.string.inform_no_bus));
            return;
        }
        else{
            tvInform.setText("");
        }
        stopTimeEstimates.clear();
        ArrayList<Marker> tmpBusMarkers = new ArrayList<>();
        //Unfinished. Plan is to check if the bus already mapped to stops. If not, then perform mapping
        for(int mainCounter=0; mainCounter<listBus.size(); mainCounter++){
            Collection<Stop> busStopsCollection = matchBusToStops.get(listBus.get(mainCounter).getVehicle().getVehicle().getLabel());
            ArrayList<Stop> listBusStops = new ArrayList<>(busStopsCollection);
             if(listBusStops.size()==0){

                 //assignStopsToBus(); // get all the stops of the bus
             }

             //Plot markers of the Bus Stops of this bus
            busStopsCollection = matchBusToStops.get(listBus.get(mainCounter).getVehicle().getVehicle().getLabel());
            listBusStops = new ArrayList<>(busStopsCollection);
            Log.d("bustopp",listBusStops.toString());
            LatLng stopPosition;
             ArrayList<Marker> tmpStopMarkers = new ArrayList<>();

             ArrayList<StopTimeUpdate> timeEstimateForBus = findTimeEstimateForBus(listBus.get(mainCounter));
             Log.d("timeee", timeEstimateForBus.toString());
//            for(int j=0; j<listBusStops.size(); j++){
//                stopPosition = new LatLng(listBusStops.get(j).getLat(),listBusStops.get(j).getLongi());
//
//                Marker stopMarker = mMap.addMarker(new MarkerOptions().position(stopPosition)
//                        .icon(BitmapDescriptorFactory
//                                .fromResource(R.drawable.stopsmall)).visible(false));
//
//
//                stopMarker.setTag(listBusStops.get(j));
//                if(!tmpStopMarkers.contains(stopMarker))
//                tmpStopMarkers.add(stopMarker);
//                stopTimeEstimates.clear();
//                for(int k=0; k<timeEstimateForBus.size(); k++){
//
//                    if(timeEstimateForBus.get(k).getStop_id().equals(listBusStops.get(j).getId())){
//                        if(timeEstimateForBus.get(k).getArrival()!=null) {
//                                stopTimeEstimates.put(listBusStops.get(j).getId(), timeEstimateForBus.get(k).getArrival());
//                                Log.d("inns", listBusStops.get(j).getId() + "inns" + timeEstimateForBus.get(k).getStop_id());
//                                //s.remove(k);
//                                break;
//
//                        }
//                        if(timeEstimateForBus.get(k).getDeparture()!=null){
//                            stopTimeEstimates.put(listBusStops.get(j).getId(), timeEstimateForBus.get(k).getDeparture());
//                            Log.d("inns", listBusStops.get(j).getId()+"inns"+timeEstimateForBus.get(k).getStop_id());
//                            //s.remove(k);
//                            break;
//                        }
//                    }
//                }
//                if(Utils.IS_TEST_VERSION){
//
//                    Log.d("stoppt" + i, j+" "+stopTimeEstimates.toString());
//                }
//            }




            for(int k=0; k<timeEstimateForBus.size(); k++){
                int begin =0;
                int end = listAllStops.size()-1;
                int j = 0; //mid
                while(begin<=end){
                    j = (begin + end)/2;
                    if(Integer.parseInt(timeEstimateForBus.get(k).getStop_id().trim())>Integer.parseInt((listAllStops.get(j).getId().trim()))){
                        begin = j + 1;
                    }
                    else if (Integer.parseInt(timeEstimateForBus.get(k).getStop_id().trim())<Integer.parseInt((listAllStops.get(j).getId().trim()))){
                        end = j - 1;
                    }
                    else {
                        if(timeEstimateForBus.get(k).getArrival()!=null) {
                                stopTimeEstimates.put(listAllStops.get(j), timeEstimateForBus.get(k).getArrival());
                                Log.d("inns" + mainCounter, listAllStops.get(j).getId() + "inns" + timeEstimateForBus.get(k).getStop_id());
                            Log.d("estinna" + mainCounter, stopTimeEstimates.toString());
                                //s.remove(k);
                                break;

                        }
                        if(timeEstimateForBus.get(k).getDeparture()!=null){
                            stopTimeEstimates.put(listAllStops.get(j), timeEstimateForBus.get(k).getDeparture());
                            Log.d("inns" + mainCounter, listAllStops.get(j).getId()+"inns"+timeEstimateForBus.get(k).getStop_id());
                            Log.d("estinnd" + mainCounter, stopTimeEstimates.toString());;
                            //s.remove(k);
                            break;
                        }
                    }
                }
                if(Utils.IS_TEST_VERSION){

                    Log.d("stoppt" + mainCounter, j+" "+stopTimeEstimates.toString());
                }
            }
            Log.d("estt" + mainCounter, stopTimeEstimates.toString());
            Iterator<Stop> ite = stopTimeEstimates.keySet().iterator();
            while(ite.hasNext()){

                Stop stop = ite.next();
                Log.d("nxt", stop.toString());

                stopPosition = new LatLng(stop.getLat(),stop.getLongi());

                Marker stopMarker = mMap.addMarker(new MarkerOptions().position(stopPosition)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.stopsmall)));


                stopMarker.setTag(stop);



                Iterator<TimeEstimate> estimateIterator = stopTimeEstimates.get(stop).iterator();

                TimeEstimate tmp = null;
                while (estimateIterator.hasNext()){
                    TimeEstimate nxt = estimateIterator.next();
                    if(nxt.getTime()*1000>=System.currentTimeMillis()){// Get the earliest estimated time from busses who are yet to make a stop
                        tmp = nxt;
                        break;
                    }
                }
                String eta;
                if(tmp == null){
                    eta = "Not Available";
                }
                else{
                    eta = Utils.formatTime(tmp.getTime());
                }
                stopMarker.setTitle(stop.getName());
                stopMarker.setSnippet("ETA for next bus: " + eta);
                stopMarker.setVisible(true);

                if(!tmpStopMarkers.contains(stopMarker))
                    tmpStopMarkers.add(stopMarker);

                if(Utils.IS_TEST_VERSION){
                    Log.d("markerr" + mainCounter, "");
                }

            }

                listStopMarkers = tmpStopMarkers;

//            for(int j=0; j<listStopMarkers.size(); j++){
//                Stop curr = (Stop) listStopMarkers.get(j).getTag();
//                String curr_id = curr.getId().trim();
//                Log.d("currr", curr_id);
//                Log.d("currrStopTime", stopTimeEstimates.toString());
//
//                if(stopTimeEstimates.get(curr_id).isEmpty()){
//                    listStopMarkers.get(j).remove();
//                    continue;
//                }
//                Iterator<TimeEstimate> estimateIterator = stopTimeEstimates.get(curr_id).descendingIterator();
//                TimeEstimate tmp = stopTimeEstimates.get(curr_id).first();
////                while (estimateIterator.hasNext()){
////                    TimeEstimate nxt = estimateIterator.next();
////                    if(nxt.getTime()*1000<=System.currentTimeMillis()){// Get the earliest estimated time from busses who are yet to make a stop
////                        tmp = nxt;
////                        break;
////                    }
////                }
//                String eta;
//                if(tmp.equals(null)){
//                    eta = "Not Available";
//                }
//                else{
//                    eta = Utils.formatTime(tmp.getTime());
//                }
//                listStopMarkers.get(j).setTitle(curr.getName());
//                listStopMarkers.get(j).setSnippet("ETA for next bus: " + eta);
//                listStopMarkers.get(j).setVisible(true);
//
//
//                if(Utils.IS_TEST_VERSION){
//                    Log.d("markerr" + i, j+" ");
//                }
//            }




            //Plot markers of the bus
            double lat = listBus.get(mainCounter).getVehicle().getPosition().getLatitude();
            double longi = listBus.get(mainCounter).getVehicle().getPosition().getLongitude();
            LatLng busPosition = new LatLng(lat,longi);
            Marker m = mMap.addMarker(new MarkerOptions().position(busPosition).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.bus3)).title(listBus.get(mainCounter).getVehicle().getTimestamp()+""));
            tmpBusMarkers.add(m);


        }

    }

    private ArrayList<StopTimeUpdate> findTimeEstimateForBus(TripEntity tripEntity) {
        for(int i =0; i<listUpdate.size(); i++){
            if(listUpdate.get(i).getTrip_update().getVehicle().getId().equals(tripEntity.getVehicle().getVehicle().getId())){
               return listUpdate.get(i).getTrip_update().getStop_time_update();
            }
        }
        return null;
    }

    private void assignBusToStopsAsync() {

    }

    private void assignStopsToBus() {
        InputStream is = getResources().openRawResource(
                getResources().getIdentifier("stop_times",
                        "raw", getPackageName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");

                // Find stops for all buses because if the first bus on a route doesen't have a stop, other buses don't as well
                for(int i=0; i<listBus.size(); i++){
                    if(listBus.get(i).getVehicle().getTrip().getTrip_id().equals(rowData[0])){
                        Stop stop = matchStopIdToStops.get(rowData[2]);
                       matchBusToStops.put(listBus.get(i).getVehicle().getVehicle().getLabel(), stop);
                    }
                }



            }
        }
        catch (IOException ex) {
            // handle exception
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                // handle exception
            }
        }
    }

    // Parse the csv file to map route ids to route names
    private void parseCSV(){
        parseStops();
        parseRoutes();

        if(Utils.IS_TEST_VERSION){
            Log.d("matchstop", matchStopIdToStops.toString());
        }
    }

    private void parseStops() {
        InputStream is = getResources().openRawResource(
                getResources().getIdentifier("stops",
                        "raw", getPackageName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                Stop stop = new Stop(rowData[0], rowData[2], Double.parseDouble(rowData[4]), Double.parseDouble(rowData[5] ));
                matchStopIdToStops.put(rowData[0],stop );
                listAllStops.add(stop);

            }
        }
        catch (IOException ex) {
            // handle exception
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                // handle exception
            }
        }
    }

    private void parseRoutes() {
        InputStream is = getResources().openRawResource(
                getResources().getIdentifier("routes",
                        "raw", getPackageName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                matchRouteNameToId.put(rowData[3] + " - " + rowData[5], rowData[0]);
                listRoutes.add(new Route(rowData[0], rowData[3], rowData[5]));
                //Route route = new Route(Integer.parseInt(rowData[0]), rowData[0], rowData[0], rowData[0], rowData[0], rowData[0], rowData[0], rowData[0], rowData[0], rowData[0], rowData[0], rowData[0]);


            }
        }
        catch (IOException ex) {
            // handle exception
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                // handle exception
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        parseCSV();

        MenuItem item = menu.findItem(R.id.spinner);
         spinner = (Spinner) item.getActionView();
        listAdapter = new ArrayList<String>(matchRouteNameToId.keySet());
        RouteAdapter routeAdapter = new RouteAdapter(this, listAdapter);
        routeAdapter.setDropDownViewResource(R.layout.dropdown);

//        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(routeAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPosition = position;
                spinnerSelection = listAdapter.get(position);
                addVehicleMarkers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
//            case R.id.btn_menu_account:
//////                finish();
////                return(true);


        }
        return(super.onOptionsItemSelected(item));
    }




    class RefreshTask implements Runnable {

        @Override
        public void run() {
            getVehiclePositions();
        }
    }
}



