package com.mori_soft.escape;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getSettingsClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int GOOGLEPLAYSERVICE_ERROR_DIALOG_CODE = 1;
    private static final int REQUST_CHECK_SETTING = 2;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getBaseContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        final int res = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == res) {
            startLocation();
        } else {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(res)) {
                Log.w(TAG, "user resolvable error");
                GoogleApiAvailability.getInstance().getErrorDialog(this, res, GOOGLEPLAYSERVICE_ERROR_DIALOG_CODE, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();
            } else {
                Log.w(TAG, "not user resolvable error");
                finish();
            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GOOGLEPLAYSERVICE_ERROR_DIALOG_CODE:
                if (resultCode == RESULT_OK) {
                    startLocation();
                } else {
                    finish();
                }
                break;
            case REQUST_CHECK_SETTING:
                if (resultCode == RESULT_OK) {
                    // 継続的な位置情報の更新
                    startLocationUpdate();
                } else {
                    finish();
                }
                break;
            default:
                finish();
                break;
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            setupLocation();
        }
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            requestLocationPermission();
        } else {
            setupLocation();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // TODO 説明ダイアログの表示と応答
            // 以下は、暫定
            Toast toast = Toast.makeText(this, "位置情報が必要です", Toast.LENGTH_LONG);
            toast.show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocation();
                    return;
                }
            default:
        }

        // アプリを終了
        this.finish();
    }

    @SuppressWarnings("MissingPermission")
    private void setupLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "lat, lon: " + location.getLatitude() + ", " + location.getLongitude());
                } else {
                    Log.d(TAG, "location is null");
                }
                // 設定の確認
                checkCurrentSettings();
            }
        });
    }

    private void checkCurrentSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(getLocationRequest());


        SettingsClient sc = LocationServices.getSettingsClient(this);
        sc.checkLocationSettings(builder.build()).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                final int statusCode = ((ApiException)e).getStatusCode();

                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this, REQUST_CHECK_SETTING);
                        } catch (IntentSender.SendIntentException excpt) {
                            Log.e(TAG, "exception occured: ", excpt);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.w(TAG, "status is: settings change inavailable");
                        break;
                    default:
                        Log.w(TAG, "status is: " + statusCode);
                        break;
                }
            }
        }).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdate();
            }
        });

    }

    private LocationRequest getLocationRequest() {
        LocationRequest req = new LocationRequest();
        req.setInterval(10000);
        req.setFastestInterval(5000);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return req;
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate() {
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
        Log.d(TAG, "mLocationCallback: " + mLocationCallback.toString());
    }

    private LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location loc = locationResult.getLastLocation();
            Log.d(TAG, "location: " + DateFormat.format("yyyy/MM/dd kk:mm:ss", new Date(loc.getTime())) + ", lat lon : " + loc.getLatitude() + ", " + loc.getLongitude());
        }
    };

}
