package com.testrangebeacons;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends Activity implements BeaconConsumer {

    protected static final String TAG = "BeaconRangingActivity";
    private BeaconManager beaconManager;
    long scanPeriod=1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BeaconManager.setsManifestCheckingDisabled(true);

        findViewById(R.id.reset_beacons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor edit=pref.edit();
                edit.clear();
                edit.commit();
            }
        });


        beaconManager= BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setBackgroundScanPeriod(scanPeriod);
        beaconManager.setForegroundScanPeriod(scanPeriod);
        beaconManager.setBackgroundBetweenScanPeriod(scanPeriod);
        beaconManager.setForegroundBetweenScanPeriod(scanPeriod);

        beaconManager.bind(this);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        onBeaconServiceConnect();

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(beaconManager!=null)
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(beaconManager!=null)
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    final String azureBeacon="E9:44:BC:80:08:E4";
    final String cyanoBeacon="C1:B3:98:AF:EE:31";
    final String violetBeacon="DE:5B:F6:AD:10:1B";
    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "Beacon service connected");
        beaconManager.setRangeNotifier(new RangeNotifier() {
            int nearness_index = 0;
            int idnotification = 1;

            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                final ArrayList<String> array_beacons = new ArrayList<String>();
                nearness_index = 0;
                if (beacons.size() > 0) {
                    final Beacon[] beacons_array = beacons.toArray(new Beacon[beacons.size()]);
                    for (int i = 0; i < beacons_array.length; i++) {
                        Log.i(TAG, "beacon: " + beacons_array[i].getBluetoothAddress());
                        if (beacons_array[i].getBluetoothAddress().equals(azureBeacon)) {
                            array_beacons.add(beacons_array[i].getBluetoothName() + " Azzurro" + " " + round(beacons_array[i].getDistance(), 2) + " m " + beacons_array[i].getRssi()
                                    + " tx:" + beacons_array[i].getTxPower());
                        } else if (beacons_array[i].getBluetoothAddress().equals(violetBeacon)) {
                            array_beacons.add(beacons_array[i].getBluetoothName() + " Viola" + " " + round(beacons_array[i].getDistance(), 2) + " m " + beacons_array[i].getRssi()
                                    + " tx:" + beacons_array[i].getTxPower());
                        } else if (beacons_array[i].getBluetoothAddress().equals(cyanoBeacon)) {
                            array_beacons.add(beacons_array[i].getBluetoothName() + " Cyano" + " " + round(beacons_array[i].getDistance(), 2) + " m " + beacons_array[i].getRssi()
                                    + " tx:" + beacons_array[i].getTxPower());
                        } else {
                            array_beacons.add(beacons_array[i].getBluetoothName() + " NV" + " " + round(beacons_array[i].getDistance(), 2) + " m " + beacons_array[i].getRssi()
                                    + beacons_array[i].getTxPower());
                        }
                        if (beacons_array[i].getDistance() < beacons_array[nearness_index].getDistance()) {
                            nearness_index = i;
                        }

                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if (!pref.contains(beacons_array[i].getBluetoothAddress())) {
                            SharedPreferences.Editor prefEdit = pref.edit();
                            prefEdit.putString(beacons_array[i].getBluetoothAddress(), beacons_array[i].getBluetoothAddress());
                            prefEdit.commit();

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            //.setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle("New Beacon")
                                            .setContentText("Beacon id: " + beacons_array[i].getBluetoothAddress());
                            NotificationManager mNotifyMgr =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            //mNotifyMgr.notify(idnotification++, mBuilder.build());

                        }


                    }
                    final String[] array_beacons_static = new String[array_beacons.size()];
                    for (int i = 0; i < array_beacons.size(); i++) {
                        array_beacons_static[i] = array_beacons.get(i);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, array_beacons_static);
                            ((ListView) findViewById(R.id.list_wifi_scan)).setAdapter(adapter);
                            if (beacons_array[nearness_index].getBluetoothAddress().equals(azureBeacon)) {
                                ((ListView) findViewById(R.id.list_wifi_scan)).setBackgroundColor(Color.parseColor("#FF33B7CF"));
                            } else if (beacons_array[nearness_index].getBluetoothAddress().equals(violetBeacon)) {
                                ((ListView) findViewById(R.id.list_wifi_scan)).setBackgroundColor(Color.parseColor("#FF7330D7"));
                            } else if (beacons_array[nearness_index].getBluetoothAddress().equals(cyanoBeacon)) {
                                ((ListView) findViewById(R.id.list_wifi_scan)).setBackgroundColor(Color.parseColor("#FF15DDAE"));
                            } else {
                                ((ListView) findViewById(R.id.list_wifi_scan)).setBackgroundColor(Color.parseColor("#FF505050"));
                            }


                            if (beacons_array[nearness_index].getBluetoothAddress().equals(azureBeacon)) {
                                ((TextView) findViewById(R.id.text_beacon)).setText("Ti trovi al piano Design");
                                        //+ " " + round(beacons_array[nearness_index].getDistance(), 2) + " m");
                            } else if (beacons_array[nearness_index].getBluetoothAddress().equals(violetBeacon)) {
                                ((TextView) findViewById(R.id.text_beacon)).setText("Ti trovi in Cucina");
                                        //+ " " + round(beacons_array[nearness_index].getDistance(), 2) + " m");
                            } else if (beacons_array[nearness_index].getBluetoothAddress().equals(cyanoBeacon)) {
                                ((TextView) findViewById(R.id.text_beacon)).setText("Ti trovi in stanza Developer");
                                        //+ " " + round(beacons_array[nearness_index].getDistance(), 2) + " m");
                            } else {
                                ((TextView) findViewById(R.id.text_beacon)).setText("Più vicino: Non verificato"
                                        + " " + round(beacons_array[nearness_index].getDistance(), 2) + " m");
                            }


                            //((TextView) findViewById(R.id.text_beacon)).setText(round(beacons.iterator().next().getDistance(),2) + " m");
                        }
                    });
                    //Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                } else {
                    if (region == null) {
                        Log.i(TAG, "Region null");
                    } else
                        Log.i(TAG, "No beacons");

                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myIdBeacon", null, null, null));
        } catch (RemoteException e) {    }
    }
}
