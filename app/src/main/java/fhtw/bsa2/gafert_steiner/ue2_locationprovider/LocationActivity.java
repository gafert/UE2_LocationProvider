package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import fhtw.bsa2.hammer.mocklocationprovider.StartMockservice;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "LocationActivity";
    private final int LOCATION_REQ_PERM = 99;
    private StartMockservice mockservice;
    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<String> listAdapter;
    private LocationListener ll;
    private LocationManager lm;

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
                .addApi(LocationServices.API)
                .build();

        //
        // MOCKSERVICE
        //

        // Start the mockService
        // And set the gps route which it should use to route_example
        mockservice = new StartMockservice(this, getResources().openRawResource(R.raw.route_example));
        // mockservice.setShowLog(true);

        // Start and stop the mockService with a switch
        Switch mockSwitch = (Switch) findViewById(R.id.mockSwitch);
        mockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mockservice.start();
                } else {
                    mockservice.shutdown();
                }
            }
        });

        //
        // BUTTON TO OPEN MAP
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

        //
        // ADD LISTVIEW AND ITS ADAPTER
        //

        // Adapter with own defined layout
        // An adapter is used to change the elements in a ListView
        // Data <-> Adapter <-> ListView
        final List coordinateList = new ArrayList(); // Add elements to this
        listAdapter = new ArrayAdapter<>(this, R.layout.list_element, coordinateList);
        final ListView coordinateListView = (ListView) findViewById(R.id.coordinate_list_view);
        coordinateListView.setAdapter(listAdapter);

        //
        // LOCATION MANAGER
        //

        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Add coordiante to the top of the array and update the listview
                coordinateList.add(0, "Longitude: " + location.getLongitude() +
                        "\nLatitude: " + location.getLatitude());
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

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
            }
            return;
        }
        setupLocationRequest();

    }

    /**
     * Setup periodic location updates
     */
    private void setupLocationRequest() {
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, ll);
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
}
