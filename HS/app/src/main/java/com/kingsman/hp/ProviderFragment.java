package com.kingsman.hp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnProviderFragmentInteractionListener}
 * interface.
 */
public class ProviderFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String SHARED_PREFERENCE_NAME = "preference";
    private final String SP_SpinnerIndex_term = "index_term";
    private final String SP_SpinnerIndex_data = "index_data";
    private final String SP_AOMUNT_offeredData = "offer_data";
    //private final String SP_AOMUNT_remainData = "remain_data";
    private final String SP_AOMUNT_offeredTotalData = "total_data";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private int mSpinnerIndex_term;
    private int mSpinnerIndex_data;
    private long offeredDataAmount;
    private long remainDataAmount;
    private long offeredTotalDataAmount;

    private TextView offeredData;
    private TextView remainData;
    private TextView offeredTotalData;

    private SharedPreferences mPref;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider, container, false);
/*        View view = inflater.inflate(R.layout.provider_fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new RecyclerViewAdapter(getContext()));
        }
        return view;*/
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

        updateUI();
    }

    private void getInfoData(){
        mPref = getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
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

        // need to change to get info from server
        offeredDataAmount = mPref.getLong(SP_AOMUNT_offeredData, 0L);
        //remainDataAmount = mPref.getInt(SP_AOMUNT_remainData, 0);
        offeredTotalDataAmount = mPref.getLong(SP_AOMUNT_offeredTotalData, 0L);
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
        }
        else if(parent.getId() == SPINNER_RID_DATA){
            mSpinnerIndex_data = position;
        }
        else {
            return;
        }
        // send info to server
        // update screen info
        updateDataUsageInfo();
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("aa", "Nothing!");
    }

    private void updateDataUsageInfo(){
        //Log.d("aa", "updateDataUsageInfo mSpinnerIndex_data : " + mSpinnerIndex_data);

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

        offeredDataAmount = getCurOfferedData(); // need to change
        offeredTotalDataAmount += offeredDataAmount; // need to change

        if(limitAmount <= offeredDataAmount){
            remainDataAmount = 0L;
            //stopOfferData();
        }
        else
            remainDataAmount = limitAmount - offeredDataAmount;

        mPref = getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putLong(SP_AOMUNT_offeredData, offeredDataAmount);
        editor.putLong(SP_AOMUNT_offeredTotalData, offeredTotalDataAmount);
        editor.putInt(SP_SpinnerIndex_term, mSpinnerIndex_term);
        editor.putInt(SP_SpinnerIndex_data, mSpinnerIndex_data);
        editor.commit();
    }

    private void updateUI(){
        offeredData.setText(Formatter.formatFileSize(getContext(), offeredDataAmount));
        remainData.setText(Formatter.formatFileSize(getContext(), remainDataAmount));
        offeredTotalData.setText(Formatter.formatFileSize(getContext(), offeredTotalDataAmount));
    }

    private long getCurOfferedData(){
        return 0L;
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
}
