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
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mori_soft.escape.R;

/**
 * 情報表示用ダイアログ.
 */

public class InfoDialogFragment extends DialogFragment {

    private static final String KEY_TITLE = "key_title";
    private static final String KEY_TEXT_ABSTRACT = "key_text_abstract";
    private static final String KEY_TEXT_DETAIL = "key_text_detail";

    public interface onInfoDialogListener {
        void onOkClickListener();
    }

    private onInfoDialogListener mListener = null;

    public static InfoDialogFragment getInstance(int titleRes) {
        return getInstance(titleRes, 0, 0);
    }
    public static InfoDialogFragment getInstance(int titleRes, int textAbstractRes) {
        return getInstance(titleRes, textAbstractRes, 0);
    }
    public static InfoDialogFragment getInstance(int titleRes, int textAbstractRes, int textDetailRes) {
        InfoDialogFragment df = new InfoDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TITLE, titleRes);

        if (textAbstractRes != 0) {
            bundle.putInt(KEY_TEXT_ABSTRACT, textAbstractRes);
        }
        if (textDetailRes != 0) {
            bundle.putInt(KEY_TEXT_DETAIL, textDetailRes);
        }

        df.setArguments(bundle);
        return df;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int titleRes = 0;
        int textAbstractRes = 0;
        int textDetailRes = 0;

        Bundle args = this.getArguments();
        if (args != null) {
            titleRes = args.getInt(KEY_TITLE, 0);
            textAbstractRes = args.getInt(KEY_TEXT_ABSTRACT, 0);
            textDetailRes = args.getInt(KEY_TEXT_DETAIL, 0);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleRes);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_info, null);
        TextView tvAbstract = v.findViewById(R.id.info_abstract);
        TextView tvDetail = v.findViewById(R.id.info_detail);
        setTextViewContent(tvAbstract, textAbstractRes);
        setTextViewContent(tvDetail, textDetailRes);

        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null) {
                    mListener.onOkClickListener();
                }
            }
        });

        return builder.show();
    }

    private void setTextViewContent(TextView tv, int resId) {
        if (resId == 0) {
            tv.setVisibility(View.GONE);
            return;
        }

        String str = this.getResources().getString(resId);
        if (TextUtils.isEmpty(str)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(str);
        }
    }

    public void setClickListener(onInfoDialogListener listener) {
        mListener = listener;
    }

}
