package com.example.mehdi.tp4;

import android.app.FragmentManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MainActivity extends AppCompatActivity {

    private GestureDetectorCompat mDetector;
    private int currentFragment = 1;
    // fragments
    private Fragment fragment_1 = null;
    private Fragment fragment_2 = null;
    private Fragment fragment_3 = null;
    private Fragment fragmentToLoad = null;
    private static final String DEBUG_TAG = "DEBUG";
    private String URL = null;
    // pages possibles
    private int premierePage = 1;
    private int deuxiemePage = 2;
    private int troisiemePage = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // creation du detecteur de glissement
        mDetector = new GestureDetectorCompat(this, new MyGestureDetector());

        // creation des fragments
        fragment_1 = new Frag1();
        fragment_2 = new Frag2();
        fragment_3 = new Frag3();

        // chargement du premier fragment au lancement de l'application
        fragmentToLoad = fragment_1;
        replaceFragment(fragmentToLoad);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // liaison de l'evenement avec le gestionnaire de glissement
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    // source : https://stackoverflow.com/questions/4098198/adding-fling-gesture-to-an-image-view-android
    // source : https://www.youtube.com/watch?v=Q5Ndr944U2o
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // Detection du glissement vers le haut
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(DEBUG_TAG, "onSwipe: up");
                    // Determination du prochain fragment a afficher
                    switch (currentFragment)
                    {
                        case 1 :
                            currentFragment = 2;
                            fragmentToLoad = fragment_2;
                            break;
                        case 2:
                            currentFragment = 3;
                            fragmentToLoad = fragment_3;
                            break;
                        case 3:
                            currentFragment = 1;
                            fragmentToLoad = fragment_1;
                            break;
                    }
                    // remplacement du fragment par le prochain fragment determine
                    replaceFragment(fragmentToLoad);
                }
            } catch (Exception e) {

            }
            return false;
        }
    }

    // source : https://developer.android.com/guide/topics/ui/controls/button.html
    public void onTest1ButtonPressed(View view)
    {
        // chargement du premier test
        setContentView(R.layout.activity_test);
        goToPage(premierePage);
    }

    public void onTest2ButtonPressed(View view)
    {
        // chargement du deuxieme test
        setContentView(R.layout.activity_test);
        goToPage(deuxiemePage);
    }

    //source : https://stackoverflow.com/questions/3913592/start-an-activity-with-a-parameter
    public void onTest3ButtonPressed(View view)
    {
        // chargement du troisieme test
        setContentView(R.layout.activity_test);
        goToPage(troisiemePage);
    }

    public void onBackButtonPressed(View view)
    {
        // retour vers l'activite principale
        setContentView(R.layout.activity_main);
        // determination du dernier fragment visite
        switch (currentFragment)
        {
            case 1:
                fragmentToLoad = new Frag1();
                break;
            case 2:
                fragmentToLoad = new Frag2();
                break;
            case 3:
                fragmentToLoad = new Frag3();
                break;
        }
        // chargement du dernier fragment visite
        replaceFragment(fragmentToLoad);
    }

    //source : https://stackoverflow.com/questions/21028786/how-do-i-open-a-new-fragment-from-another-fragment
    private void replaceFragment(Fragment fragment)
    {
        // changement du fragment courant par le fragment passe en parametre
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void goToPage(int testID)
    {
        // determination de l'URL a utiliser
        switch (testID)
        {
            case 1:
                URL = "http://192.168.1.150:8000";
                break;
            case 2:
                URL = "http://132.207.89.59/test2";
                break;
            case 3:
                URL = "http://132.207.89.59/test3";
                break;
        }
        // source : https://www.youtube.com/watch?v=TixBzK3fdFM

        // visite de l'URL a utiliser
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(URL);
    }
}