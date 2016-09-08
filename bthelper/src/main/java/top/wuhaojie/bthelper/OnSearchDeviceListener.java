package top.wuhaojie.bthelper;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by wuhaojie on 2016/9/8 14:50.
 */
public interface OnSearchDeviceListener {
    void onStartDiscovery();

    void onNewDeviceFounded(BluetoothDevice device);

    void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList);


    void onError(Exception e);
}
