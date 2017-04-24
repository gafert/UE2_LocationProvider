package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import fhtw.bsa2.hammer.mocklocationprovider.StartMockservice;

public class LocationActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationActivity";
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private final int MY_PERM_REQ = 99;
    private ArrayAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        checkGooglePS(this);

        // New Mockservice from Library
        StartMockservice mockService = new StartMockservice(this, getResources().openRawResource(R.raw.route_example));

        mockService.setShowLog(true);
        mockService.start();

        // Adapter with own defined layout
        listAdapter = new ArrayAdapter<String>(this, R.layout.list_element, new ArrayList<String>());

        listAdapter.add("1st Entry, made by LocationActivity");
        listAdapter.add("2nd Entry also made by LocationActivity");

        // ListView
        ListView coordinateListView = (ListView) findViewById(R.id.coordinate_list_view);
        // Relate Adapter and ListView
        coordinateListView.setAdapter(listAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    public void checkGooglePS(Context context) {
        // Create API availability object
        final GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        try {
            // Check installed version
            Log.d(TAG, getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (availability.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google PS availability: " + "ERROR");
        } else {
            Log.d(TAG, "Google PS availability: " + "SUCCESS");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                Log.d(TAG, "Version >= 23");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERM_REQ);
            } else {
                Log.d(TAG, "onConnected(): ACCESS PROBLEM");
                return;
            }
        }

        // Setup periodic location updates
        // Configure location request parameters
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //10 sec, inexact
        mLocationRequest.setFastestInterval(5000); // 5 sec, limit the updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Request updates with configuration from above
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //Get last location, if available
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Check permissions (see code in onConnected())

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            listAdapter.add(
                    "Latitude: " + String.valueOf(mLocation.getLatitude())
                    + ", Longitude: " + String.valueOf(mLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
