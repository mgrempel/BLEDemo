package com.example.btdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    //BLE advertiser stuff
    private AdvertiseSettings settings;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseData advertiseData;
    private AdvertiseCallback callBack;

    //BLE scanner stuff
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private ScanSettings scanSettings;

    private Button toggle, check, advert, scan;

    private boolean listening, broadcasting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize bluetooth manager and adapter
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //Setup for the BLE Advertiser
        advertiserPrimer();

        //Setup for the BLE Scanner
        listenerPrimer();

        //initialize controls
        toggle = findViewById(R.id.toggle);
        check = findViewById(R.id.check);
        advert = findViewById(R.id.toggleAdvertiser);
        scan = findViewById(R.id.toggleScanner);

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Intent enableBlueToothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBlueToothIntent, 1);
                }
                else {
                    advertiser.stopAdvertising(callBack);
                    bluetoothAdapter.disable();
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter != null) {
                    if(bluetoothAdapter.isEnabled()) {
                        Toast.makeText(v.getContext(), "Enabled!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(v.getContext(), "Disabled!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        advert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isEnabled()) {
                    if(!broadcasting) {
                        //Reinitializing this as it seems to wind up null when bluetooth is toggled
                        advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
                        advertiser.startAdvertising(settings, advertiseData, callBack);
                        broadcasting = true;
                    }
                    else {
                        advertiser.stopAdvertising(callBack);
                        broadcasting = false;
                        Toast.makeText(v.getContext(), "Stopped Advertising!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isEnabled()) {
                    if(!listening) {
                        scanner = bluetoothAdapter.getBluetoothLeScanner();
                        scanner.startScan(null, scanSettings, scanCallback);
                        listening = true;
                        Toast.makeText(v.getContext(), "Started Listening!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        scanner.stopScan(scanCallback);
                        listening = false;
                        Toast.makeText(v.getContext(), "Stopped Listening!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void advertiserPrimer() {
        //set instance BLE flags
        broadcasting = false;

        //configure our Advertiser settings
        settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        //Configure our Advertisement data
        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
//                .addServiceUuid(parcelUuid) Saving these for later, just passing the device name for now.
//                .addServiceData(pServiceDataUuid, "Data".getBytes())
                .build();

        //Configure our advertisement call backs
        callBack = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settings) {
                Log.d("TEST", "BLE started advertising");
                Toast.makeText(MainActivity.this, "Started Advertising!", Toast.LENGTH_SHORT).show();
                super.onStartSuccess(settings);
            }

            @Override
            public void onStartFailure(int code) {
                Log.d("TEST", "BLE Error code " + code);
                super.onStartFailure(code);
            }
        };
    }

    private void listenerPrimer() {
        //set instance BLE flags
        listening = false;

        //configure our scan result callback
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                //Did we get anything from the scan? If not, let's dip.
                if(result == null || result.getDevice() == null || TextUtils.isEmpty(result.getDevice().getName()))
                    return;

                //What sort of device is calling us?
                String data = result.getDevice().getName();

                Log.d("TEST", "Device name: " + data);
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.d( "TEST", "Discovery failed with code: " + errorCode );
                super.onScanFailed(errorCode);
            }
        };

        //Let's configure our scan settings
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }
}