package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import fhtw.bsa2.hammer.mocklocationprovider.StartMockservice;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // New Mockservice from Library
        StartMockservice mockService = new StartMockservice(this, getResources().openRawResource(R.raw.route_example));

        mockService.setShowLog(true);
        mockService.start();

        // Adapter with own defined layout
        // Alternatively (standard list item): R.layout.simple_list_item_1
        // Alternatively (static source): ArrayAdapter movieAdapter = new
        // ArrayAdapter<String> (this, R.layout.activity_list_view, nom_array);
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, R.layout.list_element, new ArrayList<String>());

        listAdapter.add("1st Entry, made by LocationActivity");
        listAdapter.add("2nd Entry also made by LicationActivity");

        // ListView
        ListView coordinateListView = (ListView) findViewById(R.id.coordinate_list_view);
        // Relate Adapter and ListView
        coordinateListView.setAdapter(listAdapter);
    }
}
