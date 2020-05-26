package com.samramakrishnan.campusbustracker.restapi;


import com.samramakrishnan.campusbustracker.models.ResponseVehiclePosition;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APICalls {

//    @GET("Vehicle/VehiclePositions.json")
//    Single<BaseResponse> addEvent(@Query("authkey") String authkey, @Query("latitude") String latitude,
//                                  @Query("longitude") String longitude, @Query("evtLocationName") String evtLocationName,
//                                  @Query("hostID") String hostId, @Query("evtName") String evtName,
//                                  @Query("hostName") String hostName, @Query("date") String date,
//                                  @Query("fromTime") String fromTime, @Query("toTime") String toTime,
//                                  @Query("isUniversityEvent") String isUniversityEvent, @Query("isFreeFood") String isFreeFood,
//                                  @Query("evtLongDescr") String evtLongDescr, @Query("evtImage") String img);

    @GET("Vehicle/VehiclePositions.json")
    Single<ResponseVehiclePosition> getVehiclePositions();

}
