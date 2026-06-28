package com.animalist.app;

import android.content.Context;
import android.util.AttributeSet;

public class ReselectableSpinner extends androidx.appcompat.widget.AppCompatSpinner {

    public ReselectableSpinner(Context context) {
        super(context);
    }

    public ReselectableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReselectableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);

        // KUNCI HACK: Kalau yang dipilih item yang sama, paksa jalankan fungsinya!
        if (sameSelected && getOnItemSelectedListener() != null) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);

        // KUNCI HACK: Kalau yang dipilih item yang sama, paksa jalankan fungsinya!
        if (sameSelected && getOnItemSelectedListener() != null) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}