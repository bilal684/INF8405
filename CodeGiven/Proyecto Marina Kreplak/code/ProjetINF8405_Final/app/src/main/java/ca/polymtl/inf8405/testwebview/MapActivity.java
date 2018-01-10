package ca.polymtl.inf8405.testwebview;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.Map;

import static java.lang.Double.valueOf;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private TextView userPseudo;
    private ImageView imageUser;
    private TextView robotPseudo;
    private ImageView imageRobot;
    private Utilisateur utilisateur;
    private FireBaseManager fireBaseManager;
    private DatabaseHelper myDB;
    private Marker [] mPerth;
    private Marker [] mRobot;
    private int compteurPerth=0;
    private int compteurRobot=0;

    private Map<String, Object> allUtilisateurs;
    private Map<String, Object> allRobots;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        myDB = new DatabaseHelper(this);

        fireBaseManager = new FireBaseManager(null);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        userPseudo = (TextView) findViewById(R.id.textView4);
        imageUser = (ImageView) findViewById(R.id.imageView);

        robotPseudo = (TextView) findViewById(R.id.textView5);
        imageRobot = (ImageView) findViewById(R.id.imageView1);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        utilisateur = (Utilisateur) extras.get("UTILISATEUR");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng lastPos =  new LatLng(0,0);
        LatLng robotPos =  new LatLng(0,0);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(true);
        googleMap.setMyLocationEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                if((int) marker.getTag() == 1) {
                    Robot robot = (Robot) allRobots.get(marker.getTitle());
                    Intent intent = new Intent(MapActivity.this, ModeActivity.class);
                    intent.putExtra("ROBOT", robot);
                    intent.putExtra("UTILISATEUR", utilisateur);
                    startActivity(intent);
                    marker.setPosition(new LatLng(valueOf(robot.getLocalisation().getLatitude()), valueOf(robot.getLocalisation().getLongitude())));
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Robot robot = (Robot) allRobots.get(marker.getTitle());
                marker.setPosition(new LatLng(valueOf(robot.getLocalisation().getLatitude()), valueOf(robot.getLocalisation().getLongitude())));
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LatLng posme = new LatLng(valueOf(utilisateur.getLocalisation().getLatitude()), valueOf(utilisateur.getLocalisation().getLongitude()));
        Marker f = googleMap.addMarker(new MarkerOptions()
                .position(posme)
                .title(utilisateur.getPseudo()));
        f.setTag(0);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posme, 10));
        googleMap.setOnMarkerClickListener(this);
        allRobots = fireBaseManager.getAllRobotsOnFirebase();
        allUtilisateurs = fireBaseManager.getAllUtilisateursOnFirebase();
        mPerth = new Marker[allUtilisateurs.size()];

        for(Map.Entry<String, Object> entree : allUtilisateurs.entrySet()) {
            Utilisateur utilisateur456 = (Utilisateur) entree.getValue();
            if (!utilisateur456.getPseudo().equals(utilisateur.getPseudo())) {
                lastPos = new LatLng(valueOf(utilisateur456.getLocalisation().getLatitude()), valueOf(utilisateur456.getLocalisation().getLongitude()));
                mPerth[compteurPerth] = googleMap.addMarker(new MarkerOptions()
                        .position(lastPos)
                        .title(utilisateur456.getPseudo())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                ;
                mPerth[compteurPerth].setTag(0);
                compteurPerth++;
            }
        }

        mRobot = new Marker[allRobots.size()];
        for(Map.Entry<String, Object> entree : allRobots.entrySet()) {
            Robot robot456 = (Robot)entree.getValue();
            robotPos = new LatLng(valueOf(robot456.getLocalisation().getLatitude()), valueOf(robot456.getLocalisation().getLongitude()));
            mRobot[compteurRobot] = googleMap.addMarker(new MarkerOptions()
                    .position(robotPos)
                    .title(robot456.getNom())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            ;
            mRobot[compteurRobot].setTag(1);
            mRobot[compteurRobot].setDraggable(true);
            compteurRobot++;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if((int) marker.getTag() == 0){
            Utilisateur user = (Utilisateur) allUtilisateurs.get(marker.getTitle());
            userPseudo.setText(user.getPseudo());
            Cursor res = myDB.getAllData();
            byte[] byteArray = null;
            while (res != null && res.moveToNext()) {
                if (res.getString(2).equals(user.getPseudo())) {
                    byteArray = res.getBlob(4);

                }
            }
            if (byteArray != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageUser.setImageBitmap(bitmap);
            }
            else
                imageUser.setImageBitmap(BitmapFactory.decodeFile(fireBaseManager.getUtilisateurPhoto(user).getAbsolutePath()));
        }
       if((int) marker.getTag() == 1) {
            Robot robot = (Robot) allRobots.get(marker.getTitle());
            robotPseudo.setText(robot.getNom());
            imageRobot.setImageBitmap(BitmapFactory.decodeFile(fireBaseManager.getRobotPhoto(robot).getAbsolutePath()));
        }

        return true;
    }


}
