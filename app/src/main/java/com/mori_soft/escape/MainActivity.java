package com.mori_soft.escape;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;

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
import com.graphhopper.GraphHopper;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.Date;

import static com.google.android.gms.location.LocationServices.getSettingsClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE_READ_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 2;
    private static final int GOOGLEPLAYSERVICE_ERROR_DIALOG_CODE = 1;
    private static final int GOOGLEPLAYSERVICE_LOCATION_REQUST_CHECK_SETTING = 2;

    private final static String MAP_FILE = "japan_multi.map";
    //    private final static String MAP_FILE = "ise_shima.map";

    private MapView mMapView;
    private GraphHopper mGraphHopper;

    private boolean mIsLocationAvailable;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private Marker mCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidGraphicFactory.createInstance(this.getApplication());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setTitle(R.string.toolbar_title);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitle(R.string.toolbar_subtitle);
        toolbar.setSubtitleTextColor(Color.LTGRAY);
        setSupportActionBar(toolbar);

        mMapView = (MapView) findViewById(R.id.mapView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getBaseContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE_READ_STORAGE);
                return;
            }
        }
        onGrantedMapDraw();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onDestroy() {
        mMapView.destroyAll();;
        AndroidGraphicFactory.clearResourceMemoryCache();

        super.onDestroy();
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
                    startLocationUpdate();
                    mIsLocationAvailable = true;
                } else {
                    mIsLocationAvailable = false;
                }
                break;
            default:
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
                    onGrantedMapDraw();
                    return;
                }
            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mIsLocationAvailable = true;
                    setupLocation();
                    return;
                }
                mIsLocationAvailable = false;
            default:
        }

        // アプリを終了
        this.finish();
    }

    private void onGrantedMapDraw() {
        // マップ
        setMapView();
        displayMap();

        // 経路探索準備
        prepareGraphHopper();

        // 現在位置表示のための処理
        checkGooglePlayService();
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
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE_LOCATION);
                return;
            }
        }
        setupLocation();
    }

    private void requestPermission(String permission, int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            // TODO 説明ダイアログの表示と応答
        }
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
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
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "failed getlastLocation", e);
                // 設定の確認
                checkCurrentSettings();
            }
        });
    }

    private void checkCurrentSettings() {

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
                mIsLocationAvailable = true;
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

            mCurrentLocation = locationResult.getLastLocation();
            updateCurrentLocation();
            Log.d(TAG, "location: " + DateFormat.format("yyyy/MM/dd kk:mm:ss", new Date(mCurrentLocation.getTime())) + ", lat lon : " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        }
    };


    private void setMapView() {
        mMapView.setClickable(true);
        mMapView.getMapScaleBar().setVisible(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setZoomLevelMin((byte)10);
        mMapView.setZoomLevelMax((byte)20);
    }

    private void displayMap() {
        TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache", mMapView.getModel().displayModel.getTileSize(), 1f, mMapView.getModel().frameBufferModel.getOverdrawFactor() );

        File file = new File(Environment.getExternalStorageDirectory() + "/Download/", MAP_FILE);
        if (! file.exists()) {
            Log.e(TAG, "file not found: " + file.getAbsolutePath());
            finish();
            return;
        }

        MapDataStore mds = new MapFile(file);
        TileRendererLayer trl = new TileRendererLayer(tileCache, mds, mMapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        trl.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        mMapView.getLayerManager().getLayers().add(trl);

        mMapView.setCenter(new LatLong(34.491297, 136.709685)); // 伊勢市駅
        mMapView.setZoomLevel((byte)12);
    }


    private void updateCurrentLocation() {
        if (mIsLocationAvailable) {
            if (mCurrent != null) {
                mMapView.getLayerManager().getLayers().remove(mCurrent);
            }
            mCurrent = createMarker(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), R.drawable.marker_red);
            mMapView.getLayerManager().getLayers().add(mCurrent);
        }
    }

    private Marker createMarker(LatLong latlong, int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(latlong, bitmap, 0, -bitmap.getHeight() / 2);
    }


    private void prepareGraphHopper() {

        AsyncTask<Void, Void, GraphHopper> task = new AsyncTask<Void, Void, GraphHopper>() {
            private boolean mHasError = false;

            @Override
            protected GraphHopper doInBackground(Void... params) {
                try {
                    GraphHopper tmp = new GraphHopper().forMobile();
                    tmp.load(getGraphHopperFolder());
                    return tmp;
                } catch (Exception e) {
                    Log.e(TAG, "Exception occured" , e);
                    mHasError = true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(GraphHopper graphHopper) {
                super.onPostExecute(graphHopper);
                if (mHasError) {
                    // TODO エラー処理
                } else {
                    mGraphHopper = graphHopper;
                }
                // TODO もし何かするなら
            }
        }.execute();
    }

    private String getGraphHopperFolder() {
        // TODO 暫定
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/com_mori_soft_escape/gh").getAbsolutePath();
    }

}
