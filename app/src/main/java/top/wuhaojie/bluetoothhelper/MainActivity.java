package top.wuhaojie.bluetoothhelper;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import top.wuhaojie.bthelper.BtHelperClient;
import top.wuhaojie.bthelper.MessageItem;
import top.wuhaojie.bthelper.OnSearchDeviceListener;
import top.wuhaojie.bthelper.OnSendMessageListener;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private BtHelperClient mBtHelperClient;
    private BluetoothDevice mRemoteDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtHelperClient = BtHelperClient.getInstance(MainActivity.this);

        mBtHelperClient.requestEnableBt();

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mBtHelperClient.searchDevices(new OnSearchDeviceListener() {
                    @Override
                    public void onStartDiscovery() {
                        Log.d(TAG, "onStartDiscovery()");
                    }

                    @Override
                    public void onNewDeviceFounded(BluetoothDevice device) {
                        Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());
                        mRemoteDevice = device;
                    }

                    @Override
                    public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
                        Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
                        Log.d(TAG, "SearchCompleted: newList" + newList.toString());
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });


            }
        });


        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MessageItem item = new MessageItem(new char[]{0xEF, 0x0F, 0xAB});
                mBtHelperClient.sendMessage("20:15:03:18:08:63", item, new OnSendMessageListener() {
                    @Override
                    public void onSuccess(String response) {
                        byte[] bytes = response.getBytes();
                        Log.d(TAG, response);
                        Log.d(TAG, Arrays.toString(bytes));
                        Log.d(TAG, bytesToHexString(bytes));
                        Toast.makeText(MainActivity.this, bytesToHexString(bytes), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionLost(Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }, true);

            }
        });

//        findViewById(R.id.btn_receive).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBtHelperClient.receiveMessage(new OnReceiveMessageListener() {
//                    @Override
//                    public void onNewLine(String s) {
//                        Log.d(TAG, s);
//                    }
//
//                    @Override
//                    public void onConnectionLost(Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//
//            }
//        });


        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtHelperClient.close();


            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBtHelperClient.close();
    }

    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

}
