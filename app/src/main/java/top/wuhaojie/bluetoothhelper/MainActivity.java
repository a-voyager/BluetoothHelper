package top.wuhaojie.bluetoothhelper;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import top.wuhaojie.bthelper.BtHelper;
import top.wuhaojie.bthelper.MessageItem;
import top.wuhaojie.bthelper.OnReceiveMessageListener;
import top.wuhaojie.bthelper.OnSearchDeviceListener;
import top.wuhaojie.bthelper.OnSendMessageListener;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private BtHelper mBtHelper;
    private BluetoothDevice mRemoteDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtHelper = BtHelper.getInstance(MainActivity.this);

        mBtHelper.requestEnableBt();

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mBtHelper.searchDevices(new OnSearchDeviceListener() {
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
                        Log.d(TAG, "SearchCompleted: " + bondedList.toString());
                        Log.d(TAG, "SearchCompleted: " + newList.toString());
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

                if (mRemoteDevice == null) {
                    Toast.makeText(MainActivity.this, "未发现可连接设备", Toast.LENGTH_SHORT).show();
                    return;
                }

                MessageItem item = new MessageItem("哈哈");
                mBtHelper.sendMessage(mRemoteDevice, item, new OnSendMessageListener() {
                    @Override
                    public void onConnectionLost(Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        });


        findViewById(R.id.btn_receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtHelper.receiveMessage(new OnReceiveMessageListener() {
                    @Override
                    public void onNewLine(String s) {
                        Log.d(TAG, s);
                    }

                    @Override
                    public void onConnectionLost(Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBtHelper.dispose();
    }
}
