package com.example.hlkb40_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.view.LoadingDialog;

public class BaseActivity extends Activity {
    protected Context mContext;
    protected Handler mHandler;

    // OTA的相关服务与特征
    protected String strOTA_Server = "02f00000-0000-0000-0000-00000000fe00";
    protected String strOTA_Read = "02f00000-0000-0000-0000-00000000ff02";
    protected String strOTA_Write = "02f00000-0000-0000-0000-00000000ff01";

    // 参数设置的相关服务与特征
    protected String strSET_Server = "02f00000-0000-0000-0000-00000000fe00";
    protected String strVERSION_Read = "02f00000-0000-0000-0000-00000000ff03";
    protected String strSET_Read = "02f00000-0000-0000-0000-00000000ff04";
    protected String strSET_Write = "02f00000-0000-0000-0000-00000000ff04";

    protected String strCHECK_Read = "02f00000-0000-0000-0000-00000000ff05";
    protected String strCHECK_Write = "02f00000-0000-0000-0000-00000000ff05";

    // 透传数据的相关服务与特征
    protected String strSerial_Server = "0000fff0-0000-1000-8000-00805f9b34fb";
    protected String strSerial_Read = "0000fff1-0000-1000-8000-00805f9b34fb";
    protected String strSerial_Write = "0000fff2-0000-1000-8000-00805f9b34fb";


    // 接收通知数据回调
    protected String BC_RecvData = "BC_RecvData";
    // 写数据回调
    protected String BC_WriteData = "BC_WriteData";
    // 读通道回调
    protected String BC_ReadData = "BC_ReadData";
    // MTU改变的回调
    protected String BC_ChangeMTU = "BC_ChangeMTU";
    // 连接状态变化的回调
    protected String BC_ConnectStatus = "BC_ConnectStatus";

    protected LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mContext = this;
        mHandler = new Handler();
        loadingDialog = new LoadingDialog(mContext, R.style.dialog);

    }

    protected void showToast(final String strMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext,strMsg,Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                hideKeyboard(ev, view, BaseActivity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据传入控件的坐标和用户的焦点坐标，判断是否隐藏键盘，如果点击的位置在控件内，则不隐藏键盘
     *
     * @param view
     *            控件view
     * @param event
     *            焦点位置
     * @return 是否隐藏
     */
    public static void hideKeyboard(MotionEvent event, View view,Activity activity) {
        try {
            if (view != null && view instanceof EditText) {
                int[] location = {0, 0};
                view.getLocationInWindow(location);
                int left = location[0], top = location[1], right = left
                        + view.getWidth(), bootom = top + view.getHeight();
                // 判断焦点位置坐标是否在空间内，如果位置在控件外，则隐藏键盘
                if (event.getRawX() < left || event.getRawX() > right
                        || event.getY() < top || event.getRawY() > bootom) {
                    // 隐藏键盘
                    IBinder token = view.getWindowToken();
                    InputMethodManager inputMethodManager = (InputMethodManager) activity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(token,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
