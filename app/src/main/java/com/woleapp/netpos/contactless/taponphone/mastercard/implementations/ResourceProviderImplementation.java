package com.woleapp.netpos.contactless.taponphone.mastercard.implementations;


import android.content.Context;
import android.content.res.Resources;

import com.mastercard.terminalsdk.listeners.ResourceProvider;
import com.woleapp.netpos.contactless.BuildConfig;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class ResourceProviderImplementation implements ResourceProvider {

    Resources res;

    public ResourceProviderImplementation(Context context) {
        res = context.getResources();
    }

    @Override
    public InputStream getResource(String fileName) {
        // * Return an InputStream for the fileName
        try {
            return res.getAssets().open(fileName);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Timber.e("getResource: EXCEPTION %s", e.getMessage());
            }
        }
        return null;
    }
}
