package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

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

import java.util.ArrayList;
import java.util.List;

import fhtw.bsa2.hammer.mocklocationprovider.StartMockservice;

public class LocationActivity extends AppCompatActivity {

    /**
     * Gets the current location with the LocationManager service
     * and prints latitude and longitude in a ListView
     * Additionally a Mock Location Provider can be activated to fake
     * the current location
     */

    private static final String TAG = "LocationActivity";
    private final int LOCATION_REQ_PERM = 99;
    private final int LOCATION_UPDATE_FREQ_MILLI = 3000;
    private final int LOCATION_MIN_DISTANCE_METER = 1;
    private StartMockservice mockservice;
    private ArrayAdapter<String> listAdapter;
    private LocationListener locationListener;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

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

        // Button to open the MapActivity
        FloatingActionButton mapButton = (FloatingActionButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent intent = new Intent(LocationActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Add ListView and its adapter with own defined layout
        // An adapter is used to change the elements in a ListView
        // Data <-> Adapter <-> ListView
        // The List coordinateList is only implemented to make it possible to add
        // elements to the top of the ListView
        final List<String> coordinateList = new ArrayList<>(); // Add elements to this
        listAdapter = new ArrayAdapter<>(this, R.layout.list_element, coordinateList);
        final ListView coordinateListView = (ListView) findViewById(R.id.coordinate_list_view);
        coordinateListView.setAdapter(listAdapter);

        // LocationListener Functions onLocationChanged() is called
        // on every location change, upd
        locationListener = new LocationListener() {
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

        // LocationManager is not instantiated as object
        // you request it as a system service
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Get Permission and setup location Manager
        setupLocationManager();
    }

    /**
     * Setup periodic location updates
     * Called after permission was accepted
     */
    private void setupLocationManager() {
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
        // How should the location be updated?
        // Request the location from network min every 3 seconds
        // And only request if there is min 1 meter difference
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_FREQ_MILLI, LOCATION_MIN_DISTANCE_METER, locationListener);
    }

    /**
     * Called after requestPermissions() was called and user made a choice
     * Get permission result and do what has to be done when they are granted
     * Permission granted -> setupLocationManager()
     * Permission denied -> make Toast
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQ_PERM: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, now get location data
                    setupLocationManager();

                } else {
                    // Permission was denied
                    Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
