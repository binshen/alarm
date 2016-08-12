package com.moral.alarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moral.alarm.R;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by bin.shen on 8/10/16.
 */
public class AlarmAdapter extends BaseAdapter {

    private List<JSONObject> alarmList;
    private LayoutInflater layoutInflater;

    public AlarmAdapter(Context content, List<JSONObject> alarms) {
        this.layoutInflater = LayoutInflater.from(content);
        this.alarmList = alarms;
    }

    @Override
    public int getCount() {
        return alarmList.size();
    }

    @Override
    public Object getItem(int position) {
        return alarmList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_alarms, null);
        }

        JSONObject data = this.alarmList.get(position);
        RelativeLayout rl_alarm = (RelativeLayout) convertView.findViewById(R.id.rl_alarm);
        TextView alarm1 = (TextView) convertView.findViewById(R.id.tv_alarm1);
        TextView alarm2 = (TextView) convertView.findViewById(R.id.tv_alarm2);
        alarm1.setText(data.optString("company") +  " - " + data.optString("mac"));
        alarm2.setText(data.optString("created"));
        int level = data.optInt("level");
        if(level == 1) {
            rl_alarm.setBackgroundColor(convertView.getResources().getColor(R.color.colorYellow));
        } else if(level == 2) {
            rl_alarm.setBackgroundColor(convertView.getResources().getColor(R.color.colorOrange));
        } else {
            rl_alarm.setBackgroundColor(convertView.getResources().getColor(R.color.colorRed));
        }

        return convertView;
    }
}
