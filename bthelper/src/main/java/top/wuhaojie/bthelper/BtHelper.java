package top.wuhaojie.bthelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaojie on 2016/9/7 18:57.
 */
public class BtHelper {

    private Context mContext;

    //    get bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private Receiver mReceiver = new Receiver();

    private List<BluetoothDevice> mBondedList = new ArrayList<>();
    private List<BluetoothDevice> mNewList = new ArrayList<>();


    private OnSearchDeviceListener mOnSearchDeviceListener;

    private static volatile BtHelper sBtHelper;

    public static BtHelper getInstance(Context context) {
        if (sBtHelper == null) {
            synchronized (BtHelper.class) {
                if (sBtHelper == null)
                    sBtHelper = new BtHelper(context);
            }
        }
        return sBtHelper;
    }

    private BtHelper(Context context) {
        mContext = context;
    }


    public void searchDevices(OnSearchDeviceListener listener) {

        checkNotNull(listener);

        mOnSearchDeviceListener = listener;

        if (mBluetoothAdapter == null) {
            mOnSearchDeviceListener.onError(new NullPointerException("device has not bluetooth!"));
            return;
        }

        // ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);

        // ACTION_DISCOVERY_FINISHED
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);


        mBondedList.clear();
        mNewList.clear();

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();

        mOnSearchDeviceListener.onStartDiscovery();

    }


    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mOnSearchDeviceListener != null)
                    mOnSearchDeviceListener.onNewDeviceFounded(device);

                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    mNewList.add(device);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mBondedList.add(device);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mOnSearchDeviceListener != null)
                    mOnSearchDeviceListener.onSearchCompleted(mBondedList, mNewList);
            }
        }
    }


    private void checkNotNull(Object o) {
        if (o == null)
            throw new NullPointerException();
    }

    public void dispose() {
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();

        mContext.unregisterReceiver(mReceiver);

        mOnSearchDeviceListener = null;

        mNewList = null;
        mBondedList = null;

        mReceiver = null;

        sBtHelper = null;
    }

}
