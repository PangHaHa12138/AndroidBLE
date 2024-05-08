package com.example.hlkb40_demo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.hlkb40_demo.activity.ReadWriteActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownLoad {

	URL fileurl = null;
	String urlfile;
	boolean bisConnFlag = false;
	private OutputStream outStream;
	private InputStream inStream;
	private final int BUSY = 1;
	private final int SUCCESS = 2;
	private final int FAILED = 3;
	private final int TIMEOUT = 4;
	private final int OPEN = 5;
	private Message myMessage;
	private int length;
	private InputStream is;
	OutputStream fos;
	private ReadWriteActivity relayControl;
	private String sdPath;
	private  String PATH;
	void downloadfile(String urlfile) throws MalformedURLException {
		this.urlfile = urlfile;
		relayControl = new ReadWriteActivity();
		ConnectivityManager conManager = (ConnectivityManager) ReadWriteActivity.macticity
				.getSystemService(ReadWriteActivity.macticity.CONNECTIVITY_SERVICE);
		myMessage = new Message();
		sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();



		
		NetworkInfo network = conManager.getActiveNetworkInfo();
		if (network != null) {
			bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
		}
		if (bisConnFlag) {
			MyThread thread = new MyThread();
			thread.start();
		} else {

			myMessage = myhandler.obtainMessage(OPEN);
			myhandler.sendMessage(myMessage);

		}

	}

	class MyThread extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				fileurl = new URL(urlfile);

				HttpURLConnection conn = (HttpURLConnection) fileurl
						.openConnection();
				conn.setConnectTimeout(6 * 1000);
				conn.setDoInput(true);

				conn.connect();

				is = conn.getInputStream();

				length = (int) conn.getContentLength();
			} catch (IOException e) {
				myMessage = myhandler.obtainMessage(TIMEOUT);
				myhandler.sendMessage(myMessage);
				e.printStackTrace();
			}
		if (length > 0) {

				byte[] imgData = new byte[length];

				byte[] buffer = new byte[255];
				System.out.println("size = " + length);
				while(ReadWriteActivity.readStr1 == null);
				if (ReadWriteActivity.readStr1.equals("\r\nOK\r\n")) {
					int readLen = 0;

					int destPos = 0;
					
					 File file = new File(PATH);// 获取文件路径
					 try {
						fos = new FileOutputStream(file);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						while ((readLen = is.read(buffer)) > 0) {

							System.arraycopy(buffer, 0, imgData, destPos,
									readLen);
							fos.write(buffer,0,readLen);
							//RelayControl.outStream.write(buffer);
							//System.out.println("buffer:" + new String(buffer));
							destPos += readLen;
						}
						if(file.length() == length){
							relayControl.doSendFileByBluetooth(PATH);
						}else{
							myMessage = new Message();
							myMessage = myhandler.obtainMessage(FAILED);
							myhandler.sendMessage(myMessage);
							return;
							
						}					
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					myMessage = new Message();
					myMessage = myhandler.obtainMessage(BUSY);
					myhandler.sendMessage(myMessage);
					return;
				}
		
			}else{
				myMessage = myhandler.obtainMessage(FAILED);
				myhandler.sendMessage(myMessage);
				return;
			}

		}
	}

	private Handler myhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS:
				ReadWriteActivity.mDialog.dismiss();
				Toast.makeText(ReadWriteActivity.macticity, "更新成功",
						Toast.LENGTH_SHORT).show();
				break;
			case FAILED:
				ReadWriteActivity.mDialog.dismiss();
				Toast.makeText(ReadWriteActivity.macticity, "更新失败",
						Toast.LENGTH_SHORT).show();
				break;
			case TIMEOUT:
				ReadWriteActivity.mDialog.dismiss();
				Toast.makeText(ReadWriteActivity.macticity, "连接超时",
						Toast.LENGTH_SHORT).show();
				break;
			case BUSY:
				ReadWriteActivity.mDialog.dismiss();
				Toast.makeText(ReadWriteActivity.macticity, "设备正忙",
						Toast.LENGTH_SHORT).show();
				break;
			case OPEN:
				ReadWriteActivity.mDialog.dismiss();
				Toast.makeText(ReadWriteActivity.macticity, "请打开网络",
						Toast.LENGTH_SHORT).show();
			}
		}

	};
}
