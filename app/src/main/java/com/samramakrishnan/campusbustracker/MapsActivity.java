package com.samramakrishnan.campusbustracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.samramakrishnan.campusbustracker.models.ResponseTripUpdate;
import com.samramakrishnan.campusbustracker.models.ResponseVehiclePosition;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
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

    //private ArrayList<Route> listRoutes = new ArrayList<>();

    private int spinnerPosition;
    private String spinnerSelection;

    // Match route name to id from routes raw file
    private Multimap<String, String> matchRouteNameToId = ArrayListMultimap.create();

//    // Match Bus to Stops using stoptimes raw file
//    private Multimap<String, Stop> matchBusToStops = ArrayListMultimap.create();

    // Match stop id to stops from stops raw file
    private HashMap<String, Stop> matchStopIdToStops = new HashMap<>();

    private Multimap<Stop, TimeEstimate> stopTimeEstimates = ArrayListMultimap.create();

    private TextView tvInform; // Textview which informs user if no route is selected or unable to fetch data
    private ArrayList<String> listAdapter;
    private ArrayList<UpdateEntity> listUpdate = new ArrayList<>();
    private ArrayList<Marker> listStopMarkers = new ArrayList<>();
    private ArrayList<Marker> listBusMarkers = new ArrayList<>();
    private ArrayList<Stop>   listAllStops = new ArrayList<>();
    private ProgressBar progressBarRefresh;
    
    private long lastUpdate = -1;
    private ProgressDialog mProgressDialog;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ScheduledThreadPoolExecutor exec;
    private ScheduledFuture<?> refreshSchedule;
    private RefreshTask refreshTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInform = findViewById(R.id.tv_inform);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);




    }

    private void intializeProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("consumee", "pause");
        exec.remove(refreshTask);
        refreshSchedule.cancel(true);


    }

    @Override
    public void onResume() {
        super.onResume();
        intializeProgressDialog();
        progressBarRefresh = findViewById(R.id.progressbar_refresh);
        Log.d("consumee", "resume");
        //Added:
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Log.d("consumee", "resume");
            long delay = Utils.MAP_REFRESH_RATE; //the delay between the termination of one execution and the commencement of the next

            if(exec.getActiveCount()==0){
                Log.d("consumee", "start task inside resume");
            refreshSchedule = exec.scheduleWithFixedDelay(refreshTask, 0, delay, TimeUnit.SECONDS);}

        }
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
        Log.d("consumee", "mapReady");
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

        checkLocationPermission();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mapCamera), 1, null);

        exec = new ScheduledThreadPoolExecutor(1);

        exec.setRemoveOnCancelPolicy(true);
        long delay = Utils.MAP_REFRESH_RATE; //the delay between the termination of one execution and the commencement of the next
        refreshTask = new RefreshTask();
        if(exec.getActiveCount()==0){
            Log.d("consumee", "start task inside mapReady");
            refreshSchedule = exec.scheduleWithFixedDelay(refreshTask, 0, delay, TimeUnit.SECONDS);

    }
    }

    private void getVehiclePositions() {

        CompositeDisposable mCompositeDisposable = new CompositeDisposable();

        // Initialize the  endpoint
        APICalls mAPICalls = new RetrofitHelper().getAPICalls();
//        if(isFirst){
//            if(!mProgressDialog.isShowing())
//                mProgressDialog.show();
//
//            isFirst = false;
//        }

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

                        //Show a progress dialog when first loading and after that whenever a new route is selected and loaded


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
//                        if(mProgressDialog.isShowing()){
//                            mProgressDialog.dismiss();
//                        }
                        Utils.displayErrorDialog(MapsActivity.this, getResources().getString(R.string.server_down));
                        listTrips = new ArrayList<TripEntity>();


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
//                                if(mProgressDialog.isShowing()){
//                                    mProgressDialog.dismiss();
//                                }
                            }
                        }).doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Utils.displayErrorDialog(MapsActivity.this, getResources().getString(R.string.server_down));
//                                if(mProgressDialog.isShowing()){
//                                    mProgressDialog.dismiss();
//                                }

                            }
                        })
                        .subscribe(new Consumer<ResponseTripUpdate>() {
                            @Override
                            public void accept(ResponseTripUpdate responseTripUpdate) throws Exception {



                                Log.d("consumee", responseTripUpdate.toString());
                                listUpdate = responseTripUpdate.getEntity();

                                lastUpdate = Utils.getCurrentCSTinMillis();
                                addVehicleMarkers(false);

                            }
                        })
        );
    }

    //We need to display progress dialog if from spinnerselection vs. automatic background refresh
    private void addVehicleMarkers(boolean isFromSpinnerSelection) {


        if(spinnerPosition==0){
            tvInform.setElevation(getResources().getDimension(R.dimen.elevation));
            tvInform.setText(getResources().getString(R.string.inform_no_route));
            mMap.clear();
            return;
        }

// OBSERVABLE

        Observable.create(new ObservableOnSubscribe<Boolean>() {
            // Consider this as block similar to doInbackground of AsyncTask
            // provided subscription (observor subscribes to observable)
            // happens on background thread for eg Schedulers.io()
            @Override public void subscribe(ObservableEmitter<Boolean>
                                                    emitter) throws Exception {

                // Load the route id pertaining to the selected route


                //Unfinished. Plan is to check if the bus already mapped to stops. If not, then perform mapping
                for(int mainCounter=0; mainCounter<listBus.size(); mainCounter++) {
                    ArrayList<StopTimeUpdate> timeEstimateForBus = findTimeEstimateForBus(listBus.get(mainCounter));
                    Log.d("timeee", timeEstimateForBus.toString());


                    for (int k = 0; k < timeEstimateForBus.size(); k++) {
                        int begin = 0;
                        int end = listAllStops.size() - 1;
                        int j = 0; //mid
                        while (begin <= end) {
                            j = (begin + end) / 2;
                            if (Integer.parseInt(timeEstimateForBus.get(k).getStop_id().trim()) > Integer.parseInt((listAllStops.get(j).getId().trim()))) {
                                begin = j + 1;
                            } else if (Integer.parseInt(timeEstimateForBus.get(k).getStop_id().trim()) < Integer.parseInt((listAllStops.get(j).getId().trim()))) {
                                end = j - 1;
                            } else {
                                if (timeEstimateForBus.get(k).getArrival() != null) {
                                    stopTimeEstimates.put(listAllStops.get(j), timeEstimateForBus.get(k).getArrival());
                                    Log.d("inns" + mainCounter, listAllStops.get(j).getId() + "inns" + timeEstimateForBus.get(k).getStop_id());
                                    Log.d("estinna" + mainCounter, stopTimeEstimates.toString());
                                    //s.remove(k);
                                    break;

                                }
                                if (timeEstimateForBus.get(k).getDeparture() != null) {
                                    stopTimeEstimates.put(listAllStops.get(j), timeEstimateForBus.get(k).getDeparture());
                                    Log.d("inns" + mainCounter, listAllStops.get(j).getId() + "inns" + timeEstimateForBus.get(k).getStop_id());
                                    Log.d("estinnd" + mainCounter, stopTimeEstimates.toString());
                                    ;
                                    //s.remove(k);
                                    break;
                                }
                            }
                        }
                        if (Utils.IS_TEST_VERSION) {

                            Log.d("stoppt" + mainCounter, j + " " + stopTimeEstimates.toString());
                        }
                    }
                    Log.d("estt" + mainCounter, stopTimeEstimates.toString());
                }
                if(!emitter.isDisposed()) {
                    emitter.onNext(true);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
// As we observe on AndroidSchedulers.mainThread() Observor below
// observes on main thread, so onNext called below acts as
// onPostExecute of AsyncTask
                .subscribe(new Observer<Boolean>() { // OBSERVOR
                    @Override
                    public void onSubscribe(Disposable d) {

                        if(isFromSpinnerSelection){
                            if(!mProgressDialog.isShowing()){
                                mProgressDialog.show();
                            }
                        }
                        else{
                            progressBarRefresh.setVisibility(View.VISIBLE);
                        }

                        // This is called when Subscription happens, before
                        // subscribe of Observable gets called
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
                        String lastUpdateText = Utils.formatTimeWithSeconds(lastUpdate);
                        if(listBus.size()==0){
                            tvInform.setText("Last Updated at " + lastUpdateText + "\n" + getResources().getString(R.string.inform_no_bus));

                            listStopMarkers.clear();
                            return;
                        }
                        else{
                            tvInform.setText("Last Updated at " +lastUpdateText);
                        }
                        stopTimeEstimates.clear();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        // Update your view status as per value received from
                        // Observable
                        //mAirplaneModeView.setText(getFormattedString(mode));

                        mMap.clear();
                        LatLng stopPosition;
                        ArrayList<Marker> tmpStopMarkers = new ArrayList<>();
                        ArrayList<Marker> tmpBusMarkers = new ArrayList<>();

                        Iterator<Stop> ite = stopTimeEstimates.keySet().iterator();

                        if (listBus.size() != 0){
                            while (ite.hasNext()) {

                                Stop stop = ite.next();
                                Log.d("nxt", stop.toString());

                                stopPosition = new LatLng(stop.getLat(), stop.getLongi());

                                Marker stopMarker = mMap.addMarker(new MarkerOptions().position(stopPosition)
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.stopsmall)));


                                stopMarker.setTag(stop);


                                Iterator<TimeEstimate> estimateIterator = stopTimeEstimates.get(stop).iterator();

                                TimeEstimate tmp = null;
                                while (estimateIterator.hasNext()) {
                                    TimeEstimate nxt = estimateIterator.next();
                                    //if(nxt.getTime()*1000>=System.currentTimeMillis()-60000)
                                    if (nxt.getTime() * 1000 >= Utils.getCurrentCSTinMillis() - 60000) {// Get the earliest estimated time from busses who are yet to make a stop
                                        tmp = nxt;
                                        break;
                                    }
                                }
                                String eta;
                                if (tmp == null) {
                                    eta = "Not Available";
                                } else {
                                    eta = Utils.formatTime(tmp.getTime());
                                }
                                stopMarker.setTitle(stop.getName());
                                stopMarker.setSnippet("ETA for next bus: " + eta);
                                stopMarker.setVisible(true);

                                if (!tmpStopMarkers.contains(stopMarker))
                                    tmpStopMarkers.add(stopMarker);


                            }

                        listStopMarkers = tmpStopMarkers;
                    }

                        for(int mainCounter=0; mainCounter<listBus.size(); mainCounter++) {

                            //Plot markers of the bus
                            double lat = listBus.get(mainCounter).getVehicle().getPosition().getLatitude();
                            double longi = listBus.get(mainCounter).getVehicle().getPosition().getLongitude();
                            LatLng busPosition = new LatLng(lat,longi);
                            Marker m = mMap.addMarker(new MarkerOptions().position(busPosition).icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.bus3)).title(listBus.get(mainCounter).getVehicle().getTimestamp()+""));
                            tmpBusMarkers.add(m);

                        }

                        if(mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                        progressBarRefresh.setVisibility(View.GONE);

                    }
                    @Override
                    public void onError(Throwable e) {
                        if(mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                        Utils.displayErrorDialog(MapsActivity.this, getResources().getString(R.string.server_down));
                    }

                    @Override
                    public void onComplete() {

                    }
                });











    }

    private ArrayList<StopTimeUpdate> findTimeEstimateForBus(TripEntity tripEntity) {
        for(int i =0; i<listUpdate.size(); i++){
            if(listUpdate.get(i).getTrip_update().getVehicle().getId().equals(tripEntity.getVehicle().getVehicle().getId())){
               return listUpdate.get(i).getTrip_update().getStop_time_update();
            }
        }
        return null;
    }


//    private void assignStopsToBus() {
//        InputStream is = getResources().openRawResource(
//                getResources().getIdentifier("stop_times",
//                        "raw", getPackageName()));
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        try {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] rowData = line.split(",");
//
//                // Find stops for all buses because if the first bus on a route doesen't have a stop, other buses don't as well
//                for(int i=0; i<listBus.size(); i++){
//                    if(listBus.get(i).getVehicle().getTrip().getTrip_id().equals(rowData[0])){
//                        Stop stop = matchStopIdToStops.get(rowData[2]);
//                       matchBusToStops.put(listBus.get(i).getVehicle().getVehicle().getLabel(), stop);
//                    }
//                }
//
//
//
//            }
//        }
//        catch (IOException ex) {
//            // handle exception
//        }
//        finally {
//            try {
//                is.close();
//            }
//            catch (IOException e) {
//                // handle exception
//            }
//        }
//    }

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
                //listRoutes.add(new Route(rowData[0], rowData[3], rowData[5]));
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
        Spinner spinner = (Spinner) item.getActionView();
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
                addVehicleMarkers(true);
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

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            }
            return false;
        } else {
            mMap.setMyLocationEnabled(true);
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }
}



