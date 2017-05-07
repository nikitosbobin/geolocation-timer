package com.nikit.bobin.geolocationtimer;

import android.content.Context;
import android.location.Address;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeoInfoListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private LocationHelper locationHelper;
    private Context context;

    public GeoInfoListViewAdapter(Context context) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        locationHelper = new LocationHelper(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return (int) GeoInfo.count(GeoInfo.class);
    }

    @Override
    public GeoInfo getItem(int position) {
        List<GeoInfo> geoInfos = GeoInfo.listAll(GeoInfo.class);
        return geoInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.geo_info_list_view_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        GeoInfo item = getItem(position);
        holder.title.setText(item.getTitle());
        Address address = locationHelper.getAddress(item.getLocation());
        if (address != null) {
            holder.addressLine.setVisibility(View.VISIBLE);
            holder.addressLine.setText(address.getAddressLine(0));
        }
        else
            holder.addressLine.setVisibility(View.GONE);
        holder.spentTime.setText(DateUtils.formatElapsedTime(item.getSpentTimeSeconds()));
        if (!item.isNotifyPeriodEnds() && !item.isClearTimerEachPeriod())
            holder.periodInfo.setVisibility(View.GONE);
        else {
            int periodDays = item.getPeriodDays();
            String periodStart = DateUtils.formatDateTime(
                    context,
                    item.getPeriodStart().getTime(),
                    0);
            String format = String.format(
                    Locale.getDefault(),
                    "Updates every %d days starts of %s",
                    periodDays,
                    periodStart);
            holder.periodInfo.setText(format);
        }
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.address_line)
        TextView addressLine;
        @BindView(R.id.spent_time)
        TextView spentTime;
        @BindView(R.id.period_info)
        TextView periodInfo;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
