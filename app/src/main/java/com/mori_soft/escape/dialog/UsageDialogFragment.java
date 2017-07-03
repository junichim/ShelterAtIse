package com.mori_soft.escape.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class UsageDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.usage);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_usage, null);

        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, null);

        return builder.show();
    }


}
