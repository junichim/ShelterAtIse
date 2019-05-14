/*
 * Copyright 2017 Junichi MORI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mori_soft.escape.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.mori_soft.escape.R;

/**
 * 『このアプリについて』ダイアログ.
 */

public class AboutDialogFragment extends DialogFragment {
    private static final int[] RES_IDS = {R.id.about_license_me, R.id.about_license_mapsforge, R.id.about_license_graphhopper, R.id.about_license_shelter, R.id.about_license_osm, R.id.about_license_icon};
    private static final int[] RES_STRINGS = {R.string.about_license_me, R.string.about_license_mapsforge, R.string.about_license_graphhopper, R.string.about_license_shelter, R.string.about_license_osm, R.string.about_license_icon};
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
