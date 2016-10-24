# Bluetooth Helper Library

[中文](https://github.com/a-voyager/BluetoothHelper/blob/master/README_zh.md) | [English](https://github.com/a-voyager/BluetoothHelper/blob/master/README.md)

A library makes your app's bluetooth operation(for Smart Iot Hardware especially) **so easy**!

![image](https://github.com/a-voyager/BluetoothHelper/raw/master/imgs/ble_icon.png)

> Don't forget give me a star :）

## Feature
 - Packaged common bluetooth operations, such as device discovery, connect the remote device and more.

  ```java
  btHelperClient.searchDevices(listener);
  ```
 - Sending messages is such as Http, you send a message to the remote device(not need to connect device firstly, such as Smart Iot Hardware) then you could get a response and a status code.

  ```java
  btHelperClient.sendMessage("20:15:03:18:08:63", item, true, new OnSendMessageListener() {

          @Override
          public void onSuccess(int status, String response) {
          }

          ...

  });
  ```

## Dependency
There are two ways:

 - clone this project, and use as dependency
 - just add following code to you build.gradle:

 ```groovy
    // Add it in your root build.gradle at the end of repositories
 	allprojects {
 		repositories {
 			...
 			maven { url "https://jitpack.io" }
 		}
 	}
 	// Add the dependency
	dependencies {
	    compile 'com.github.a-voyager:BluetoothHelper:f71d40a98b'
	}
 ```

## Usage
 - Get the instance
 use a Context object to initialize
 ```java
 btHelperClient = BtHelperClient.from(MainActivity.this);
 ```

 - Send message to the remote device
 send message asynchronously, callback in main thread.
 parameters means: device's mac address, message object, if need to obtain response, SendMessageListener.
 ```java
 MessageItem item = new MessageItem("Hello");

 btHelperClient.sendMessage("20:15:03:18:08:63", item, true, new OnSendMessageListener() {

         @Override
         public void onSuccess(int status, String response) {
            // Call when send a message succeed, and get a response from the remote device

            // status:   the status describes ok or error.
            //           1 respect the response is valid, -1 respect the response is invalid
            // response: the response from the remote device, you can call response.getBytes() to get char[]

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

 - Close connection
 just call close() method
 ```java
     @Override
     protected void onDestroy() {
         super.onDestroy();

         btHelperClient.close();

     }
 ```


## More API
- Search devices
  search devices with just calling searchDevices() method

 ```java
 btHelperClient.searchDevices(new OnSearchDeviceListener() {

         @Override
         public void onStartDiscovery() {
             // Call before discovery devices

             Log.d(TAG, "onStartDiscovery()");

         }

         @Override
         public void onNewDeviceFound(BluetoothDevice device) {
             // Call when found a new device

             Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());

         }

         @Override
         public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
             // Call when the discovery process completed

             Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
             Log.d(TAG, "SearchCompleted: newList" + newList.toString());

         }

         @Override
         public void onError(Exception e) {

             e.printStackTrace();

         }

 });
 ```

- Set filter
 use a filter to check if a given response is an expect data.
 ```java
 btHelperClient.setFilter(new Filter() {

        @Override
        public boolean isCorrect(String response) {
            return response.trim().length() >= 5;
        }

 });
 ```

## To-Do List
 - Listening data mode
 - BtHelperServer


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


