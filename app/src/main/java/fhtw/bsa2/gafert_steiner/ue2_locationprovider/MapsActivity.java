package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "MapsActivity";
    private final int LOCATION_REQ_PERM = 99;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker positionMarker;
    private LocationListener ll;
    private LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        //
        // LOCATION MANAGER
        //

        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
                positionMarker.setPosition(currentPos);
                positionMarker.setTitle("Current Position (" + currentPos + ")");
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
            if (Build.VERSION.SDK_INT >= 23) {
                Log.d(TAG, "Version >= 23");
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, ll);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Default Blue Dot showing current location
        // mMap.setMyLocationEnabled(true);

        // Add a marker and move the camera
        // Default location is Vienna
        LatLng mCurrentPos = new LatLng(48, 16);
        positionMarker = mMap.addMarker(new MarkerOptions()
                .position(mCurrentPos)
                .title("Current Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentPos));
    }

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

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
