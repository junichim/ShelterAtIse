/*
 * Copyright 2017 Junichi MORI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mori_soft.escape;

import android.Manifest;
import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

import org.mapsforge.core.model.LatLong;

import static com.google.android.gms.location.LocationServices.getSettingsClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE_READ_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 2;
    private static final int GOOGLEPLAYSERVICE_ERROR_DIALOG_CODE = 1;
    private static final int GOOGLEPLAYSERVICE_LOCATION_REQUST_CHECK_SETTING = 2;

    private static final int SEC2MSEC = 1000; // 秒 -> ミリ秒 への変換単位
    private static final int LOCATION_INTERVAL = 10 * SEC2MSEC;         // ミリ秒単位
    private static final int FASTEST_LOCATION_INTERVAL = 5 * SEC2MSEC; // ミリ秒単位


    private boolean mIsLocationAvailable;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // アプリバーの設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getBaseContext());
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        // パーミッションチェック
        if (! PermissionUtil.checkPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionUtil.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE_READ_STORAGE);
        } else {
            checkGooglePlayService();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
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
                    checkLocationPermission();
                } else {
                    mIsLocationAvailable = false;
                }
                break;
            case GOOGLEPLAYSERVICE_LOCATION_REQUST_CHECK_SETTING:
                if (resultCode == RESULT_OK) {
                    // 継続的な位置情報の更新
                    startLocation();
                    mIsLocationAvailable = true;
                } else {
                    mIsLocationAvailable = false;
                }
                break;
            default:
                Log.w(TAG, "予期せぬActivityResultリクエストコード: " + requestCode);
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGooglePlayService();
                } else {
                    // TODO 地図が表示できないことを説明
                }
                break;
            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkCurrentLocationSettings();
                } else {
                    mIsLocationAvailable = false;
                    // TODO 現在位置を表示できないことを説明
                }
                break;
            default:
                Log.w(TAG, "予期せぬパーミッションリクエストコード: " + requestCode );
                finish();
                break;
        }
    }

    private void checkGooglePlayService() {
        final int res = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == res) {
            checkLocationPermission();
        } else {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(res)) {
                Log.w(TAG, "user resolvable error");
                GoogleApiAvailability.getInstance().getErrorDialog(this, res, GOOGLEPLAYSERVICE_ERROR_DIALOG_CODE, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mIsLocationAvailable = false;
                    }
                }).show();
            } else {
                Log.w(TAG, "not user resolvable error");
                mIsLocationAvailable = false;
            }
        }
    }

    private void checkLocationPermission() {
        if (! PermissionUtil.checkPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionUtil.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE_LOCATION);
        } else {
            checkCurrentLocationSettings();
        }
    }

    private void checkCurrentLocationSettings() {
        Log.d(TAG, "checkCurrentLocationSettings");

        mIsLocationAvailable = false;

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
                            resolvable.startResolutionForResult(MainActivity.this, GOOGLEPLAYSERVICE_LOCATION_REQUST_CHECK_SETTING);
                        } catch (IntentSender.SendIntentException excpt) {
                            Log.e(TAG, "exception occured: request setting change dialog", excpt);
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
                mIsLocationAvailable = true;
                startLocation();
            }
        });

    }

    private LocationRequest getLocationRequest() {
        LocationRequest req = new LocationRequest();
        req.setInterval(LOCATION_INTERVAL);
        req.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return req;
    }

    @SuppressWarnings("MissingPermission")
    private void startLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d(TAG, "getLastLocation: success");

                MapFragment f = (MapFragment) MainActivity.this.getFragmentManager().findFragmentById(R.id.fragment_map);
                f.updateLastLocation(new LatLong(location.getLatitude(), location.getLongitude()));

                startLocationUpdate();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "getLastLocation: failed");
                startLocationUpdate();
            }
        });
    }
    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate() {
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
    }

    private LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            MapFragment f = (MapFragment) MainActivity.this.getFragmentManager().findFragmentById(R.id.fragment_map);
            f.updateCurrentLocation(new LatLong(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
        }
    };

}
