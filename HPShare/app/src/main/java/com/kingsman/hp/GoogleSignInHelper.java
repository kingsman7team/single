package com.kingsman.hp;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by T420_Note2 on 2017-06-04.
 */

public class GoogleSignInHelper {
    private static GoogleSignInHelper mGoogleSignInHelper;
    private static GoogleApiClient mGoogleApiClient;

    private GoogleSignInHelper(){};

    public static GoogleSignInHelper getInstance(){
        if(mGoogleSignInHelper == null){
            mGoogleSignInHelper = new GoogleSignInHelper();
        }
        return mGoogleSignInHelper;
    }

    public void setGoogleApiClient(GoogleApiClient a){
        mGoogleApiClient = a;
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }
}
