package com.samramakrishnan.madisonbustracker.restapi;


import com.samramakrishnan.madisonbustracker.models.ResponseTripUpdate;
import com.samramakrishnan.madisonbustracker.models.ResponseVehiclePosition;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface APICalls {


    @GET("Vehicle/VehiclePositions.json")
    Single<ResponseVehiclePosition> getVehiclePositions();

    @GET("TripUpdate/TripUpdates.json")
    Single<ResponseTripUpdate> getTripUpdates();

}
