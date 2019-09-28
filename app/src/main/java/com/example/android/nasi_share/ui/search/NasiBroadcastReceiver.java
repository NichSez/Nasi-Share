package com.example.android.nasi_share.ui.search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.example.android.nasi_share.R;
import com.example.android.nasi_share.ui.search.DeviceDetailFragment;
import com.example.android.nasi_share.ui.search.DeviceListFragment;
import com.example.android.nasi_share.ui.search.SearchFragment;

/**
 * This class will listen for changes to the System's Wi-Fi P2P state.
 * Which means reports any change in Wi-Fi P2P state to application
 */

public class NasiBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private SearchFragment fragment;


    /**
     * overwrite class constructor
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param fragment fragment associated with the receiver
     */
    public NasiBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                 SearchFragment fragment) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.fragment = fragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            // Check to see if Wi-Fi is enabled and notify appropriate fragment
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                fragment.setIsWifiP2pEnabled(true);
            } else {
                fragment.setIsWifiP2pEnabled(false);
                fragment.resetData();
            }
            Log.d(SearchFragment.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //1 request available peers from the wifi p2p manager. This is an
            //1 asynchronous call and the calling activity is notified with a
            //1 callback on PeerListListener.onPeersAvailable()
            //2 Indicates a change in the list of available peers.
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) fragment.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }
            Log.d(SearchFragment.TAG, "P2P peers changed");
        }
//        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//            //1 Indicates the state of Wi-Fi P2P connectivity has changed.
//            //2 Respond to new connection or disconnections
//            if (manager == null) {
//                return;
//            }
//            NetworkInfo networkInfo = (NetworkInfo) intent
//                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//            if (networkInfo.isConnected()) {
//                // we are connected with the other device, request connection
//                // info to find group owner IP
//                DeviceDetailFragment fragmentD = (DeviceDetailFragment) fragment
//                        .getFragmentManager().findFragmentById(R.id.frag_detail);
//                manager.requestConnectionInfo(channel, fragmentD);
//            } else {
//                // It's a disconnect
//                fragment.resetData();
//            }
//        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragmentD = (DeviceListFragment) fragment.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragmentD.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
//        }
    }
}
