package com.smartcl.accountAlert;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.smartcl.communicationlibrary.MessageSender;

import org.json.simple.JSONObject;

import java.util.List;

import eu.smartbeacon.sdk.core.SBBeacon;
import eu.smartbeacon.sdk.core.SBLocationManager;
import eu.smartbeacon.sdk.core.SBLocationManagerListener;
import eu.smartbeacon.sdk.utils.SBLogger;

/**
 * Service which scans the smartbeacons around.
 * If a smartbecon is found, it sends a message to the handheld device
 * via the MessageSender class.
 */
public class DiscoverBeaconsService extends Service implements SBLocationManagerListener {

    public static final String BEACON_ENTERED_PATH = "/beacon/entered/";
    private final String GET_PREFERENCES = "/preferences/";
    private boolean _isInitialized = false;
    private MessageSender _messageSender = null;
    private boolean _arePreferencesGotten = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeBeacons();
    }

    private void initializeBeacons() {
        if (!_isInitialized) {
            _isInitialized = true;

            _messageSender = new MessageSender(this);
            // disable logging message
            SBLogger.setSilentMode(true);

            SBLocationManager sbManager = SBLocationManager.getInstance(this);
            sbManager.addEntireSBRegion();
            sbManager.addBeaconLocationListener(this);
            sbManager.startMonitoringAllBeaconRegions();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeBeacons();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        SBLocationManager.getInstance(this)
                .stopMonitoringAllBeaconRegions();
        super.onDestroy();
    }

    @Override
    public void onEnteredBeacons(List<SBBeacon> sbBeacons) {
        if (_arePreferencesGotten == false) {
            _arePreferencesGotten = true;
            _messageSender.sendMessage(GET_PREFERENCES);
        }
        for (SBBeacon beacon : sbBeacons) {
            JSONObject json = buildJsonObjectFromBeacon(beacon);
            _messageSender.sendMessage(BEACON_ENTERED_PATH, json);
            //Toast.makeText(this, "Beacon enter", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onExitedBeacons(List<SBBeacon> sbBeacons) {
        for (SBBeacon beacon : sbBeacons) {
            //Toast.makeText(this, "Beacon exit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDiscoveredBeacons(List<SBBeacon> sbBeacons) {
    }

    @Override
    public void onUpdatedProximity(SBBeacon sbBeacon, SBBeacon.Proximity proximity,
                                   SBBeacon.Proximity proximity2) {
    }

    private JSONObject buildJsonObjectFromBeacon(SBBeacon beacon) {
        JSONObject json = new JSONObject();
        json.put("major", beacon.getMajor());
        json.put("minor", beacon.getMinor());
        return json;
    }

}
