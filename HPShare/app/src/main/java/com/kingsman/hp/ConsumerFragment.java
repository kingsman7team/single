package com.kingsman.hp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.kingsman.hp.ap.AccessPoint;
import com.kingsman.hp.ap.WifiApManager;
import com.kingsman.hp.wifi.Scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConsumerFragment.OnComsumerFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConsumerFragment#newInstance} factory method to
 * create an instance of this fragment by Haemdam
 */
public class ConsumerFragment extends Fragment implements Switch.OnCheckedChangeListener{
    private static final String TAG = ConsumerFragment.class.getName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnComsumerFragmentInteractionListener mListener;
    private Switch mWifiSwitch;
    private Scanner mScanner;
    private WifiManager mWifiManager;
    private WifiApManager mWifiApManager;

    private final Receiver mReceiver = new Receiver();

    protected RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private LinearLayoutManager mLayoutManager;

    public ConsumerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConsumerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConsumerFragment newInstance(String param1, String param2) {
        ConsumerFragment fragment = new ConsumerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (mWifiManager == null)
            mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (mWifiApManager == null) {
            mWifiApManager = new WifiApManager(getContext());
        }

        if (mScanner == null) {
            mScanner = new Scanner(getContext());
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_consumer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerViewAdapter = new RecyclerViewAdapter(getContext());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mWifiSwitch = (Switch) view.findViewById(R.id.switch_bar);
        mWifiSwitch.setOnCheckedChangeListener(this);

        updateWifiState();

        rebuildUI();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onComsumerFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnComsumerFragmentInteractionListener) {
            mListener = (OnComsumerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnComsumerFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mReceiver != null) {
            mReceiver.register(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            mReceiver.register(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.consummer_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_map:
                Toast.makeText(getContext(), "select", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClassName(getContext(), UserMapActivity.class.getName());
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (isAirplaneModeOn(getContext())) {
                Toast.makeText(getContext(), R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
                buttonView.setChecked(false);
                return;
            }

            int wifiApState = mWifiApManager.getWifiApState();
            if (wifiApState == WifiApManager.WIFI_AP_STATE_ENABLING || wifiApState == WifiApManager.WIFI_AP_STATE_ENABLED) {
                mWifiApManager.setWifiApEnabled(null, false);
            }

            mWifiSwitch.setEnabled(false);
            if (!mWifiManager.setWifiEnabled(isChecked)) {
                mWifiSwitch.setEnabled(true);
                Toast.makeText(getContext(), R.string.wifi_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            mWifiSwitch.setEnabled(false);
            if (!mWifiManager.setWifiEnabled(isChecked)) {
                mWifiSwitch.setEnabled(true);
                Toast.makeText(getContext(), R.string.wifi_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        public void register(boolean register) {
            if (mRegistered == register) return;
            if (register) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
                filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.RSSI_CHANGED_ACTION);

                getActivity().registerReceiver(this, filter);
            } else {
                getActivity().unregisterReceiver(this);
            }
            mRegistered = register;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            handleEvent(context, intent);
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState();
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
                /*|| WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)
                || WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)*/) {
            //updateAccessPoints();
            final Collection<AccessPoint> accessPoints = constructAccessPoints();
            if (accessPoints.size() == 0) {
                    /* TODO
                    *
                    * Show empty UI to user if accessPoint is not exist.
                    *
                    * */
            } else {
                mRecyclerViewAdapter.updateItems((List<AccessPoint>)accessPoints);
            }

            /*for (AccessPoint accessPoint : accessPoints) {

            }*/
        } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateWifiState();
        }
    }

    public void updateWifiState() {

        int state = mWifiManager.getWifiState();

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mWifiSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchChecked(true);
                mWifiSwitch.setEnabled(true);
                mScanner.resume();
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mWifiSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchChecked(false);
                mWifiSwitch.setEnabled(true);
                break;
            default:
                setSwitchChecked(false);
                mWifiSwitch.setEnabled(true);
                break;
        }
    }

    private void setSwitchChecked(boolean checked) {
        if (mWifiSwitch != null && checked != mWifiSwitch.isChecked()) {
            mWifiSwitch.setChecked(checked);
        }
    }

    /** Returns sorted list of access points */
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(getContext(), config);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result)) {
                        found = true;
                    }
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(getContext(), result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }

        //Collections.sort(accessPoints);
        return accessPoints;
    }


    private class Multimap<K,V> {
        private final HashMap<K,List<V>> store = new HashMap<K,List<V>>();
        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    /**
     * Get the security type of the wireless network
     *
     * @param scanResult the wifi scan result
     * @return one of WEP, PSK of OPEN
     */
    private String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {WEP, PSK};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return OPEN;
    }


    private void rebuildUI() {
        if (!isAdded()) {
            Log.w(TAG, "Cannot build the DashboardSummary UI yet as the Fragment is not added");
            return;
        }

        new APLoader().execute();
    }

    private class APLoader extends AsyncTask<Void, Void, List<AccessPoint>> {

        @Override
        protected List<AccessPoint>  doInBackground(Void... params) {
            return constructAccessPoints();
        }

        @Override
        protected void onPostExecute(List<AccessPoint> accessPoints) {
            final Activity activity = getActivity();
            if (!isAdded()) {
                return;
            }

            mRecyclerViewAdapter.updateItems(accessPoints);
        }
    }


        /**
         * This interface must be implemented by activities that contain this
         * fragment to allow an interaction in this fragment to be communicated
         * to the activity and potentially other fragments contained in that
         * activity.
         * <p>
         * See the Android Training lesson <a href=
         * "http://developer.android.com/training/basics/fragments/communicating.html"
         * >Communicating with Other Fragments</a> for more information.
         */
    public interface OnComsumerFragmentInteractionListener {
        // TODO: Update argument type and name
        void onComsumerFragmentInteraction(Uri uri);
    }
}
