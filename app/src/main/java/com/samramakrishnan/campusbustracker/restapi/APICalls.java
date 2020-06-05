package com.samramakrishnan.campusbustracker.restapi;


import com.samramakrishnan.campusbustracker.models.ResponseTripUpdate;
import com.samramakrishnan.campusbustracker.models.ResponseVehiclePosition;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APICalls {


    @GET("Vehicle/VehiclePositions.json")
    Single<ResponseVehiclePosition> getVehiclePositions();

    @GET("TripUpdate/TripUpdates.json")
    Single<ResponseTripUpdate> getTripUpdates();

}
