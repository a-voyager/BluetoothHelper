# 蓝牙操作库

[中文](https://github.com/a-voyager/BluetoothHelper/blob/master/README_zh.md) | [English](https://github.com/a-voyager/BluetoothHelper/blob/master/README.md)

这是一个能够让你应用的蓝牙操作(特别是手机通过蓝牙控制物联网智能硬件设备)变得 **如此简洁**!

![image](https://github.com/a-voyager/BluetoothHelper/raw/master/imgs/ble_icon.png)

> 感兴趣的话就别忘记给个Start哦 :）

## 特点
 - 封装了常用的蓝牙相关操作, 将它们与Activity解耦, 比如搜索蓝牙设备等.

  ```java
  btHelperClient.searchDevices(listener);
  ```
 - 重写IO流操作, 将蓝牙发送指令操作包装得像HTTP一样, 你可以直接将指令发送出去(不需要考虑设备是否连接问题, 尤其是智能硬件设备)然后像HTTP一样获得响应码和返回体.

  ```java
  btHelperClient.sendMessage("20:15:03:18:08:63", item, true, new OnSendMessageListener() {

          @Override
          public void onSuccess(int status, String response) {
          }

          ...

  });
  ```

## 依赖
有两种方式可供选择:

 - 克隆该项目，并设置为依赖
 - 只需要将下面的脚本加入到 build.gradle:

 ```groovy
    // 把这个加入到项目根 build.gradle
 	allprojects {
 		repositories {
 			...
 			maven { url "https://jitpack.io" }
 		}
 	}
 	// 这个加入到应用module的 build.gradle
	dependencies {
	    compile 'com.github.a-voyager:BluetoothHelper:f71d40a98b'
	}
 ```

## 用法
 - 获取实例
 使用Context来进行初始化
 ```java
 btHelperClient = BtHelperClient.from(MainActivity.this);
 ```

 - 向远程蓝牙设备发送指令
 异步发送消息，并在主线程回调。
 参数含义: 设备的Mac地址, 消息对象, 是否需要获取响应信息, 监听器.
 ```java
 MessageItem item = new MessageItem("Hello");

 btHelperClient.sendMessage("20:15:03:18:08:63", item, true, new OnSendMessageListener() {

         @Override
         public void onSuccess(int status, String response) {
            // 当发送成功，同时获得响应体时回调

            // 状态码:   描述响应是否正确.
            //           1代表响应回复内容正确, -1代表响应内容不正确, 即数据损坏
            // 响应信息: 来自远程蓝牙设备的响应内容, 可以通过response.getBytes()获取字节数组

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
 ```

 - 关闭连接
 只需要调用 close() 方法
 ```java
     @Override
     protected void onDestroy() {
         super.onDestroy();

         btHelperClient.close();

     }
 ```


## 更多程序接口
- 搜索蓝牙设备
  搜索蓝牙设备调用 searchDevices() 方法即可

 ```java
 btHelperClient.searchDevices(new OnSearchDeviceListener() {

         @Override
         public void onStartDiscovery() {
             // 在进行搜索前回调

             Log.d(TAG, "onStartDiscovery()");

         }

         @Override
         public void onNewDeviceFound(BluetoothDevice device) {
             // 当寻找到一个新设备时回调

             Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());

         }

         @Override
         public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
             // 当搜索蓝牙设备完成后回调

             Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
             Log.d(TAG, "SearchCompleted: newList" + newList.toString());

         }

         @Override
         public void onError(Exception e) {

             e.printStackTrace();

         }

 });
 ```

- 设置过滤器
 使用过滤器来过滤掉那些硬件设备出现差错的数据
 ```java
 btHelperClient.setFilter(new Filter() {

        @Override
        public boolean isCorrect(String response) {
            return response.trim().length() >= 5;
        }

 });
 ```

## 待更新列表
 - 纯监听数据模式
 - 蓝牙服务端模块


## License
    The MIT License (MIT)

    Copyright (c) 2015 WuHaojie

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.


