package com.mori_soft.escape.dialog;

import android.content.Context;

import androidx.fragment.app.Fragment;

public class ShelterUpdateConfirmationDialogFragment extends UpdateConfirmationDialogFragment {

    public interface onUpdateListener {
        void onOkClickShelter();
    }

    private static final int FRAGMENT_TARGET = 10;

    private onUpdateListener mListener = null;

    public static ShelterUpdateConfirmationDialogFragment getInstance(Fragment target) {
        ShelterUpdateConfirmationDialogFragment f = new ShelterUpdateConfirmationDialogFragment();
        f.setTargetFragment(target, FRAGMENT_TARGET);
        return f;
    }

    @Override
    protected String getTitle() {
        return "避難所情報更新の確認";
    }

    @Override
    protected String getMessage() {
        return "最新の避難所情報があります。更新しますか？";
    }


    @Override
    protected void onOkClick() {
        if (null != mListener) {
            mListener.onOkClickShelter();
        }
    }
    @Override
    protected void onCancelClick() {
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
