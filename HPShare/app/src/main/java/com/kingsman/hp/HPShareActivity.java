package com.kingsman.hp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kingsman.hp.utils.PermissionUtils;
import com.kingsman.hp.utils.SharePreferenceSettings;

public class HPShareActivity extends AppCompatActivity implements ConsumerFragment.OnComsumerFragmentInteractionListener {

    private static final String TAG = HPShareActivity.class.getName();

    private static final String PROVIDER_FRAGMENT_TAG = "privider_fragment";
    private static final String COMSUMER_FRAGMENT_TAG = "comsumer_fragment";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 2;
    private static final int NAVIGATION_PROVIDER = 0;
    private static final int NAVIGATION_COMSUMER = 1;

    private Fragment fragment;
    private FragmentManager fragmentManager;


    private SharePreferenceSettings mSharePreferenceSettings;
    private int mNavigationType;
    private String mFragmentTag;

    private boolean mPermissionDenied = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_provider:
                    fragment = new ProviderFragment();
                    mFragmentTag = PROVIDER_FRAGMENT_TAG;
                    mNavigationType = NAVIGATION_PROVIDER;
                    break;
                case R.id.navigation_comsumer:
                    fragment = new ConsumerFragment();
                    mFragmentTag = PROVIDER_FRAGMENT_TAG;
                    mNavigationType = NAVIGATION_COMSUMER;
                    break;
            }

            mSharePreferenceSettings.put(SharePreferenceSettings.Key.NAVIGATION_TYPE, mNavigationType);

            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content_container, fragment, mFragmentTag).commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hs_layout);

        checkPermission();

        mSharePreferenceSettings = SharePreferenceSettings.getInstance(getApplicationContext());
        mNavigationType = mSharePreferenceSettings.getInt(SharePreferenceSettings.Key.NAVIGATION_TYPE, 0);

        fragmentManager = getSupportFragmentManager();
        if (mNavigationType == NAVIGATION_PROVIDER) {
            fragment = new ProviderFragment();
            mFragmentTag = PROVIDER_FRAGMENT_TAG;
        } else {
            fragment = new ConsumerFragment();
            mFragmentTag = PROVIDER_FRAGMENT_TAG;
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content_container, fragment, mFragmentTag).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

//        if(!GoogleApiAvailability.isGooglePlayServicesAvailable(this)) {
//            GoogleApiAvailability.makeGooglePlayServicesAvailable();
//        }

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(false).show(getSupportFragmentManager(), "dialog");
            mPermissionDenied = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onComsumerFragmentInteraction(Uri uri) {
        Log.d(TAG, uri == null ? null : uri.toString());
    }

    private void checkPermission() {

        requestCoarseLocationPermission(MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                Toast.makeText(this, "onCreate: Already Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "onCreate: Not Granted. Permission Requested", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }


    }

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */
    public void requestCoarseLocationPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                PermissionUtils.RationaleDialog.newInstance(requestCode, Manifest.permission.ACCESS_COARSE_LOCATION, false)
                        .show(getSupportFragmentManager(), "dialog");
            } else {
                PermissionUtils.requestPermission(this, requestCode, Manifest.permission.ACCESS_COARSE_LOCATION, false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            // Enable the My Location button if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                mPermissionDenied = true;
            }

        }
    }


}
