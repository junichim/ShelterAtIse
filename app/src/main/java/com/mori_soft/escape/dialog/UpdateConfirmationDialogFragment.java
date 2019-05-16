package com.mori_soft.escape.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

public class UpdateConfirmationDialogFragment extends DialogFragment {

    public interface onUpdateListener {
        void onOkClickListener();
        void onCancelListener();
    }

    private static final int FRAGMENT_TARGET = 1;
    private onUpdateListener mListener = null;

    public static UpdateConfirmationDialogFragment getInstance(Fragment target) {
        UpdateConfirmationDialogFragment f = new UpdateConfirmationDialogFragment();
        f.setTargetFragment(target, FRAGMENT_TARGET);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("オフラインマップ更新の確認")
               .setMessage("最新のオフラインマップがあります。更新しますか？")
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                        if (null != mListener) {
                            mListener.onOkClickListener();
                        }
                   }
               })
               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       if (null != mListener) {
                           mListener.onCancelListener();
                       }
                   }
               });

        return builder.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Fragment f = this.getTargetFragment();
        if (f instanceof onUpdateListener) {
            mListener = (onUpdateListener) f;
        }
    }

}
