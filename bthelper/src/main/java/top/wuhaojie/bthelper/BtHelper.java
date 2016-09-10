package top.wuhaojie.bthelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wuhaojie on 2016/9/7 18:57.
 */
public class BtHelper {

    public static final String DEVICE_HAS_NOT_BLUETOOTH_MODULE = "device has not bluetooth module!";
    public static final String STR_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private Context mContext;

    //    get bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private Receiver mReceiver = new Receiver();

    private List<BluetoothDevice> mBondedList = new ArrayList<>();
    private List<BluetoothDevice> mNewList = new ArrayList<>();


    private OnSearchDeviceListener mOnSearchDeviceListener;

    private static volatile BtHelper sBtHelper;
    private boolean mNeed2unRegister;
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private InputStream mAcceptInputStream;
    private OutputStream mAcceptOutputStream;

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


    public void requestEnableBt() {
        if (mBluetoothAdapter == null) {
            throw new NullPointerException(DEVICE_HAS_NOT_BLUETOOTH_MODULE);
        }
        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }


    public void searchDevices(OnSearchDeviceListener listener) {

        checkNotNull(listener);
        if (mBondedList == null) mBondedList = new ArrayList<>();
        if (mNewList == null) mNewList = new ArrayList<>();

        mOnSearchDeviceListener = listener;

        if (mBluetoothAdapter == null) {
            mOnSearchDeviceListener.onError(new NullPointerException(DEVICE_HAS_NOT_BLUETOOTH_MODULE));
            return;
        }

        // ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);

        // ACTION_DISCOVERY_FINISHED
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);

        mNeed2unRegister = true;

        mBondedList.clear();
        mNewList.clear();

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();

        if (mOnSearchDeviceListener != null)
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

    private Queue<MessageItem> mMessageQueue = new LinkedBlockingQueue<>();


    public void sendMessage(BluetoothDevice device, MessageItem item, OnSendMessageListener listener) {
        // TODO: 2016/9/9
        connectDevice(device.getAddress(), listener);
        mMessageQueue.add(item);
        WriteRunnable writeRunnable = new WriteRunnable(listener);
        mExecutorService.submit(writeRunnable);
    }


    public void receiveMessage(OnReceiveMessageListener listener) {
        if (mBluetoothAdapter == null) {
            listener.onError(new RuntimeException(DEVICE_HAS_NOT_BLUETOOTH_MODULE));
            return;
        }

        AcceptRunnable acceptRunnable = new AcceptRunnable(listener);
        mExecutorService.submit(acceptRunnable);

//        ReadRunnable readRunnable = new ReadRunnable(listener, false);
//        mExecutorService.submit(readRunnable);
    }

    private class AcceptRunnable implements Runnable {

        private OnReceiveMessageListener mListener;

        public AcceptRunnable(OnReceiveMessageListener listener) {
            mListener = listener;
        }

        @Override
        public void run() {
            try {
                BluetoothServerSocket socket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BT", UUID.fromString(STR_UUID));
                BluetoothSocket accept = socket.accept();
                accept.connect();
                mAcceptInputStream = accept.getInputStream();
                mAcceptOutputStream = accept.getOutputStream();
                ReadRunnable readRunnable = new ReadRunnable(mListener, true);
                mExecutorService.submit(readRunnable);
            } catch (IOException e) {
                mListener.onError(e);
            }
        }
    }


    private volatile boolean mWritable = true;

    private class WriteRunnable implements Runnable {

        private OnSendMessageListener listener;

        public WriteRunnable(OnSendMessageListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            while (mOutputStream != null && mWritable) ;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mOutputStream));

            while (mWritable) {
                MessageItem item = mMessageQueue.poll();

                if (item.mTYPE == MessageItem.TYPE.STRING) {
                    try {
                        writer.write(item.text);
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        listener.onConnectionLost(e);
                        break;
                    }

                } else if (item.mTYPE == MessageItem.TYPE.CHAR) {
                    try {
                        writer.write(item.data);
                        writer.flush();
                    } catch (IOException e) {
                        listener.onConnectionLost(e);
                        break;
                    }
                }
            }

        }
    }


    private volatile boolean mReadable = true;


    private class ReadRunnable implements Runnable {

        private OnReceiveMessageListener mListener;
        private boolean mAccept;

        public ReadRunnable(OnReceiveMessageListener listener, boolean accept) {
            mListener = listener;
            mAccept = accept;
        }

        @Override
        public void run() {
            InputStream stream;
            if (mAccept)
                stream = mAcceptInputStream;
            else
                stream = mInputStream;

            while (stream != null && mReadable) ;
            checkNotNull(stream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            while (mReadable) {
                try {
                    while (stream.available() == 0) ;
                } catch (IOException e) {
                    mListener.onConnectionLost(e);
                    break;
                }

                while (mReadable) {

                    try {
                        String s = reader.readLine();
                        mListener.onNewLine(s);
                    } catch (IOException e) {
                        mListener.onConnectionLost(e);
                        break;
                    }


                }

            }


        }


    }

    private void connectDevice(String mac, IErrorListener listener) {
        if (mac == null || TextUtils.isEmpty(mac))
            throw new IllegalArgumentException("mac address is null or empty!");
        if (!BluetoothAdapter.checkBluetoothAddress(mac))
            throw new IllegalArgumentException("mac address is not correct! make sure it's upper case!");

        ConnectDeviceRunnable connectDeviceRunnable = new ConnectDeviceRunnable(mac, listener);
        checkNotNull(mExecutorService);

        mExecutorService.submit(connectDeviceRunnable);

    }


    private class ConnectDeviceRunnable implements Runnable {
        private String mac;
        private IErrorListener listener;

        public ConnectDeviceRunnable(String mac, IErrorListener listener) {
            this.mac = mac;
            this.listener = listener;
        }

        @Override
        public void run() {
            // always return a remote device
            BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mac);

            try {
                BluetoothSocket socket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(STR_UUID));
                socket.connect();
                mInputStream = socket.getInputStream();
                mOutputStream = socket.getOutputStream();
            } catch (IOException e) {
                listener.onError(e);
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

        // unregister
        if (mNeed2unRegister)
            mContext.unregisterReceiver(mReceiver);

        mOnSearchDeviceListener = null;

        mNewList = null;
        mBondedList = null;

        mReceiver = null;

        sBtHelper = null;
    }

}
