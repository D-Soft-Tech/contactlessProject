package com.woleapp.netpos.contactless.taponphone.mastercard.implementations.nfc;

import android.app.Activity;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mastercard.terminalsdk.exception.L1RSPException;
import com.mastercard.terminalsdk.listeners.CardCommunicationProvider;
import com.mastercard.terminalsdk.objects.ErrorIndication;

import java.io.IOException;

public class NfcProvider implements CardCommunicationProvider {

    private final String TAG = "NfcProvider";

    @Nullable public NFCManager mNFCManager;
    private IsoDep mIsoDep;
    private TagEventListener mTagEventListener;
    private boolean isCardTapped;


    private boolean nFCEnabled;

    /**
     * Command execution time in nano seconds
     */
    private long mCommandExecutionTime = 0;

    public NfcProvider(Activity currentContext) {

        mNFCManager = new NFCManager(currentContext);
        // Check if NFC is enabled
        if (!mNFCManager.isNFCEnabled()) {
            nFCEnabled = false;
        } else {
            nFCEnabled = true;
        }
        disconnectReader();
    }

    @Override
    public byte[] sendReceive(byte[] bytes) throws L1RSPException {

        byte[] response;
        try {
            if (mIsoDep.isConnected()) {
                long startTime = System.nanoTime();
                response = mIsoDep.transceive(bytes);
                long endTime = System.nanoTime();
                // Command execution time in nano seconds
                mCommandExecutionTime = endTime - startTime;
            } else {

                isCardTapped = false;
                throw new L1RSPException("", ErrorIndication.L1_Error_Code.TRANSMISSION_ERROR);
            }
        } catch (TagLostException exception) {
            isCardTapped = false;
            throw new L1RSPException("Tag Lost", ErrorIndication.L1_Error_Code.TIMEOUT_ERROR);

        } catch (IOException e) {
            isCardTapped = false;
            throw new L1RSPException(e.getMessage(), ErrorIndication.L1_Error_Code.TRANSMISSION_ERROR);

        }
        if (response.length < 2) {
            throw new L1RSPException("Response Length less than 2 bytes",
                    ErrorIndication.L1_Error_Code.PROTOCOL_ERROR);
        }

        return response;
    }

    @Override
    public ConnectionObject waitForCard() throws L1RSPException {
        if (isCardTapped) {
            return new ConnectionObjectImpl();
        }
        while (!isCardTapped) {
            try {
                mIsoDep = mTagEventListener.getIsoDep();
            } catch (Exception e) {
            }
            if (mIsoDep != null) {
                try {
                    mIsoDep.connect();
                    mIsoDep.setTimeout(10000);
                    isCardTapped = true;
                    return new ConnectionObjectImpl();
                } catch (IOException | IllegalStateException e) {
                    throw new L1RSPException(e.getMessage(),
                            ErrorIndication.L1_Error_Code.PROTOCOL_ERROR);
                }
            }
        }
        throw new L1RSPException("Some Connection issue",
                ErrorIndication.L1_Error_Code.PROTOCOL_ERROR);
    }

    @Override
    public boolean removeCard() {

        if (mIsoDep != null && mIsoDep.isConnected()) {
            try {
                mIsoDep.close();
                isCardTapped = false;
                mIsoDep = null;
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean connectReader() throws L1RSPException {

        if (mIsoDep == null) {
            // Establish Connection with reader
            mTagEventListener = new TagEventListener();
            mNFCManager.enableNFCReaderMode(new ReaderCallbackImpl(mTagEventListener));
            mIsoDep = null;
            isCardTapped = false;
        }
        return true;
    }

    @Override
    public boolean disconnectReader() {
        try {
            mIsoDep.close();
        } catch (IOException e) {
            return false;
        } catch (NullPointerException npe) {
            return true;
        }

        mNFCManager.disableNFCReaderMode();

        mIsoDep = null;
        isCardTapped = false;
        return true;
    }

    public void disableReaderMode() {
        mNFCManager.disableNFCReaderMode();
    }

    @Override
    public boolean isReaderConnected() {
        return true;
    }

    @Override
    public boolean isCardPresent() {
        return mIsoDep.isConnected();
    }

    @Override
    public String getDescription() {
        return "Built-in NFC Controller";
    }

    @Override
    public long getPreviousCommandExecutionTime() {
        // return command execution time in microseconds
        return mCommandExecutionTime / 1000;
    }

    public boolean isNfcEnabled() {
        return mNFCManager.isNFCEnabled();
    }

    private class ConnectionObjectImpl implements ConnectionObject {

        @Override
        public InterfaceType getInterfaceType() {
            return InterfaceType.CONTACTLESS;
        }

        @Override
        public byte[] getBytes() {
            return new byte[0];
        }
    }
}
