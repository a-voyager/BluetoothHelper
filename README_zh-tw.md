# 藍芽操作套件

[繁體中文](https://github.com/a-voyager/BluetoothHelper/blob/master/README_zh-tw.md) | [简体中文](https://github.com/a-voyager/BluetoothHelper/blob/master/README_zh.md) | [English](https://github.com/a-voyager/BluetoothHelper/blob/master/README.md)

這是一個能讓你 App 操作藍芽（特別是手機透過藍芽控制物聯網設備）變得 **如此簡潔** 的套件！

![image](https://github.com/a-voyager/BluetoothHelper/raw/master/imgs/ble_icon.png)

> 感興趣的話別忘了給個 Star 哦 :）

## 特色
 - 封裝了常見的藍芽相關操作，例如搜尋藍芽設備等。

  ```java
  btHelperClient.searchDevices(listener);
  ```
 - 重寫 IO 操作，將藍芽發送指令包裝得像 HTTP，你可以直接將指令發送出去（不需考慮設備是否連結的問題，尤其是智能設備）然後像 HTTP 一樣獲得狀態碼以及返回值。

  ```java
  btHelperClient.sendMessage("20:15:03:18:08:63", item, true, new OnSendMessageListener() {

          @Override
          public void onSuccess(int status, String response) {
          }

          ...

  });
  ```

## 調用方法
有兩種方式可供選擇：

 - Clone 此專案並設置 dependency
 - 只需要將下面 Scripts 加入至 build.gradle：

 ```groovy
    // 把這個加入到專案的 build.gradle
 	allprojects {
 		repositories {
 			...
 			maven { url "https://jitpack.io" }
 		}
 	}
 	// 這個加入到 App 的 build.gradle
	dependencies {
	    compile 'com.github.a-voyager:BluetoothHelper:f71d40a98b'
	}
 ```

## 用法
 - 抓取範例
 使用 Context 來進行初始化
 ```java
 btHelperClient = BtHelperClient.from(MainActivity.this);
 ```

 - 向藍芽設備發送指令
 異步發送訊息，並在主執行緒回傳。
 參數含意：設備的 Mac 地址，訊息對象，是否需要抓取回傳訊息，監聽器。
 ```java
 MessageItem item = new MessageItem("Hello");

 btHelperClient.sendMessage("20:15:03:18:08:63", item, true, new OnSendMessageListener() {

         @Override
         public void onSuccess(int status, String response) {
            // 當發送成功，同時獲得回傳時呼叫

            // 狀態碼：　描述回傳是否正確。
            //           1 代表回傳內容正確，-1 代表回傳內容不正確，即數據損壞
            // 回傳內容: 來自藍芽設備的回傳内容，可以透過 response.getBytes() 來抓取字節組

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

 - 關閉連結
 只需要調用 close() 方法
 ```java
     @Override
     protected void onDestroy() {
         super.onDestroy();

         btHelperClient.close();

     }
 ```


## 更多程式調用
- 搜尋藍芽設備
  搜尋藍芽設備調用 searchDevices() 即可

 ```java
 btHelperClient.searchDevices(new OnSearchDeviceListener() {

         @Override
         public void onStartDiscovery() {
             // 在進行搜尋前回傳

             Log.d(TAG, "onStartDiscovery()");

         }

         @Override
         public void onNewDeviceFound(BluetoothDevice device) {
             // 當尋找到一個新設備時回傳

             Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());

         }

         @Override
         public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
             // 當搜尋藍芽設備完成後回傳

             Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
             Log.d(TAG, "SearchCompleted: newList" + newList.toString());

         }

         @Override
         public void onError(Exception e) {

             e.printStackTrace();

         }

 });
 ```

- 設置過濾器
 使用過濾器來過濾掉那些硬體設備出現差錯的數據
 ```java
 btHelperClient.setFilter(new Filter() {

        @Override
        public boolean isCorrect(String response) {
            return response.trim().length() >= 5;
        }

 });
 ```

## 待更新列表
 - 純監聽數據模式
 - 藍芽服務端模組


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


