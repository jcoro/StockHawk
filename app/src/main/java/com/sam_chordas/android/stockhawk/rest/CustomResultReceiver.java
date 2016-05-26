package com.sam_chordas.android.stockhawk.rest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Returns information from Intent Services to Activities which implement CustomResultReceiver.Receiver
 */
@SuppressLint("ParcelCreator")
public class CustomResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public CustomResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}
