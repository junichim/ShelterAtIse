package com.mori_soft.escape.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mori_soft.escape.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mor on 2017/07/03.
 */

public class LegendDialogFragment extends DialogFragment {

    private static class LegendItem {
        int drawableId;
        int textId;
        public LegendItem(int drawable_id, int text_id) {
            drawableId = drawable_id;
            textId = text_id;
        }
    }
    private static class LegendAdapter extends ArrayAdapter<LegendItem> {
        private LayoutInflater mInflater;

        public LegendAdapter(Context context, int resource, List<LegendItem> objects) {
            super(context, resource, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LegendItem item = (LegendItem) getItem(position);
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.legend_item, null);
            }

            ImageView iv = (ImageView) convertView.findViewById(R.id.legend_image);
            iv.setImageResource(item.drawableId);

            TextView tv = (TextView) convertView.findViewById(R.id.legend_text);
            tv.setText(item.textId);

            return convertView;
        }
    }

    private static final int[] DRAWABLE_LIST = {
        R.drawable.ic_my_location,
        R.drawable.marker_red,
        R.drawable.marker_yellow,
        R.drawable.marker_green,
        R.drawable.marker_gray,
    };
    private static final int[] DESCRIPTIONS = {
        R.string.legend_current,
        R.string.legend_red,
        R.string.legend_yellow,
        R.string.legend_green,
        R.string.legend_gray,
    };
    private static List<LegendItem> mList = new ArrayList<LegendItem>();

    static {
        for (int i = 0; i < DRAWABLE_LIST.length; i++) {
            mList.add(new LegendItem(DRAWABLE_LIST[i], DESCRIPTIONS[i]));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("凡例");

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_legend, null);
        ListView lv = (ListView) v.findViewById(R.id.legend_list);
        ArrayAdapter adapter = new LegendAdapter(getActivity(), 0, mList);
        lv.setAdapter(adapter);

        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, null);

        return builder.show();
    }


}