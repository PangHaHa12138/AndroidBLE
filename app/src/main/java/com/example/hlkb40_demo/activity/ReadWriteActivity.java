package com.example.hlkb40_demo.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.hlkb40_demo.AdapterManager;
import com.example.hlkb40_demo.BluetoothApplication;
import com.example.hlkb40_demo.BluetoothLeClass;
import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.activity.DeviceScanActivity;
import com.example.hlkb40_demo.activity.SelectFileActivity;
import com.example.hlkb40_demo.utilInfo.Utils;
import com.example.hlkb40_demo.WriterOperation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressLint("ShowToast")
public class ReadWriteActivity extends Activity implements
		OnCheckedChangeListener {

	public static boolean isRecording = false;// �߳̿��Ʊ��
	private EditText pathet = null;
	private Button localbt = null;

	private Button releaseCtrl, btBack, btSend;
	public static final String SEND_FILE_NAME = "sendFileName";
	public static final int RESULT_CODE = 1000;

	private Button updatebt = null;
	private EditText _txtRead, _txtSend;
	private ConnectedThread manageThread;
	private Handler mHandler;
	private BluetoothApplication mApplication;
	private AdapterManager mAdapterManager;
	private RadioGroup radiogr;
	private RadioButton filebt;
	private RadioButton webbt;
	private RadioGroup selectrg;
	private RadioButton psk_patchrb;
	private RadioButton sbcrb;
	private EditText wwwet;
	private Button readbt;
	private String encodeType = "GBK";
	public static Dialog mDialog;
	public static Activity macticity;
	private TextView precenttv;
	private final static int SET = 1;
	private byte[] temp = new byte[256];;
	public static String readStr1;
	private String sdPath;
	private Editor editor;
	private String filePath;
	private File dir;
	private int precent;
	private BluetoothGattCharacteristic blagttchar;
	private BluetoothLeClass bleclass;
	private SharedPreferences sp;
	private String sharepath = null;
	private FileInputStream isfile = null;
	private List<BluetoothGattCharacteristic> gattCharacteristics;
	private BluetoothGattCharacteristic mgattCharacteristic = null;
	private BluetoothGattCharacteristic readgattCharacteristic = null;
	private BluetoothGattDescriptor descriptor = null;
	private final static String UUID_KEY_DATA = "0000ff01-0000-1000-8000-00805f9b34fb";
	private final static String UUID_RECV_DATA = "0000ff02-0000-1000-8000-00805f9b34fb";
	private final static String UUID_SERVER = "0000fe00-0000-1000-8000-00805f9b34fb";
	private final static String UUID_CHARA = "00002803-0000-1000-8000-00805f9b34fb";
	private final static String UUID_DES = "00002902-0000-1000-8000-00805f9b34fb";
	private WriterOperation woperation;
	private int recv_data;
	private InputStream input;
	private long leng;
	private boolean isrun = true;
	private Object lock = new Object();
	private byte[] baseaddr = null;
	private int firstaddr = 0;
	private int sencondaddr = 0x14000;
	private final static int OTA_CMD_GET_STR_BASE = 1;
	private final static int OTA_CMD_PAGE_ERASE = 3;
	private final static int OTA_CMD_CHIP_ERASE = 4;
	private final static int OTA_CMD_WRITE_DATA = 5;
	private final static int OTA_CMD_READ_DATA = 6;
	private final static int OTA_CMD_WRITE_MEM = 7;
	private final static int OTA_CMD_READ_MEM  = 8;
    private final static int OTA_CMD_REBOOT = 9;
	private final static int OTA_CMD_NULL = 10;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.relaycontrol);
		bleclass = DeviceScanActivity.getInstance().mBLE;
		macticity = this;
		woperation = new WriterOperation();
		List<BluetoothGattService> gattServices = new ArrayList<>();
		for (BluetoothGattService gatt : gattServices) {
			gattCharacteristics = gatt.getCharacteristics();
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				if (gattCharacteristic.getUuid().toString()
						.equals(UUID_KEY_DATA)) {
					mgattCharacteristic = gattCharacteristic;
					setTitle("找到端口");
				}
				descriptor = gattCharacteristic.getDescriptor(UUID.fromString(UUID_DES));
			    if(descriptor != null){
			    	readgattCharacteristic =  gattCharacteristic;
			    	bleclass.setCharacteristicNotification(gattCharacteristic, true);
			    	descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			    	bleclass.writeDescriptor(descriptor);
			    }		
			}
		}
//		manageThread = new ConnectedThread();
		mHandler = new MyHandler();
//		manageThread.Start();
		
		findMyView();
		setMyViewListener();
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		editor = sp.edit();
		sharepath = sp.getString("path", "");
		pathet.setText(sharepath);
		if (sharepath.length() <= 0) {
			editor.putString("path", "");
			editor.commit();
		}
		mApplication = BluetoothApplication.getInstance();
		mApplication.getTouchObject();
		mAdapterManager = new AdapterManager(this);
		mApplication.setAdapterManager(mAdapterManager);
		// 接收区不可见
		_txtRead.setCursorVisible(false); // 设置输入框中的光标不可见
		_txtRead.setFocusable(false); // 无焦点
		registerBoradcastReceiver();

	}
	 public void registerBoradcastReceiver(){  
	        IntentFilter myIntentFilter = new IntentFilter();  
	        myIntentFilter.addAction("state"); 
	        myIntentFilter.addAction("recvdata");
	        //注册广播        
	        registerReceiver(mBroadcastReceiver, myIntentFilter);  
	    }
	   BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){  
		        @Override  
		        public void onReceive(Context context, Intent intent) {  
		            String action = intent.getAction();
		            int i = intent.getIntExtra("state", 0);
		            if(action.equals("state")){  
		            	 getActionBar().setTitle(R.string.disconnected); 
		            } 
		            if(action.equals("recvdata")){
//		            	synchronized(lock){
//							try {
//								lock.notifyAll();
//								lock.wait();
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						 }
		            	setRecv_data(1);
		            	baseaddr = readgattCharacteristic.getValue();
						_txtRead.append(Utils.bytesToHexString(baseaddr) + '\n');
				    }
		        }  
		          
		 };
		public int getRecv_data() {
			return recv_data;
		}
		public void setRecv_data(int recv_data) {
			this.recv_data = recv_data;
		}
	 protected void onDestroy() {
		 super.onDestroy();
		 isrun = false;
		 bleclass.disconnect();
		 unregisterReceiver(mBroadcastReceiver);
		 
	 };
	
	private void findMyView() {

		pathet = (EditText) findViewById(R.id.pathet);
		localbt = (Button) findViewById(R.id.localbt);
		btBack = (Button) findViewById(R.id.button2);
		btSend = (Button) findViewById(R.id.btSend);
		updatebt = (Button) findViewById(R.id.updatebt);
		_txtRead = (EditText) findViewById(R.id.etShow);
		_txtSend = (EditText) findViewById(R.id.etSend);
		radiogr = (RadioGroup) findViewById(R.id.radiogr);
		webbt = (RadioButton) findViewById(R.id.webbt);
		filebt = (RadioButton) findViewById(R.id.filebt);
		selectrg = (RadioGroup) findViewById(R.id.selectrg);
		psk_patchrb = (RadioButton) findViewById(R.id.psk_patchrb);
		sbcrb = (RadioButton) findViewById(R.id.sbcrb);
		wwwet = (EditText) findViewById(R.id.wwwet);
		// readbt = (Button) findViewById(R.id.readbt);

	}

	private void setMyViewListener() {
		// ����RadioButton

		localbt.setOnClickListener(new localListener());
		btBack.setOnClickListener(new ClickEvent());
		btSend.setOnClickListener(new ClickEvent());
		// readbt.setOnClickListener(new ClickEvent());
		updatebt.setOnClickListener(new updateListener());
		radiogr.setOnCheckedChangeListener(new fileorweb());
		selectrg.setOnCheckedChangeListener(new fileorweb());
	}

	class fileorweb implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			if (arg0 == radiogr) {
				if (filebt.getId() == arg1) {
					Toast.makeText(ReadWriteActivity.this, "请选择文件",
							Toast.LENGTH_SHORT).show();
				}
				if (webbt.getId() == arg1) {

					Toast.makeText(ReadWriteActivity.this, "请输入网址",
							Toast.LENGTH_SHORT).show();
				}
			}

		}

	}

	class updateListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			filePath = pathet.getText().toString().trim();
			File file = new File(filePath);
			if(file.length() < 100){
				Toast.makeText(ReadWriteActivity.this, "请选择有效的配置文件", Toast.LENGTH_LONG).show();
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					
					try {
						isrun = true;
						doSendFileByBluetooth(filePath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}

	}

	class mythread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Message myMessage = new Message();
			myMessage = MyHandler.obtainMessage(SET);
			MyHandler.sendMessage(myMessage);
		}

	}

	private Handler MyHandler = new Handler() {

		@Override
		public void handleMessage(Message arg0) {
			super.handleMessage(arg0);
			switch (arg0.what) {
			case SET:

			}
		}

	};



	class sendfileListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			try {
				// 获取文件路径
				String filePath = pathet.getText().toString().trim();
				doSendFileByBluetooth(filePath);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_CODE) {
			// 请求为 "选择文件"
			try {
				// 取得选择的文件名
				String sendFileName = data.getStringExtra(SEND_FILE_NAME);
				editor.putString("path", sendFileName);
				editor.commit();
				pathet.setText(sendFileName);
			} catch (Exception e) {

			}
		}

	}

	public void showRoundProcessDialog(Context mContext, int layout) {
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(
				R.layout.loading_process_dialog_anim, null);
		precenttv = (TextView) view.findViewById(R.id.precenttv);
		mDialog = new Dialog(ReadWriteActivity.this, R.style.dialog);
		// mDialog.setOnKeyListener(keyListener);
		mDialog.setCancelable(true);
		mDialog.setContentView(view);
		mDialog.show();

	}

	class localListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(ReadWriteActivity.this,
					SelectFileActivity.class);
			intent.putExtra("filepatch", pathet.getText().toString());
			startActivityForResult(intent, ReadWriteActivity.RESULT_CODE);
		}

	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {

	}

	private class ClickEvent implements OnClickListener {
		@Override
		public void onClick(View v) {

			if (v == btBack) {

				manageThread.Stop();
				Intent intent = new Intent();
				Toast.makeText(ReadWriteActivity.this, "返回上一界面",
						Toast.LENGTH_SHORT).show();

				ReadWriteActivity.this.finish();

			} else if (v == btSend) {
				String infoSend = _txtSend.getText().toString();
				_txtRead.append(infoSend + "->" + '\n');
				sendMessage(infoSend + '\r');
				setTitle("发送成功");
			} else if (v == readbt) {
				sdPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				dir = new File(sdPath + "/Freqchip/");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				showRoundProcessDialog(ReadWriteActivity.this,
						R.layout.loading_process_dialog_anim);
			}

		}

	}

	public static void setEditTextEnable(TextView view, Boolean able) {
		// view.setTextColor(R.color.read_only_color); //设置只读时的文字颜色
		if (view instanceof EditText) {
			view.setCursorVisible(able); // 设置输入框中的光标不可见
			view.setFocusable(able); // 无焦点
			view.setFocusableInTouchMode(able); // 触摸时也得不到焦点
		}
	}

	@SuppressLint("ShowToast")
	void sendMessage(String message) {

		// 控制模块
		byte[] msgBuffer = null;
		byte[] cmd_write = null;
		try {
			msgBuffer = message.getBytes(encodeType);// 设置编码
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
		}
		cmd_write = woperation.cmd_operation(OTA_CMD_WRITE_MEM,0x2000800,msgBuffer.length);		
		mgattCharacteristic.setValue(woperation.byteMerger(cmd_write,msgBuffer));
		bleclass.writeCharacteristic(mgattCharacteristic);
	
		Toast.makeText(this, "发送数据成功", Toast.LENGTH_SHORT);
	}
    private int page_erase(int addr,long length,BluetoothGattCharacteristic mgattCharacteristic,BluetoothLeClass bleclass){
		
		long count = length / 0x1000;
		if((length % 0x1000) != 0){
			count ++;
		}
		for(int i = 0;i < count;i ++){
			woperation.send_data(OTA_CMD_PAGE_ERASE,addr,null,0,mgattCharacteristic,bleclass);
			while(getRecv_data() != 1);
			setRecv_data(0);
			addr += 0x1000;
		}
		return 0;
	}
	public void doSendFileByBluetooth(String filePath)
			throws FileNotFoundException {

		if (!filePath.equals(null)) {
		
			int read_count;
			int i= 0;
			int addr;
			byte[] inputBuffer = new byte[256];
			File file = new File(filePath);// 成文件路径中获取文件
			isfile = new FileInputStream(file);
			leng = file.length();
			input = new BufferedInputStream(isfile);
			setRecv_data(0);
			woperation.send_data(OTA_CMD_GET_STR_BASE,0,null,0,mgattCharacteristic,bleclass);
			while(getRecv_data() != 1) ;
			if(woperation.bytetoint(baseaddr) == firstaddr){
				addr = sencondaddr;
			}else{
				addr = firstaddr;	
			}
			setRecv_data(0);
			
			page_erase(addr,leng,mgattCharacteristic,bleclass);
			try {
			while(((read_count = input.read(inputBuffer,0,256)) != -1) && isrun){
				woperation.send_data(OTA_CMD_WRITE_DATA,addr,inputBuffer,read_count,mgattCharacteristic,bleclass);
				while(getRecv_data() != 1) ;
				setRecv_data(0);
				addr += read_count;
				System.out.println("radrr " + " " + i++ );
			}
			mHandler.sendEmptyMessage(9);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
			
			
			
			
//			try {
//				while(((read_count = input.read(inputBuffer,0,256)) != -1) && isrun){
//					woperation.send_data(OTA_CMD_WRITE_MEM,addr,inputBuffer,read_count,mgattCharacteristic,bleclass);
//					while(getRecv_data() != 1) ;
////					synchronized(lock){
////						try {
////							lock.notifyAll();
////							lock.wait();
////						} catch (InterruptedException e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
////					 }
//					setRecv_data(0);
//					addr += read_count;
//					System.out.println("radrr " + " " + i++ );
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		} else {
			Toast.makeText(getApplicationContext(), "请选择要发送的文件!",
					Toast.LENGTH_LONG).show();
		}
	}

	class ConnectedThread extends Thread {

		private long wait;
		private Thread thread;
		public ConnectedThread() {
			isRecording = false;
			this.wait = 10;
			thread = new Thread(new ReadRunnable());
		}
		public void Stop() {
			isRecording = false;
		}
		public void Start() {
			isRecording = true;
			State aa = thread.getState();
			if (aa == State.NEW) {
				thread.start();
			} else
				thread.resume();
		}
		private class ReadRunnable implements Runnable {
			public void run() {
				byte[] recv = null;
				int len = 0;
				while (isRecording) {		
						try {
							// System.out.println("totle " + totle );
							recv = readgattCharacteristic.getValue();
							if (recv.length > 0) {
								

							 mHandler.sendEmptyMessage(9);
							 /*byte[] btBuf = new byte[10];
								System.arraycopy(recv, 0, btBuf, 0, recv.length);

								readStr1 = new String(btBuf, encodeType);
								//System.out.println("readstr " + readStr1);
								mHandler.obtainMessage(01, len, -1, readStr1)
										.sendToTarget();
							  */
//							 mHandler.obtainMessage(01, len, -1, recv)
//								.sendToTarget();
							}
							Thread.sleep(wait);
						} catch (Exception e) {
							// TODO Auto-generated catch block
						

					}

				}
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 00:
				isRecording = false;
				_txtRead.setText("");
				_txtRead.setHint("socket连接已关闭");
				Intent intent = new Intent();
				Toast.makeText(ReadWriteActivity.this, "返回上一界面",
						Toast.LENGTH_SHORT).show();
				ReadWriteActivity.this.finish();
				// _txtRead.setText("inStream establishment Failed!");
				break;

			case 01:
				byte[] info = (byte[]) msg.obj;
				_txtRead.append("read " + info[0] + " " + info[1]);
				break;
			case 02:
				mDialog.cancel();
				Toast.makeText(ReadWriteActivity.this, "擦除失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 03:
				mDialog.cancel();
				Toast.makeText(ReadWriteActivity.this, "更新失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 04:
				mDialog.cancel();
				Toast.makeText(ReadWriteActivity.this, "读取失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 05:
				mDialog.cancel();
				Toast.makeText(ReadWriteActivity.this, "读取成功",
						Toast.LENGTH_SHORT).show();
				break;
			case 06:
				mDialog.cancel();
				Toast.makeText(ReadWriteActivity.this, "写入成功",
						Toast.LENGTH_SHORT).show();
				break;
			case 07:
				precenttv.setText("正在更新.." + precent + "%");
				break;
			case 8:
				Toast.makeText(ReadWriteActivity.this, "无接收数据",
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				Toast.makeText(ReadWriteActivity.this, "写入成功",
						Toast.LENGTH_SHORT).show();
			default:
				break;
			}
		}
	}

}