package fhtw.bsa2.gafert_steiner.ue2_locationprovider;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    // Makes a splash screen
    // States the date of development
    // And name of the application
    // A click on the screen starts the main application

    String[] DATA = {"01.05.2017","Location","Provider"};
    int TIME_BETWEEN_ANIMATIONS = 350;

    // Activity class to start when splash screen finishes
    Class MAIN_ACTIVITY = LocationActivity.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // All word will me children of the splashContainer Layout
        LinearLayout splashContainer = (LinearLayout)findViewById(R.id.splash_text_container);

        // The rootLayout is the parent of splash layout and only needed for the screen click
        // which starts the main activity
        LinearLayout rootLayout = (LinearLayout)findViewById(R.id.root);

        // For handling animation time
        Handler mHandler = new Handler();

        // This function assigns to every word of the DATA String Array a splashElement TextView
        // And adds a animation to it
        int waitFor = 0;
        for (String word: DATA) {
            final TextView splashElement = new TextView(this);

            // Make the first word smaller
            if(waitFor == 0){
                splashElement.setTextSize(20);
            } else {
                splashElement.setTextSize(50);
            }

            // Setup the appearance
            splashElement.setText(word);
            splashElement.setAlpha(0.0f);
            splashElement.setTextColor(Color.WHITE);

            // That the animation looks like it is coming
            // from somewhere we need to offset
            // it´s initial position
            splashElement.setTranslationX(-500f);

            // Add the Textview containing our word to the main layout
            splashContainer.addView(splashElement);

            // Wait for an specified amount and then animate
            // Wait time increases with each word
            // to have the same time between each animation
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    // Animates the x coordinate to position 0
                    splashElement.animate().translationX(0).setDuration(800);
                    // Animates the alpha channel to opaque
                    splashElement.animate().alpha(1f).setDuration(800);
                }
            }, waitFor);
            waitFor = waitFor + TIME_BETWEEN_ANIMATIONS;
        }

        // When the screen was clicked open the main Activity
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashScreen.this, MAIN_ACTIVITY);
                startActivity(intent);
                finish();
            }
        });

    }
}
