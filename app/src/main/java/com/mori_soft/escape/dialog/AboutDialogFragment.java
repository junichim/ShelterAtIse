package com.mori_soft.escape.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.mori_soft.escape.R;

/**
 * Created by mor on 2017/07/03.
 */

public class AboutDialogFragment extends DialogFragment {
    private static final int[] RES_IDS = {R.id.about_license_me, R.id.about_license_mapsforge, R.id.about_license_graphhopper, R.id.about_license_shelter, R.id.about_license_icon};
    private static final int[] RES_STRINGS = {R.string.about_license_me, R.string.about_license_mapsforge, R.string.about_license_graphhopper, R.string.about_license_shelter, R.string.about_license_icon};
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);

        for (int i = 0; i < RES_IDS.length; i++) {
            TextView tv = (TextView) v.findViewById(RES_IDS[i]);
            String str = getActivity().getResources().getString(RES_STRINGS[i]);
            tv.setText(Html.fromHtml(str));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }

        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, null);

        return builder.show();
    }


}
