package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piet on 23-09-17.
 */

public class Geofencing implements ResultCallback {

    private static final long GEOFENCE_TIMEOUT = 1000 * 60 * 60 * 24;
    private static final float GEOFENCE_RADIUS = 50.0f;
    private static final String TAG = Geofencing.class.getSimpleName();


    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private PendingIntent mGeofencePendingIntent;
    private List<Geofence> mGeoFenceList;



    public Geofencing(GoogleApiClient client, Context context) {
        mGoogleApiClient = client;
        mContext = context;
        mGeoFenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
    }

    public void registerAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()
                || mGeoFenceList == null || mGeoFenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void updateGeofenceList(PlaceBuffer places) {
        mGeoFenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) return;
        for (Place place : places) {
            String placeId = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    //You can use Geofence.NEVER_EXPIRE but be careful
                    // For production app set a JobScheduler
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeoFenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;

    }


    @Override
    public void onResult(@NonNull Result result) {
        Log.i(TAG, String.format("error receiving/removing geofence : %s",
                result.getStatus().toString()));
    }
}
