package com.example.locationapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int PERMISSIONS_READ_FINE_LOCATION = 100;
    private TextView txtListProviders;
    private TextView txtBestProvider;
    private TextView txtConnectionStatus;
    private TextView txtLocationRequest;
    private LocationManager locationManager;
    //Minimo tiempo para updates en Milisegundos
    //private static final long MIN_TIEMPO_ENTRE_UPDATES = 1000 * 60 * 1; // 1 minuto
    private static final long MIN_TIEMPO_ENTRE_UPDATES = 1000 * 30; // 30 segundos
    //Minima distancia para updates en metros.
    private static final float MIN_CAMBIO_DISTANCIA_PARA_UPDATES = 2; // 2 metros

    private TextView txtResumen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtListProviders = findViewById(R.id.txtListProviders);
        txtBestProvider = findViewById(R.id.txtBestProvider);
        txtLocationRequest=findViewById(R.id.txtLocationRequest);
        txtResumen = findViewById(R.id.tvResumen);

        //Se solicita los permisos
        requestPermissions();

        //Se obtiene el servicio de localización
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Se obtiene el mejor provedor en base a los criterios que se han establecido
        txtBestProvider.setText(getBestProviderName());

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();

        // Esta verificación debe realizarse durante onStart () porque el sistema llama
        // este método cuando el usuario regresa a la actividad, lo que asegura el deseado
        // El proveedor de ubicación se habilita cada vez que la actividad se reanuda desde el estado detenido.

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // Cree un diálogo de alerta aquí que solicite que el usuario habilite
            // los servicios de ubicación, luego, cuando el usuario hace clic en el botón "Aceptar",
            enableLocationSettings();
        } else if (checkPermissions())
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, this);
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void showProviders() {
        //Se obtiene la lista de proveedores
        String name;
        int accuracy = 0;
        boolean supportsAltitude = false;
        int requirement = 0;
        List listProvider = locationManager.getAllProviders();
        for (int i = 0; i < listProvider.size(); i++) {
            LocationProvider provider = locationManager.getProvider((String) listProvider.get(i));
            name = provider.getName();
            accuracy = provider.getAccuracy();
            supportsAltitude = provider.supportsAltitude();
            requirement = provider.getPowerRequirement();
            txtListProviders.append("Nombre-> " + name + " Precisión ->" + accuracy + " Altitud->" + supportsAltitude + " Consumo:-> " + requirement + "\n");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_READ_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    showProviders();

                } else {

                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // Comprobar otros tipos de permisos que se han solicitado
        }//switch
    }

    private boolean checkPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_READ_FINE_LOCATION);
    }

    public String getBestProviderName() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        return locationManager.getBestProvider(criteria, true);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TAG, "Location Request :" + location.getLatitude() + "," + location.getLongitude());
            String localizacion = location.getLatitude() + "," + location.getLongitude();
            txtLocationRequest.setText(localizacion);
            txtResumen.append(localizacion + "\n" );
        }

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.i(TAG, "onProviderEnabled");
        txtConnectionStatus.setText("Proveedor Status : Enabled");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.i(TAG, "onProviderDisabled");
        txtConnectionStatus.setText("Proveedor Status : Disabled");
    }

}