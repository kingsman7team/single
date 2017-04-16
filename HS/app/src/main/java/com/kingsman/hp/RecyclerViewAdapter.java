package com.kingsman.hp;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.kingsman.hp.ap.AccessPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter}
 * TODO: Replace the implementation with code for your data type.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemHolder> implements View.OnClickListener{

    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String OPEN = "Open";
    private final Context mContext;
    private final List<AccessPoint> mItems = new ArrayList<>();
    private final List<Integer> mTypes = new ArrayList<>();

    public RecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(
                viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        switch (mTypes.get(position)) {
            case R.layout.access_point_empty:
                onBindEmpty(holder);
                break;
            case R.layout.access_point_header:
                onBindHeader(holder);
                break;

            case R.layout.access_point_item:
                final AccessPoint accessPoint = (AccessPoint) mItems.get(position);
                onBindItem(holder, accessPoint);
                holder.itemView.setTag(accessPoint);
                holder.itemView.setOnClickListener(this);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        /* Implements HS Connection */
    }

    private void onBindEmpty(ItemHolder holder) {
        holder.title.setText("empty");
    }

    private void onBindItem(ItemHolder holder, AccessPoint accessPoint) {

        if (accessPoint.mRssi == Integer.MAX_VALUE) {
            holder.icon.setImageDrawable(null);
        } else {
            holder.icon.setImageLevel(getLevel(accessPoint.mRssi));
            holder.icon.setImageResource(R.drawable.wifi_signal_dark);
        }

        holder.title.setText(accessPoint.ssid);
        holder.summary.setVisibility(View.GONE);
    }

    private void onBindHeader(ItemHolder holder) {
        holder.title.setText("category");
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mTypes.size()==0 ? 0 : mTypes.get(position);
    }



    private void reset() {
        mItems.clear();
        mTypes.clear();
    }

    private void updateItem(AccessPoint accessPoint, int type) {
        mItems.add(accessPoint);
        mTypes.add(type);
    }


    public void updateItems(List<AccessPoint> accessPoints) {
        reset();
        if (accessPoints == null || accessPoints.isEmpty()) {
            updateItem(null, R.layout.access_point_empty);
        } else {
            updateItem(null, R.layout.access_point_header);

            for (AccessPoint accessPoint : accessPoints) {
                updateItem(accessPoint, R.layout.access_point_item);
            }
        }

        notifyDataSetChanged();
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

    int getLevel(int mRssi) {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 5);
    }


    public class ItemHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView title;
        public final TextView summary;

        public ItemHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.icon);
            title = (TextView) view.findViewById(R.id.title);
            summary = (TextView) view.findViewById(R.id.summary);
        }
    }
}
