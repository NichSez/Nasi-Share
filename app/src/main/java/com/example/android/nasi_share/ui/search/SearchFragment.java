package com.example.android.nasi_share.ui.search;

import androidx.fragment.app.Fragment;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.android.nasi_share.R;
import com.example.android.nasi_share.ui.search.DeviceListFragment.DeviceActionListener;
import com.example.android.nasi_share.ui.send.SendViewModel;

import static android.os.Looper.getMainLooper;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */

public class SearchFragment extends Fragment implements ChannelListener, DeviceActionListener {

    private SearchViewModel searchViewModel;
    private boolean isWifiP2pEnabled = false;
    public static final String TAG = "nasisharedemo";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
    private WifiP2pManager manager;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        searchViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;

    }

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION:
                if  (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Coarse location permission is not granted!");
                    getActivity().finish();
                }
                break;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //for having optionMenu in this fragment
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);
        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getContext(), getMainLooper(), null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    SearchFragment.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }
    }

    private void setContentView(int fragment_search) {
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new NasiBroadcastReceiver(manager, channel, this);
        getActivity().registerReceiver(receiver, intentFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }
    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
    }
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;
            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(getContext(), R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getActivity().getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(getContext(), "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }
    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(getContext(), "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }
            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }
        });
    }
    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(getContext(), "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(getContext(), getMainLooper(), this);
        } else {
            Toast.makeText(getContext(),
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getActivity().getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(getContext(),
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}