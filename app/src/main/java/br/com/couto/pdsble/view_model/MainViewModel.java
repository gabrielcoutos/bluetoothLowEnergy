package br.com.couto.pdsble.view_model;

import android.Manifest;
import android.app.Application;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.com.couto.pdsble.bluetooth.BleInterface;
import br.com.couto.pdsble.bluetooth.BleUtils;
import br.com.couto.pdsble.utils.Utils;


public class MainViewModel extends ViewModel implements BleInterface {

    public final static String PERMISSION = "FINE_PERMISSION";

    public final static String BLE_MAC = "02:80:E1:80:00:01";

    public final ObservableField<String> status = new ObservableField<>("DESCONECTADO");

    public final ObservableField<String> comandoEnviado = new ObservableField<>("0xb3 0x74 0x56 0x78 0xff 0x4 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x0 0x12");

    public final ObservableField<String> comandoRecebido = new ObservableField<>();

    public final ObservableField<String> scan = new ObservableField<>("SCAN");

    public final ObservableBoolean enableBtn = new ObservableBoolean(true);

    public final ObservableBoolean connected = new ObservableBoolean(false);

    public final MutableLiveData<String> getPermission = new MutableLiveData<>();

    public final ObservableInt progress = new ObservableInt(View.GONE);

    private BleUtils mBlueUtils;

    private Context mContext;

    public void initScan(Context context) {
        mContext = context;
        if (checkPermission(context)) {
            mBlueUtils = BleUtils.getInstance(context);
            mBlueUtils.setmBleListener(this);
            mBlueUtils.startScan();
            enableBtn.set(false);
            scan.set("SCANING");
        }
    }

    private boolean checkPermission(Context context) {
        if (!(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            getPermission.postValue(PERMISSION);
            return false;
        }
        return true;
    }


    @Override
    public void onResultScan(ScanResult result) {
        if (result.getDevice().getName() != null) {
            if (result.getDevice().getAddress().equals(BLE_MAC)) {
                mBlueUtils.connectBle(result.getDevice(), mContext, null);
            }
        }

    }

    @Override
    public void onFinishScan() {
        scan.set("SCAN");
        enableBtn.set(true);
    }

    @Override
    public void onResponse(byte[] message) {
        progress.set(View.GONE);
        String data = Utils.hexArrayToString(message);
        comandoRecebido.set(data);
    }

    @Override
    public void onConectionStatus(int newState) {
        switch (newState) {
            case 0:
                Log.d("STATE: ","DESCONECTADO");
                connected.set(false);
                status.set("DESCONECTADO");
                break;

            case 1:
                Log.d("STATE: ","CONECTANDO");
                status.set("CONECTANDO");
                break;

            case 2:
                Log.d("STATE: ","CONECTADO");
                connected.set(true);
                status.set("CONECTADO");
                break;

            default:
                Log.d("STATE: ", "" + newState);
        }


    }

    public void send() {
        progress.set(View.VISIBLE);
        byte[] data = new byte[]{-77,116,86,120,-1,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,18};
        mBlueUtils.sendCommand(data);

    }
}
