package com.kingsman.hs;

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

import com.kingsman.hs.data.SharePreferenceSettings;

public class HSActivity extends AppCompatActivity implements ComsumerFragment.OnComsumerFragmentInteractionListener {

    private static final String TAG = HSActivity.class.getName();

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
                    fragment = new ComsumerFragment();
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
            fragment = new ComsumerFragment();
            mFragmentTag = PROVIDER_FRAGMENT_TAG;
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content_container, fragment, mFragmentTag).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onResume() {
        super.onResume();


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

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }








}
