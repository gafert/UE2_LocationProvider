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
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private final int LOCATION_REQ_PERM = 99;
    private ArrayAdapter<String> listAdapter;

    private Switch mockSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mockSwitch = (Switch) findViewById(R.id.mockSwitch);

        // Get application context = environment information about application
        checkGooglePS(getApplicationContext());

        // Create an instance of GoogleAPIClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Adapter with own defined layout
        listAdapter = new ArrayAdapter<>(this, R.layout.list_element, new ArrayList<String>());

        // ListView
        ListView coordinateListView = (ListView) findViewById(R.id.coordinate_list_view);
        // Relate Adapter and ListView
        coordinateListView.setAdapter(listAdapter);

        final StartMockservice mockservice = new StartMockservice(this, getResources().openRawResource(R.raw.route_example));
        mockservice.setShowLog(true);

        // Start and stop mockService with switch
        mockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mockservice.start();
                } else {
                    mockservice.shutdown();
                }
            }
        });

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
    public void onConnected(Bundle bundle) {

        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                Log.d(TAG, "Version >= 23");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQ_PERM);
            } else {
                Log.d(TAG, "onConnected(): ACCESS PROBLEM");
                return;
            }
        } else {
            // If it has permission already
            getLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Check permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            listAdapter.add(
                    "Latitude: " + String.valueOf(mLocation.getLatitude())
                            + ", Longitude: " + String.valueOf(mLocation.getLongitude()));
        }
    }

    // Setup periodic location updates
    public void getLocation() {
        // Configure location request parameters
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //10 sec, inexact
        mLocationRequest.setFastestInterval(5000); // 5 sec, limit the updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Request updates with configuration from above when permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // Get permission result and do what has to be done when they are granted
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQ_PERM: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, now get location data
                    getLocation();

                } else {
                    // permission was denied
                    Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
