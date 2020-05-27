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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.samramakrishnan.campusbustracker.models.ResponseVehiclePosition;
import com.samramakrishnan.campusbustracker.models.Route;
import com.samramakrishnan.campusbustracker.models.TripEntity;
import com.samramakrishnan.campusbustracker.restapi.APICalls;
import com.samramakrishnan.campusbustracker.restapi.RetrofitHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private Multimap<String, String> mapRouteNametiId = ArrayListMultimap.create();
    private TextView tvInform; // Textview which informs user if no route is selected or unable to fetch data
    private ArrayList<String> listAdapter;

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
        Collection<String> selectedRoute = mapRouteNametiId.get(spinnerSelection);

        if(Utils.IS_TEST_VERSION){
            Log.d("selectedroute",selectedRoute.toString());
        }

        listBus.clear();
        for(int i=0; i<listTrips.size(); i++){
            if(selectedRoute.contains(listTrips.get(i).getVehicle().getTrip().getRoute_id())){ // If bus's route and selected route matches, add bus
                listBus.add(listTrips.get(i));                                                 //for display on map
            }
        }

        if(listBus.size()==0){
            tvInform.setText(getResources().getString(R.string.inform_no_bus));
            return;
        }


        for(int i=0; i<listBus.size(); i++){
            double lat = listBus.get(i).getVehicle().getPosition().getLatitude();
            double longi = listBus.get(i).getVehicle().getPosition().getLongitude();
            LatLng busPosition = new LatLng(lat,longi);
            mMap.addMarker(new MarkerOptions().position(busPosition).title(listBus.get(i).getVehicle().getTimestamp()+""));
        }

    }

    // Parse the csv file to map route ids to route names
    private void parseCSV(){

        InputStream is = getResources().openRawResource(
                getResources().getIdentifier("routes",
                        "raw", getPackageName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                mapRouteNametiId.put(rowData[3] + " - " + rowData[5], rowData[0]);
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
        listAdapter = new ArrayList<String>(mapRouteNametiId.keySet());
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



