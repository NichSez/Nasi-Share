package com.example.android.nasi_share.ui.search;

import androidx.fragment.app.Fragment;

public class SearchFragment extends Fragment {

    private boolean isWifiP2pEnabled = false;
    public static final String TAG = "nasisharedemo";


    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }


    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    //TODO: complete this function for changes in UI
    public void resetData() {
    }
}