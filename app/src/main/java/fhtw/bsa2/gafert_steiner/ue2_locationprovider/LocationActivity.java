package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

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
    private final int LOCATION_REQ_PERM = 99;
    // Class member?
    public StartMockservice mockservice;
    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        //
        // SETUP GOOGLE PLAY SERVICES
        //

        // Get application context = environment information about application
        checkGooglePS(getApplicationContext());

        // Create an instance of GoogleAPIClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //
        // ADD LISTVIEW AND ITS ADAPTER
        //

        // Adapter with own defined layout
        // An adapter is used to change the elements in a ListView
        // Data <-> Adapter <-> ListView
        listAdapter = new ArrayAdapter<>(this, R.layout.list_element, new ArrayList<String>());

        // ListView containing the coordinates
        ListView coordinateListView = (ListView) findViewById(R.id.coordinate_list_view);

        // Relate Adapter and ListView
        coordinateListView.setAdapter(listAdapter);

        //
        // MOCKSERVICE
        // TODO: Make mockservice work
        //

        // Start the mockService
        // And set the gps route which it should use to route_example
        mockservice = new StartMockservice(this, getResources().openRawResource(R.raw.route_example));
        mockservice.setShowLog(true);

        // Start and stop the mockService with a switch
        Switch mockSwitch = (Switch) findViewById(R.id.mockSwitch);
        mockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mockservice.start();
                } else {
                    mockservice.shutdown();
                }
            }
        });

        //
        // OPEN MAP ON CLICK
        //

        FloatingActionButton mapButton = (FloatingActionButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent intent = new Intent(LocationActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Checks the location permissions
     * If they are not allowed ask the user to allow them
     * If they are already allowed continue with setupLocationRequest()
     * The user input (if the permission was allowed or denied) is handled
     * by the function onRequestPermissionsResult()
     */
    @Override
    public void onConnected(Bundle bundle) {
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not set
            // Only request permission if build version is higher than 23
            if (Build.VERSION.SDK_INT >= 23) {
                Log.d(TAG, "Version >= 23");
                // Request the permission ACCESS_FINE_LOCATION
                // Android shows a prompt and asks the user for permission
                // The choice (deny/allow) is handled by onRequestPermissionsResult()
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQ_PERM);
            } else {
                Log.d(TAG, "onConnected(): ACCESS PROBLEM");
                Toast.makeText(this, "You need to run Android 6.0 or above!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // If it already has permission
            setupLocationRequest();
        }
    }

    /**
     * Setup periodic location updates
     */
    private void setupLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //10 sec, inexact
        mLocationRequest.setFastestInterval(5000); // 5 sec, limit the updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Called when the location changes
     * But only in intervals specified in setupLocationRequest()
     * -> setInterval() and setFastestInterval()
     * Only called when the LocationRequest is setup -> setupLocation was called
     */
    @Override
    public void onLocationChanged(Location location) {
        // Get current location
        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            // Add the current location to the ListView -> show it to the user
            listAdapter.add(
                    "Latitude: " + String.valueOf(mLocation.getLatitude())
                            + ", Longitude: " + String.valueOf(mLocation.getLongitude()));
        }
    }

    /**
     * Called after requestPermissions() was called and user made a choice
     * Get permission result and do what has to be done when they are granted
     * Permission granted -> setupLocationRequest()
     * Permission denied -> make Toast
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQ_PERM: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, now get location data
                    setupLocationRequest();

                } else {
                    // Permission was denied
                    Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    /**
     * Checks if the Google Play Services are available
     */
    private void checkGooglePS(Context context) {
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
