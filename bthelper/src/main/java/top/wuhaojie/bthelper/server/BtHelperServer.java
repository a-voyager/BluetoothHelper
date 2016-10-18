package top.wuhaojie.bthelper.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

import top.wuhaojie.bthelper.Constants;
import top.wuhaojie.bthelper.OnReceiveMessageListener;

/**
 * Created by wuhaojie on 2016/10/18 13:57.
 */

 class BtHelperServer {

    private final BluetoothAdapter mBluetoothAdapter;

    public BtHelperServer(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    // -------- DEBUG --------
    private final OnReceiveMessageListener mOnReceiveMessageListener = new OnReceiveMessageListener() {
        @Override
        public void onNewLine(String s) {

        }

        @Override
        public void onConnectionLost(Exception e) {

        }

        @Override
        public void onError(Exception e) {

        }
    };



    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mServerSocket;


        public AcceptThread(BluetoothAdapter adapter) {
            BluetoothServerSocket temp = null;
            try {
                temp = adapter.listenUsingRfcommWithServiceRecord("BT", UUID.fromString(Constants.STR_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = temp;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                if (socket != null) {

                    readAndWriteData(socket);

                    try {
                        mServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    break;
                }
            }


        }

        private void readAndWriteData(BluetoothSocket socket) {

        }


        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
