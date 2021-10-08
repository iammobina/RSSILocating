package com.fatemesaffari.locatingcell;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.fatemesaffari.locatingcell.db.AppDatabase;
import com.fatemesaffari.locatingcell.db.ApplicationDao;
import com.fatemesaffari.locatingcell.model.Parameters;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.List;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MapsActivity";
    List<CellInfo> cellInfoList;
    int cellID;
    int signalStrength = 0;
    double beta = 0;
    double p0;
    double x=0, y=0;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location mLastLOcation;
    TelephonyManager telephonyManager;
    private LocationRequest locationRequest;
    private static final int LOCATION_SETTINGS_REQUEST = 110;
    public static AppDatabase appDatabase;
    private ApplicationDao dao;
    private Marker mCurrLocationMarker;
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        appDatabase = AppDatabase.getDatabase(this);
        dao = appDatabase.globalDao();
        insertDb();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setLocationRequest();
        checkAndroidVersion();
//        for (int i = 0; i <dao.getAll().size() ; i++) {
//
//        Log.e(TAG, "onCreate: " + dao.getAll().get(i).getCellID());
//        }

    }

    private void insertDb() {
        AppDatabase.databaseExcuter.execute(new Runnable() {
            @Override
            public void run() {
               //dao.insertAll(new Parameters(1, 1.234e2, 1.234e2, 2));
               // dao.insertAll(new Parameters(3, 1.234e2, 1.234e2, 2));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    private void setLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        settingsBuilder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(MapsActivity.this,
                                                LOCATION_SETTINGS_REQUEST);

                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });
    }

    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MapsActivity.this).addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                }

                @Override
                public void onConnectionSuspended(int i) {
                    googleApiClient.connect();
                }
            }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                }
            }).build();
            googleApiClient.connect();
            setLocationRequest();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
//
                                status.startResolutionForResult(MapsActivity.this, REQUEST_LOCATION);
                                finish();
                            } catch (IntentSender.SendIntentException e) {
//                                Ignore the error.
                            }
                            break;


                    }
                }
            });
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    void getCellInfo() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        assert telephonyManager != null;
        cellInfoList = telephonyManager.getAllCellInfo();
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoGsm) {
                cellID = ((CellInfoGsm) cellInfo).getCellIdentity().getCid();
                signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
            }
            if (cellInfo instanceof CellInfoLte) {
                cellID = ((CellInfoLte) cellInfo).getCellIdentity().getCi();
                signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
            }
            if (cellInfo instanceof CellInfoWcdma) {
                cellID = ((CellInfoWcdma) cellInfo).getCellIdentity().getCid();
                signalStrength = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
            }
        }
        Log.e(TAG, "getCellInfo: "+"Cell id :"+cellID+"getLongitude"+ mLastLOcation.getLongitude()+"latutide"+ mLastLOcation.getLatitude()+"signal sretnt"+signalStrength );
        dao.insertAll(new Parameters(cellID, mLastLOcation.getLatitude(), mLastLOcation.getLatitude(),signalStrength));

    }
    double calculateNoise(double p0, double beta, double x, double y)
    {
        double sum = 0, noise, di;
       List<Parameters> params = appDatabase.globalDao().getAll();

        for (Parameters param : params) {
            di = Math.sqrt(Math.pow(x - param.getLatitude(),2) + Math.pow(y - param.getLongitude(),2));
            noise = param.getSignalStrength() - p0 + 10*beta*Math.log10(di);
            sum = Math.pow(noise,2) + sum;
        }
        return sum;
    }
    void calculateParams(){
        double x_bar = 0, y_bar = 0, p_bar = 0;
        double x_sum = 0, y_sum = 0, p_sum = 0;
        double sum1 = 0, sum2 = 0, step = 0.001;
        double dx = 0, dy = 0, dp = 0;
        double x_new = 0, y_new = 0, p_new = 0;
        List<Parameters> params = appDatabase.globalDao().getAll();
        // calculate beta
        for (Parameters param : appDatabase.globalDao().getAll()){
            x_sum = param.getLatitude() + x_bar;
            y_sum = param.getLongitude() + y_bar;
            p_sum = param.getSignalStrength() + p_bar;
        }

        x_bar = x_sum/params.size();
        y_bar = y_sum/params.size();
        p_bar = p_sum/params.size();

        for (Parameters param : params){
            sum1 = ((param.getLatitude() - x_bar)*(param.getLongitude() - y_bar)) + sum1;
            sum2 = Math.pow((param.getLatitude() - x_bar), 2) + sum2;
        }
        beta = sum1/sum2;

        // calculate P0, Xt, Yt
        // set P0, x, y to initial value
        x = x_bar;
        y = y_bar;
        p0 = p_bar;

        while (true)
        {
            // calculate dx, dy, dp
            double s,f = 0;
            for (Parameters param : params) {
                s = Math.pow(x - param.getLatitude(), 2) + Math.pow(y - param.getLongitude(), 2);
                dp = p0 - param.getSignalStrength() - 10*beta*Math.log10(Math.sqrt(s)) + dp;
                f = (param.getSignalStrength() + 10*beta - p0) / (s*Math.log(10));
                dx = f * (x - param.getLatitude()) + dx;
                dy = f * (y - param.getLongitude()) + dy;
            }
            dp = 2*dp;
            dx = 20 * beta * dx;
            dy = 20 * beta * dy;

            // new values
            x_new = x - step * dx;
            y_new = y - step * dy;
            p_new = p0 - step * dp;

            if (calculateNoise(p_new, beta, x_new, y_new) > calculateNoise(p0, beta, x, y))
                break;

            else {
                x = x_new;
                y = y_new;
                p0 = p_new;
            }
        }
    }

    public LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationResult.getLastLocation();
                mLastLOcation = location;

                // get info and store it
                getCellInfo();
                List<Parameters> params = appDatabase.globalDao().getAll();
                if (params.size()>10) {
                    calculateParams();
                    String msg = "P0 = " + p0 + ", " +
                            "Beta = " + beta + ", " +
                            "Xt = " + x + ", " +
                            "Yt = " + y;
                    Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_LONG).show();
                }

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                // Get Info LOcation User
                Log.e(TAG, "onLocationResult: " + location.getLatitude());
                Log.e(TAG, "onLocationResult: " + location.getLongitude());

                if (mCurrLocationMarker == null) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Longitude"+mLastLOcation.getLongitude()+"Latitude"+mLastLOcation.getLatitude()+"\n"+"Cell Id"+cellID+"Signal power"+signalStrength));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18f));
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
//            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }


    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } else {
                checkPermissionLocation();
            }
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissionLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "nut enalbel", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION_SETTINGS_REQUEST);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_SETTINGS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());
                    } else {
                        Toast.makeText(this, "nut enalbel exit", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        System.exit(0);
                    }

                }
                break;
        }
    }
}