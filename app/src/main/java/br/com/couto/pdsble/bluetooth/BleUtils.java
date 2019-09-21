package br.com.couto.pdsble.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * SINGLETON PATTERN
 * */

public class BleUtils {

    public static final String REQUEST_ENABLE_BLE = "ACTION.ENABLE.BLE";

    private static final String TAG = BleUtils.class.getSimpleName();

    public static final String UUID_SERVICE_1 = "ed0ef62e-9b0d-11e4-89d3-123b93f75cba";

    public static final String CHARACT_SERVICE_1 = "ed0efb1a-9b0d-11e4-89d3-123b93f75cba";


    public static final Integer CODE_REQUEST_ENABLE_BLUETOOTH = 287;
    private static final Long LONG_SCAN_PERIOD = 2000L;


    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothManager mBluetoothManager;
    private static BleUtils mBleUtils;
    private boolean isScanning;
    private List<ScanResult> scanResults;
    private BluetoothGatt mBluetoothGatt;
    private BleInterface mBleInterface;

    public static BleUtils getInstance(Context context){
        if(mBleUtils == null){
            mBleUtils = new BleUtils(context);
        }
        return mBleUtils;
    }

    private BleUtils(Context context) {
        if(mBluetoothAdapter == null || mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    public boolean bluetoothIsEnable(){
        boolean isEnable = false;
        if(mBluetoothAdapter != null){
            if(mBluetoothAdapter.isEnabled())
                isEnable = true;
        }
        return isEnable;
    }

    public void startScan(){
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            scanResults = new ArrayList<>();
            ScanSettings scanSettings = getScanSettings();
            List<ScanFilter> scanFilters = new ArrayList<>();
            isScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, mScanCallback);
            new Handler().postDelayed(() -> {
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                        if (mBleInterface != null) {
                            mBleInterface.onFinishScan();
                        }
                        isScanning = false;

                    }
                    , LONG_SCAN_PERIOD);
        }
    }


    private ScanSettings getScanSettings (){
        return new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }

    public void setmBleListener(BleInterface bleListener){
        this.mBleInterface = bleListener;
    }

    public void connectBle(BluetoothDevice device,Context context,String bleMac){
        if(device == null){
            mBluetoothGatt = getDeviceByMac(bleMac).connectGatt(context,false,bluetoothGattCallback);
        }else{
            mBluetoothGatt = device.connectGatt(context,false,bluetoothGattCallback);
        }
    }

    public boolean isConnected (){
        boolean isConnect = false;
        if(mBluetoothGatt != null){
            List<BluetoothDevice> connect = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
            for(BluetoothDevice aux: connect){
                if(aux.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
                    isConnect= true;
            }
        }
        return isConnect;
    }

    public void disconnect(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
        }
    }

    public void sendCommand(byte [] data){
        if(mBluetoothGatt!= null && isConnected()){
            BluetoothGattService gattService= mBluetoothGatt.getService(UUID.fromString(UUID_SERVICE_1));
            BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(CHARACT_SERVICE_1));
            characteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }

    }
    public void readCommand(){

    }

    public boolean isScaning(){
        return isScanning;
    }

    private BluetoothDevice getDeviceByMac(String bleMac){
        return mBluetoothAdapter.getRemoteDevice(bleMac);
    }


    /**
     * SCAN CALLBACK
     * */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            boolean isMapping = false;
            for(ScanResult aux: scanResults){
                if(aux.getDevice().getAddress().equals(result.getDevice().getAddress()))
                {
                    isMapping = true;
                    break;
                }
            }
            if(!isMapping){
                scanResults.add(result);
                Log.d("RESULT:",result.getDevice().getAddress());
                if(mBleInterface != null){
                    mBleInterface.onResultScan(result);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                mBluetoothGatt = gatt;
                sendCommand(new byte[]{3});
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("Response: ",characteristic.getValue().toString());
            if(mBleInterface != null) mBleInterface.onResponse(characteristic.getValue());
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mBluetoothGatt = gatt;
            if(mBleInterface!=null) mBleInterface.onConectionStatus(newState);
            switch (newState){
                case STATE_CONNECTED:
                {
                    Log.d(TAG,"CONNECT: "+gatt.getDevice().getAddress());
                    mBluetoothGatt.discoverServices();

                }
                break;
                case STATE_CONNECTING:
                {
                    Log.d(TAG,"CONNECTING: "+gatt.getDevice().getAddress());

                }
                break;
                case STATE_DISCONNECTED:
                {
                    mBluetoothGatt = null;

                    Log.d(TAG,"DISCONNECT: "+gatt.getDevice().getAddress());


                }
                break;

                default:
                    Log.d(TAG,"CASE : "+ newState);
            }
        }
    };

}
