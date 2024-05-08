package com.example.hlkb40_demo.utilInfo;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.hlkb40_demo.BluetoothLeClass;
import com.example.hlkb40_demo.UUIDInfo;
import com.example.hlkb40_demo.WriterOperation;
import com.example.hlkb40_demo.activity.DeviceScanActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// <参数，进度，结果>
public class SendOTAFileTask extends AsyncTask<Object, String, String> {
    Handler mHandler;
    WriterOperation woperation;
    UUIDInfo selectWrite;
    int onePackageSize;
    Long iMaxFileSize;
    File selectFile;
    public Boolean isResultCallBack;
    public byte[] recvValue = null;
    int startAddr = 0;
    FileInputStream isfile = null;
    BufferedInputStream input = null;
    int read_count;
    int lastReadCount = 0;
    int send_data_count = 0;
    float lastProgress = 0.0f;

    // 准备开始（可更新UI）
    protected void onPreExecute() {
        Log.e("SendOTAFileTask", "onPreExecute -> 准备发送文件！");
        super.onPreExecute();
    }

    // 耗时操作
    protected String doInBackground(Object... objects) {
        mHandler = (Handler) objects[0];
        woperation = (WriterOperation) objects[1];
        selectWrite = (UUIDInfo) objects[2];
        selectFile = (File) objects[3];
        startAddr = (Integer) objects[4];
        onePackageSize = (Integer) objects[5];
        iMaxFileSize = selectFile.length();

        try {
            isfile = new FileInputStream(selectFile);
            input = new BufferedInputStream(isfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            byte[] inputBuffer = new byte[onePackageSize];
            Log.e("SendOTAFileTask","doInBackground,开始发送文件，每次："+onePackageSize);
            // 最后开始发送升级文件
            while (((read_count = input.read(inputBuffer, 0, onePackageSize)) != -1)) {
                isResultCallBack = false;
                woperation.send_data(WriterOperation.OTA_CMD_WRITE_DATA, startAddr, inputBuffer,read_count,
                        selectWrite.getBluetoothGattCharacteristic(), DeviceScanActivity.getInstance().mBLE);
                while (!isResultCallBack) {
                    Log.e("SendOTAFileTask", "等待写，回复！");
                }
                startAddr += read_count;
                lastReadCount = read_count;
                send_data_count += read_count;
                // 更新升级进度%
                float writePrecent = (((float)send_data_count / iMaxFileSize) * 100);
                Log.e("SendOTAFileTask", "写入.."+writePrecent+"%");
                if ((writePrecent - lastProgress) > 2) {
                    lastProgress = writePrecent;
                    onProgressUpdate("已写入.."+lastProgress+"%");
                }
            }
            Log.e("SendOTAFileTask", "**********升级完成，重启设备**********");
            input.close();
            isfile.close();
            // 升级完成，则重启设备！
            woperation.send_data(WriterOperation.OTA_CMD_REBOOT, 0, null,
                    0, selectWrite.getBluetoothGattCharacteristic(), DeviceScanActivity.getInstance().mBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 当前进度（由耗时操作传递）（可更新UI）
    protected void onProgressUpdate(String strMsg) {
        super.onProgressUpdate(strMsg);
        Message msgUpdate = new Message();
        msgUpdate.what = 111;
        msgUpdate.obj = strMsg;
        mHandler.sendMessage(msgUpdate);
    }

    // 操作完成（可更新UI）
    protected void onPostExecute(String strResult) {
        super.onPostExecute(strResult);
        Message msgUpdate = new Message();
        msgUpdate.what = 222;
        msgUpdate.obj = "**********升级完成，重启设备**********";
        mHandler.sendMessage(msgUpdate);

    }

    // 取消回调（可更新UI）
    protected void onCancelled() {
        super.onCancelled();
    }





}
