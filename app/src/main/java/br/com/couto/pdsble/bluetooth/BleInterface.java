package br.com.couto.pdsble.bluetooth;

import android.bluetooth.le.ScanResult;

public interface BleInterface {

    void onResultScan(ScanResult result);
    void onFinishScan();
    void onResponse(byte [] message);
    void onConectionStatus(int newState);
}
