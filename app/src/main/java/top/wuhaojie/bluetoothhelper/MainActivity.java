package top.wuhaojie.bluetoothhelper;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;

import top.wuhaojie.bthelper.BtHelper;
import top.wuhaojie.bthelper.OnSearchDeviceListener;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private BtHelper mBtHelper;

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



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBtHelper.dispose();
    }
}
