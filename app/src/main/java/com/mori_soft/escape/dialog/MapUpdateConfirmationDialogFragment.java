package com.mori_soft.escape.dialog;

import android.content.Context;

import androidx.fragment.app.Fragment;

public class MapUpdateConfirmationDialogFragment extends UpdateConfirmationDialogFragment {

    public interface onUpdateListener {
        void onOkClickMap();
    }

    private static final int FRAGMENT_TARGET = 1;

    private onUpdateListener mListener = null;

    public static MapUpdateConfirmationDialogFragment getInstance(Fragment target) {
        MapUpdateConfirmationDialogFragment f = new MapUpdateConfirmationDialogFragment();
        f.setTargetFragment(target, FRAGMENT_TARGET);
        return f;
    }

    @Override
    protected String getTitle() {
        return "オフラインマップ更新の確認";
    }

    @Override
    protected String getMessage() {
        return "最新のオフラインマップがあります。更新しますか？";
    }

    @Override
    protected void onOkClick() {
        if (null != mListener) {
            mListener.onOkClickMap();
        }
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
