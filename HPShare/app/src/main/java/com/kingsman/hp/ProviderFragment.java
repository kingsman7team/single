package com.kingsman.hp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.kingsman.hp.service.DataCheckingService;
import com.kingsman.hp.utils.SharePreferenceSettings;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnProviderFragmentInteractionListener}
 * interface.
 */
public class ProviderFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String SHARED_PREFERENCE_NAME = "preference";
    public static final String SP_SpinnerIndex_term = "index_term";
    public static final String SP_SpinnerIndex_data = "index_data";
    public static final String SP_AOMUNT_offeredData = "offer_data";
    //private final String SP_AOMUNT_remainData = "remain_data";
    public static final String SP_AOMUNT_offeredTotalData = "total_data";
    public static final String SP_AOMUNT_limitData = "limit_data";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private int mSpinnerIndex_term;
    private int mSpinnerIndex_data;
    private long offeredDataAmount;
    private long remainDataAmount;
    private long offeredTotalDataAmount;
    private long mLimitAmount;

    private TextView offeredData;
    private TextView remainData;
    private TextView offeredTotalData;
    private Button btn_ApSetting;
    private Button btn_Start;
    private Button btn_Stop;

    //private SharedPreferences mPref;
    private SharePreferenceSettings mPref;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProviderFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProviderFragment newInstance(int columnCount) {
        ProviderFragment fragment = new ProviderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onResume() {
        startServiceMethod();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DataCheckingService.INTENT_HPSHARE_DATA_USAGE);
        getActivity().registerReceiver(mReceiver, filter);

        updateDataUsageInfo();
        updateUI();

        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);

        try {
            if (mConnection != null)
                getActivity().unbindService(mConnection);
        }
        catch (IllegalArgumentException e){ // Service not registered
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider, container, false);
    }

    protected static final int SPINNER_RID_TERM = R.id.spinner_select_date;
    protected static final int SPINNER_RID_DATA = R.id.spinner_data_per_term;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getInfoData();

        Spinner spinnerDate = (Spinner) view.findViewById(SPINNER_RID_TERM);
        spinnerDate.setSelection(mSpinnerIndex_term);
        spinnerDate.setOnItemSelectedListener(this);

        Spinner spinnerLimit = (Spinner) view.findViewById(SPINNER_RID_DATA);
        spinnerLimit.setSelection(mSpinnerIndex_data);
        spinnerLimit.setOnItemSelectedListener(this);

        offeredData = (TextView)view.findViewById(R.id.provider_offered_data_amount);
        remainData = (TextView)view.findViewById(R.id.provider_remain_data_amount);
        offeredTotalData = (TextView)view.findViewById(R.id.provider_total_offered_amount);

        btn_Start = (Button)view.findViewById(R.id.provider_start);
        btn_Start.setOnClickListener(this);
        btn_Stop = (Button)view.findViewById(R.id.provider_stop);
        btn_Stop.setOnClickListener(this);

        //updateUI();
    }

    private void getInfoData(){
        mPref = SharePreferenceSettings.getInstance();//getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        mSpinnerIndex_term = mPref.getInt(SP_SpinnerIndex_term, 0);
        Log.d("aa", "mSpinnerIndex_term : " + mSpinnerIndex_term);
        if(mSpinnerIndex_term >= getResources().getStringArray(R.array.select_date).length){
            mSpinnerIndex_term = 0;
        }
        mSpinnerIndex_data = mPref.getInt(SP_SpinnerIndex_data, 0);
        Log.d("aa", "mSpinnerIndex_data : " + mSpinnerIndex_data);
        if(mSpinnerIndex_data >= getResources().getStringArray(R.array.data_per_term).length){
            mSpinnerIndex_term = 0;
        }
        mLimitAmount = getLimitAmount();

        // need to change to get info from server
        offeredDataAmount = mPref.getLong(SP_AOMUNT_offeredData, 0L);
        Log.d("aa", "offeredDataAmount : " + offeredDataAmount);
        //remainDataAmount = mPref.getInt(SP_AOMUNT_remainData, 0);
        offeredTotalDataAmount = mPref.getLong(SP_AOMUNT_offeredTotalData, 0L);
        Log.d("aa", "offeredTotalDataAmount : " + offeredTotalDataAmount);

        if(mLimitAmount <= offeredDataAmount){
            remainDataAmount = 0L;
        }
        else {
            remainDataAmount = mLimitAmount - offeredDataAmount;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("aa", "parent : " + parent.toString() + ", view : " + view.toString() + ", pos : " + position  + ", id : " + id);
/*        Log.d("aa", "parent id : " + parent.getId());
        Log.d("aa", "view id : " + view.getId());
        Log.d("aa", "SPINNER_RID_TERM id : " + SPINNER_RID_TERM);
        Log.d("aa", "SPINNER_RID_DATA id : " + SPINNER_RID_DATA);*/
        if(parent.getId() == SPINNER_RID_TERM){
            mSpinnerIndex_term = position;
            if(mService != null){
                mService.setTermType(mSpinnerIndex_term);
            }
        }
        else if(parent.getId() == SPINNER_RID_DATA){
            mSpinnerIndex_data = position;
            if(mService != null){
                mService.setLimitValue(getLimitAmount());
            }
        }
        else {
            return;
        }
        // send info to server
        // update screen info
        updateDataUsageInfo();
        savePreferenceInfo();
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("aa", "Nothing!");
    }

    private long getLimitAmount(){
        long limitAmount;
        try {
            limitAmount =
                    Integer.parseInt(getResources().getStringArray(R.array.data_per_term)[mSpinnerIndex_data])
                            * 1024L * 1024L; // bytes
        }
        catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            limitAmount = 0L;
        }

        return limitAmount;
    }

    private void updateDataUsageInfo() {
        if(mService != null) {
            offeredDataAmount = mService.getCurTotalValue();
            offeredTotalDataAmount = mService.getTotalOfferedValue();
        }

        mLimitAmount = getLimitAmount();
        if (mLimitAmount <= offeredDataAmount) {
            remainDataAmount = 0L;
            //stopOfferData();
        } else
            remainDataAmount = mLimitAmount - offeredDataAmount;
    }

    private void savePreferenceInfo(){
        mPref = SharePreferenceSettings.getInstance();
        //mPref.put(SP_AOMUNT_offeredData, offeredDataAmount);
        //mPref.put(SP_AOMUNT_offeredTotalData, offeredTotalDataAmount);
        mPref.put(SP_SpinnerIndex_term, mSpinnerIndex_term);
        mPref.put(SP_SpinnerIndex_data, mSpinnerIndex_data);
        mPref.put(SP_AOMUNT_limitData, mCallback.getLimitValue() * 1024L * 1024L);
    }

    private void updateUI(){
        offeredData.setText(Formatter.formatFileSize(getContext(), offeredDataAmount));
        remainData.setText(Formatter.formatFileSize(getContext(), remainDataAmount));
        offeredTotalData.setText(Formatter.formatFileSize(getContext(), offeredTotalDataAmount));
    }

//    private long getCurOfferedData(){
//        return 0L;
//    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.provider_start){
            //startServiceMethod();
            Intent intent = new Intent(getActivity(), DataCheckingService.class);
            getActivity().startService(intent);
        }
        else if(v.getId() == R.id.provider_stop){
            if(mService != null)
                mService.stopChcecking();
            if(mConnection != null)
                getActivity().unbindService(mConnection);
            Intent intent = new Intent(getActivity(), DataCheckingService.class);
            getActivity().stopService(intent);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnProviderFragmentInteractionListener {
        // TODO: Update argument type and name
        void onProviderListFragmentInteraction();
    }


    // service
    private DataCheckingService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataCheckingService.DataServiceBinder binder = (DataCheckingService.DataServiceBinder) service;
            mService = binder.getService();
            Log.d("aaa","onServiceConnected registerCallback");
            mService.registerCallback(mCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private DataCheckingService.ICallback mCallback = new DataCheckingService.ICallback() {
        @Override
        public void updateCurUsage(long curUsage) {
            Log.d("aa", "updateCurUsage curUsage : " + curUsage);
            //Toast.makeText(getContext(), "" + Formatter.formatFileSize(getContext(), curUsage), Toast.LENGTH_SHORT).show();
            offeredTotalDataAmount += curUsage;//(curTotalUsage - offeredDataAmount);
            offeredDataAmount += curUsage;
            remainDataAmount = getLimitAmount() - offeredDataAmount;
            updateUI();
        }

        @Override
        public int getTermType() {
            return mSpinnerIndex_term;
        }

        @Override
        public int getLimitValue() {
            return Integer.parseInt(getResources().getStringArray(R.array.data_per_term)[mSpinnerIndex_data]);
        }

        @Override
        public int getOldOfferedValue() {
            return (int)offeredDataAmount;
        }


    };

    private void startServiceMethod() {
        Intent service = new Intent(getActivity(), DataCheckingService.class);
        getActivity().bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(DataCheckingService.INTENT_HPSHARE_DATA_USAGE)){
                updateDataUsageInfo();
                updateUI();
            }
        }
    };
}
