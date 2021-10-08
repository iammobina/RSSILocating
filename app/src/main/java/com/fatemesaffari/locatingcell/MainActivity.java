//package com.fatemesaffari.locatingcell;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Looper;
//import android.os.StrictMode;
//import android.telephony.CellInfo;
//import android.telephony.CellInfoGsm;
//import android.telephony.CellInfoLte;
//import android.telephony.CellInfoWcdma;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.room.Room;
//
//import com.fatemesaffari.locatingcell.db.AppDatabase;
//import com.fatemesaffari.locatingcell.db.ApplicationDao;
//import com.fatemesaffari.locatingcell.model.Parameters;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//
//import java.util.List;
//
//import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
//
//public class MainActivity extends AppCompatActivity implements  {
//    private static final String TAG = "MainActivity";
//
//    List<CellInfo> cellInfoList;
//    int cellID;
//    int signalStrength = 0;
//    Location currentLocation;
//    TelephonyManager telephonyManager;
//    LocationRequest mLocationRequest;
//    FusedLocationProviderClient fusedLocationProviderClient;
//    private static final int REQUEST_CODE = 101;
//    long UPDATE_INTERVAL = 5 * 1000; // 5 secs
//    long FASTEST_INTERVAL = 5 * 1000; // 5 sec
//
//
//
//    double beta = 0;
//    double p0;
//
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        appDatabase = AppDatabase.getDatabase(this);
//        dao = appDatabase.globalDao();
//        insertDb();
//
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//
//
//
////        setupRoomDatabase();
////
////        startLocationUpdates();
//    }
//
//    protected void startLocationUpdates() {
//
//        // Create the location request to start receiving updates
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
//
//        // Create LocationSettingsRequest object using location request
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//
//        // Check whether location settings are satisfied
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        settingsClient.checkLocationSettings(locationSettingsRequest);
//
//        // Start
//        getCellInfo();
//        fusedLocationProviderClient = getFusedLocationProviderClient(this);
//        fetchLocation();
//
//        // Update
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                onLocationChanged(locationResult.getLastLocation());
//            }
//        }, Looper.myLooper());
//    }
//
//    public void onLocationChanged(Location location) {
//        getCellInfo();
//
//        // New location
//        String info = " "; ///////////////////////////////////////////////////////////
//        String msg = "Updated Location: " + location.getLatitude() + "," + location.getLongitude() + "\n" + info;
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//
//        fusedLocationProviderClient = getFusedLocationProviderClient(this);
//        fetchLocation();
//    }
//
//    private void fetchLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
//            return;
//        }
//        Task<Location> task = fusedLocationProviderClient.getLastLocation();
//        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if (location != null) {
//                    currentLocation = location;
//                }
//            }
//        });
//    }
//
//    void getCellInfo() {
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        assert telephonyManager != null;
//        cellInfoList = telephonyManager.getAllCellInfo();
//        for (CellInfo cellInfo : cellInfoList) {
//            if (cellInfo instanceof CellInfoGsm) {
//                cellID = ((CellInfoGsm) cellInfo).getCellIdentity().getCid();
//                signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
//            }
//            if (cellInfo instanceof CellInfoLte) {
//                cellID = ((CellInfoLte) cellInfo).getCellIdentity().getCi();
//                signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
//            }
//            if (cellInfo instanceof CellInfoWcdma) {
//                cellID = ((CellInfoWcdma) cellInfo).getCellIdentity().getCid();
//                signalStrength = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
//            }
//        }
//
//        appDatabase.globalDao().insertAll(new Parameters(cellID, currentLocation.getLongitude(), currentLocation.getLatitude(), signalStrength));
//    }
//
//    void calculateParams() {
//        double x_bar = 0, y_bar = 0;
//        double sum1 = 0, sum2 = 0;
//        List<Parameters> params = appDatabase.globalDao().getAll();
//
//        // calculate beta
//        for (Parameters param : params) {
//            x_bar = param.getLatitude() + x_bar;
//            y_bar = param.getLongitude() + y_bar;
//        }
//        for (Parameters param : params) {
//            sum1 = ((param.getLatitude() - x_bar) * (param.getLongitude() - y_bar)) + sum1;
//            sum2 = Math.pow((param.getLatitude() - x_bar), 2) + sum2;
//        }
//        beta = sum1 / sum2;
//
//        // calculate p0
//        //  p0 = params.get(0).signal_strength - 10*beta*Math.log10()
//
//    }
//
//
//
//}