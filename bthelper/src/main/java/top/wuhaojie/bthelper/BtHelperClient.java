package top.wuhaojie.bthelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
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
public class BtHelperClient {

    public static final String DEVICE_HAS_NOT_BLUETOOTH_MODULE = "device has not bluetooth module!";
    //    public static final String STR_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String TAG = "BtHelperClient";
    public static final int HANDLER_WHAT_NEW_MSG = 1;
    public static final int DEFAULT_BUFFER_SIZE = 256;
    public static final int HANDLER_WHAT_NEW_RESPONSE = 2;
    private Context mContext;
    private BluetoothSocket mSocket;

    private enum STATUS {
        DISCOVERING,
        CONNECTED,
        FREE
    }

    private volatile STATUS mCurrStatus = STATUS.FREE;


    //    get bluetooth adapter
//    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothAdapter mBluetoothAdapter;

    private volatile Receiver mReceiver = new Receiver();

    private List<BluetoothDevice> mBondedList = new ArrayList<>();
    private List<BluetoothDevice> mNewList = new ArrayList<>();


    private OnSearchDeviceListener mOnSearchDeviceListener;

    private static volatile BtHelperClient sBtHelperClient;
    private boolean mNeed2unRegister;
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private InputStream mAcceptInputStream;
    private OutputStream mAcceptOutputStream;

    public static BtHelperClient getInstance(Context context) {
        if (sBtHelperClient == null) {
            synchronized (BtHelperClient.class) {
                if (sBtHelperClient == null)
                    sBtHelperClient = new BtHelperClient(context);
            }
        }
        return sBtHelperClient;
    }

    private BtHelperClient(Context context) {
        mContext = context.getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

        if (mReceiver == null) mReceiver = new Receiver();

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

    public void sendMessage(String mac, MessageItem item, OnSendMessageListener listener) {
        sendMessage(mac, item, listener, false);
    }

//    public void sendMessage(BluetoothDevice device, MessageItem item, OnSendMessageListener listener, boolean needResponse) {
//        sendMessage(device.getAddress(), item, listener, needResponse);
//    }

    public void sendMessage(String mac, MessageItem item, OnSendMessageListener listener, boolean needResponse) {
        // if not connected
        if (mCurrStatus != STATUS.CONNECTED)
            connectDevice(mac, listener);
        mMessageQueue.add(item);
        WriteRunnable writeRunnable = new WriteRunnable(listener, needResponse);
        mExecutorService.submit(writeRunnable);
    }


    public void receiveMessage(OnReceiveMessageListener listener) {
        if (mBluetoothAdapter == null) {
            listener.onError(new RuntimeException(DEVICE_HAS_NOT_BLUETOOTH_MODULE));
            return;
        }
        ReadRunnable_ readRunnable = new ReadRunnable_(listener);
        mExecutorService.submit(readRunnable);
    }

//    public void listenMessage(OnReceiveMessageListener listener) {
//        if (mBluetoothAdapter == null) {
//            listener.onError(new RuntimeException(DEVICE_HAS_NOT_BLUETOOTH_MODULE));
//            return;
//        }
//        AcceptRunnable acceptRunnable = new AcceptRunnable(listener);
//        mExecutorService.submit(acceptRunnable);
//    }
//
//
//    private class AcceptRunnable implements Runnable {
//
//        private OnReceiveMessageListener mListener;
//
//        public AcceptRunnable(OnReceiveMessageListener listener) {
//            mListener = listener;
//        }
//
//        @Override
//        public void run() {
//            try {
//                BluetoothServerSocket socket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BT", UUID.fromString(Constants.STR_UUID));
////                BluetoothServerSocket socket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BT", UUID.fromString(STR_UUID));
//                Log.d(TAG, "开始监听");
//                BluetoothSocket accept = socket.accept();
//                accept.connect();
//                Log.d(TAG, "开始连接");
//                mAcceptInputStream = accept.getInputStream();
//                mAcceptOutputStream = accept.getOutputStream();
//                // ----- CONNECTED -----
//                mCurrStatus = STATUS.CONNECTED;
//                ReadRunnable readRunnable = new ReadRunnable(mListener);
//                mExecutorService.submit(readRunnable);
//            } catch (IOException e) {
//                mListener.onError(e);
//            }
//        }
//    }


    private volatile boolean mWritable = true;

    private class WriteRunnable implements Runnable {

        private OnSendMessageListener listener;
        private boolean needResponse;


        private Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLER_WHAT_NEW_RESPONSE:
                        String s = (String) msg.obj;
                        listener.onSuccess(s);
                        break;
                }
            }
        };


        public WriteRunnable(OnSendMessageListener listener, boolean needResponse) {
            this.listener = listener;
            this.needResponse = needResponse;
        }

        @Override
        public void run() {
            mWritable = true;
//            Log.d(TAG, "准备写入");
//            while (mOutputStream != null && mWritable) ;
            // 并且要写入线程未被取消
            while (mCurrStatus != STATUS.CONNECTED && mWritable) ;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mOutputStream));
//            BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream));
//            Log.d(TAG, "开始写入");
            Message message = new Message();
            message.what = HANDLER_WHAT_NEW_RESPONSE;

            while (mWritable) {
                MessageItem item = mMessageQueue.poll();

                if (item.mTYPE == MessageItem.TYPE.STRING) {
                    try {
                        writer.write(item.text);
                        writer.newLine();
                        writer.flush();
                        Log.d(TAG, "send: " + item.text);
                    } catch (IOException e) {
                        listener.onConnectionLost(e);
                        mCurrStatus = STATUS.FREE;
                        break;
                    }

                } else if (item.mTYPE == MessageItem.TYPE.CHAR) {
                    try {
                        writer.write(item.data);
                        writer.flush();
                    } catch (IOException e) {
                        listener.onConnectionLost(e);
                        mCurrStatus = STATUS.FREE;
                        break;
                    }
                }

                // ----- Read For Response -----
                if (!needResponse) continue;
                try {
//                    String s = reader.readLine();
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    StringBuilder builder = new StringBuilder();

                    while (mInputStream.available() == 0) ;

                    while (true) {
                        int num = mInputStream.read(buffer);
                        String s = new String(buffer, 0, num);
                        builder.append(s);
                        if (mInputStream.available() == 0) break;

                    }
                    message.obj = builder.toString();
                    mHandler.sendMessage(message);

                } catch (IOException e) {
//                    e.printStackTrace();
                    listener.onConnectionLost(e);
                    mCurrStatus = STATUS.FREE;
                }

            }

        }
    }


    private volatile boolean mReadable = true;


//    private class ReadRunnable implements Runnable {
//
//        private OnReceiveMessageListener mListener;
//
//        public ReadRunnable(OnReceiveMessageListener listener) {
//            mListener = listener;
//        }
//
//        @Override
//        public void run() {
//            InputStream stream = mInputStream;
//
//            while (mCurrStatus != STATUS.CONNECTED && mReadable) ;
//            checkNotNull(stream);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//            while (mReadable) {
//                try {
//                    while (stream.available() == 0) ;
//                } catch (IOException e) {
//                    mListener.onConnectionLost(e);
//                    mCurrStatus = STATUS.FREE;
//                    break;
//                }
//
//                while (mReadable) {
//
//                    try {
//                        String s = reader.readLine();
//                        mListener.onNewLine(s);
//                    } catch (IOException e) {
//                        mListener.onConnectionLost(e);
//                        mCurrStatus = STATUS.FREE;
//                        break;
//                    }
//
//
//                }
//
//            }
//
//
//        }
//
//
//    }

    private class ReadRunnable_ implements Runnable {

        private OnReceiveMessageListener mListener;

        public ReadRunnable_(OnReceiveMessageListener listener) {
            mListener = listener;
        }

        private Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLER_WHAT_NEW_MSG:
                        String s = (String) msg.obj;
                        mListener.onNewLine(s);
                        break;
                }
            }
        };

        @Override
        public void run() {
            mReadable = true;
            InputStream stream = mInputStream;

            while (mCurrStatus != STATUS.CONNECTED && mReadable) ;
            checkNotNull(stream);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            StringBuilder builder = new StringBuilder();
            Message message = new Message();
            message.what = HANDLER_WHAT_NEW_MSG;
            int n = 0;
//            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            while (mReadable) {

                try {
                    while (stream.available() == 0) ;

                    while (mReadable) {
                        int num = stream.read(buffer);
                        n = 0;
                        String s = new String(buffer, 0, num);
                        builder.append(s);
                        if (stream.available() == 0) break;

                    }
                    message.obj = builder.toString();
                    mHandler.sendMessage(message);


                } catch (IOException e) {
                    mListener.onConnectionLost(e);
                    mCurrStatus = STATUS.FREE;
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
            mBluetoothAdapter.cancelDiscovery();
            mCurrStatus = STATUS.FREE;
            try {
                Log.d(TAG, "准备连接: " + remoteDevice.getAddress() + remoteDevice.getName());
//                BluetoothSocket socket =  remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(STR_UUID));
//                BluetoothSocket socket =  (BluetoothSocket) remoteDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(remoteDevice,1);
                mSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(Constants.STR_UUID));
//                if(!socket.isConnected())
                mSocket.connect();
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                mCurrStatus = STATUS.CONNECTED;
            } catch (Exception e) {
                listener.onError(e);
                try {
                    mInputStream.close();
                    mOutputStream.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                mCurrStatus = STATUS.FREE;
            }
        }
    }

    private void checkNotNull(Object o) {
        if (o == null)
            throw new NullPointerException();
    }

    public void close() {
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();

        // unregister
        if (mNeed2unRegister) {
            mContext.unregisterReceiver(mReceiver);
            mNeed2unRegister = !mNeed2unRegister;
        }

        mWritable = false;
        mReadable = false;

        if (mSocket != null) try {
            mSocket.close();
        } catch (IOException e) {
            mSocket = null;
        }

//        mOnSearchDeviceListener = null;

        mNewList = null;
        mBondedList = null;

        mReceiver = null;

        sBtHelperClient = null;
        mCurrStatus = STATUS.FREE;
    }

}
