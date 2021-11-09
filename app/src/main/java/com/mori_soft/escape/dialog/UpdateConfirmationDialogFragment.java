package com.mori_soft.escape.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

abstract public class UpdateConfirmationDialogFragment extends DialogFragment {

    abstract protected String getTitle();
    abstract protected String getMessage();
    abstract protected void onOkClick();
    abstract protected void onCancelClick();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(getTitle())
               .setMessage(getMessage())
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       onOkClick();
                   }
               })
               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       onCancelClick();
                   }
               });

        return builder.show();
    }

}
