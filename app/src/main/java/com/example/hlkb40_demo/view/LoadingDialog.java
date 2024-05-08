package com.example.hlkb40_demo.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hlkb40_demo.R;


public class LoadingDialog extends Dialog {

	private TextView mTextView;
	private Context mContext;
	private LoadingDialog load;
	private String strMsg = "";

	public LoadingDialog(Context context) {
		super(context);
		load = this;
		mContext = context;
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
		load = this;
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.loading_dialog);
		this.setCanceledOnTouchOutside(true);
		mTextView = (TextView) findViewById(R.id.loading_text);

		if (!strMsg.equals(""))
            mTextView.setText(strMsg);

		setCanceledOnTouchOutside(false);

	}

	public void showAndMsg(String text) {
        strMsg = text;
        if (!isShowing())
            show();

        if (mTextView != null)
            mTextView.setText(strMsg);
    }

	public void updateStatusText(String text) {
	    strMsg = text;
        if (mTextView != null)
            mTextView.setText(strMsg);
	}
	
	/**
	 *  设定超时显示
	 * @param iTimeOut 超时时间（秒）
	 */
	public void showAndTime(int iTimeOut) {
		super.show();
		this.iTimeOut = iTimeOut;
		handler.post(runnableTimeOut);
	}
	
	/** 隐藏并停止计时 */
	public void dismissAndTime() {
		handler.removeCallbacks(runnableTimeOut);
		this.dismiss();
	}
	
	private int iTimeOut = 0;
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case -999:
				Toast.makeText(mContext, "操作超时！", Toast.LENGTH_SHORT).show();
				load.dismiss();
				break;
			default:
				break;
			}
			
		}
	};

	Runnable runnableTimeOut = new Runnable() {
		public void run() {
			--iTimeOut;
			if (iTimeOut > 0) {
				handler.postDelayed(this, 1000);
			}
			else {
				handler.sendEmptyMessage(-999);
			}
		}
	};
	
	

}
