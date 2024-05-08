package com.example.hlkb40_demo.utilInfo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.hlkb40_demo.BluetoothLeClass;
import com.example.hlkb40_demo.UUIDInfo;
import com.example.hlkb40_demo.WriterOperation;
import com.example.hlkb40_demo.activity.DeviceScanActivity;

// <参数，进度，结果>
public class GetOTAAddrTask extends AsyncTask<Object, String, String> {
    Handler mHandler;
    WriterOperation woperation;
    UUIDInfo selectWrite;
    Long iFileSize;
    int onePackageSize;
    public Boolean isResultCallBack;
    public byte[] recvValue = null;
    int startAddr = 0;

    // 准备开始（可更新UI）
    protected void onPreExecute() {
        Log.e("GetOTAAddrTask", "onPreExecute -> 准备擦除地址！");
        super.onPreExecute();
    }

    // 耗时操作
    protected String doInBackground(Object... objects) {
        mHandler = (Handler) objects[0];
        woperation = (WriterOperation) objects[1];
        selectWrite = (UUIDInfo) objects[2];
        iFileSize = (Long) objects[3];

        // 先获取起始地址
        Log.e("GetOTAAddrTask","doInBackground,查询起始地址？");
        isResultCallBack = false;
        woperation.send_data(WriterOperation.OTA_CMD_GET_STR_BASE, 0, null, 0,
                selectWrite.getBluetoothGattCharacteristic(), DeviceScanActivity.getInstance().mBLE);
        while (!isResultCallBack) {
            Log.e("GetOTAAddrTask","doInBackground,等待回复起始地址...");
        }
        startAddr = woperation.bytetoint(recvValue);
        Log.e("GetOTAAddrTask","得到升级的起始地址："+startAddr);
        onProgressUpdate("得到升级的起始地址："+startAddr);
        // 按照上面的起始地址和文件大小，计算具体需要发多少数据，并擦除模块对应的升级内存空间
        page_erase(startAddr, iFileSize, selectWrite.getBluetoothGattCharacteristic(), DeviceScanActivity.getInstance().mBLE);
        Log.e("GetOTAAddrTask","位置擦除完成，准备发送固件！");
        Message msgUpdate = new Message();
        msgUpdate.what = 333;
        msgUpdate.obj = "位置擦除完成，准备发送固件！";
        mHandler.sendMessage(msgUpdate);


        return null;
    }

    private int page_erase(int addr, long length, BluetoothGattCharacteristic mgattCharacteristic, BluetoothLeClass bleclass) {
        int addr0 = addr;
        long count = length / 0x1000;
        if ((length % 0x1000) != 0) {
            count++;
        }
        Log.e("page_erase","该升级文件，需要擦除："+count+" 次空间，每次0x1000个长度");
        for (int i = 0; i < count; i++) {
            isResultCallBack = false;
            woperation.send_data(WriterOperation.OTA_CMD_PAGE_ERASE, addr0, null, 0,
                    mgattCharacteristic, bleclass);
            while (!isResultCallBack) {
                Log.e("page_erase","等待擦除位置的回复...");
            }
            Log.e("page_erase","addr:"+addr0);
            addr0 += 0x1000;
        }
        return 0;
    }


    // 当前进度（由耗时操作传递）（可更新UI）
    protected void onProgressUpdate(String strMsg) {
        super.onProgressUpdate(strMsg);
        Message msgUpdate = new Message();
        msgUpdate.what = 333;
        msgUpdate.obj = strMsg;
        mHandler.sendMessage(msgUpdate);
    }

    // 操作完成（可更新UI）
    protected void onPostExecute(String strResult) {
        super.onPostExecute(strResult);
        Message msgUpdate = new Message();
        msgUpdate.what = 444;
        msgUpdate.obj = startAddr;
        mHandler.sendMessage(msgUpdate);
    }

    // 取消回调（可更新UI）
    protected void onCancelled() {
        super.onCancelled();
    }





}
