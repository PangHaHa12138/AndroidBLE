package com.example.hlkb40_demo.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.example.hlkb40_demo.BluetoothLeClass;
import com.example.hlkb40_demo.BluetoothLeClass.OnRecvDataListerner;
import com.example.hlkb40_demo.AdapterManager;
import com.example.hlkb40_demo.BluetoothApplication;
import com.example.hlkb40_demo.BluetoothLeClass.OnWriteDataListener;
import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.WriterOperation;
import com.example.hlkb40_demo.activity.DeviceScanActivity;
import com.example.hlkb40_demo.activity.SelectFileActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OtaActiviy extends Activity {
	private final static String UUID_SEND_DATA = "0000ff01-0000-1000-8000-00805f9b34fb";
	private final static String UUID_RECV_DATA = "0000ff02-0000-1000-8000-00805f9b34fb";
	private final static String UUID_SEND_DATA_H = "02f00000-0000-0000-0000-00000000ff01";
	private final static String UUID_RECV_DATA_H = "02f00000-0000-0000-0000-00000000ff02";
	private final static String UUID_DES = "00002902-0000-1000-8000-00805f9b34fb";
	private final static int OTA_CMD_NVDS_TYPE = 0;
	private final static int OTA_CMD_GET_STR_BASE = 1;
	private final static int OTA_CMD_PAGE_ERASE = 3;
	private final static int OTA_CMD_CHIP_ERASE = 4;
	private final static int OTA_CMD_WRITE_DATA = 5;
	private final static int OTA_CMD_READ_DATA = 6;
	private final static int OTA_CMD_WRITE_MEM = 7;
	private final static int OTA_CMD_READ_MEM = 8;
	private final static int OTA_CMD_REBOOT = 9;
	private final static int OTA_CMD_NULL = 10;
	private final static int DEVICE_8010 = 0;
	private final static int DEVICE_8010H = 1;

	private Editor editor;
	private Button localbt;
	private Button updatebt;
	private EditText _txtRead;
	private EditText checksumEt;
	private EditText pathet = null;
	private String filePath;
	private String sharepath = null;
	private SharedPreferences sp;
	private InputStream input;
	private long leng;
	private int recv_data;
	private int writePrecent;
	private TextView precenttv;
	static Dialog mDialog;
	private AdapterManager mAdapterManager;
	private BluetoothApplication mApplication;
	private BluetoothLeClass bleclass;
	private FileInputStream isfile = null;
	private int sencondaddr = 0x14000;
	private int firstaddr = 0;
	private byte[] recvValue = null;
	private Handler mHandler;
	private String checkSum;
	private int checkSumLength;
	private List<BluetoothGattCharacteristic> gattCharacteristics;
	private WriterOperation woperation;
	private BluetoothGattCharacteristic mgattCharacteristic = null;
	private BluetoothGattCharacteristic mHgattCharacteristic = null;
	private BluetoothGattCharacteristic mledwritegattCharacteristic = null;
	private BluetoothGattCharacteristic readgattCharacteristic = null;
	private BluetoothGattCharacteristic ledreadgattCharacteristic = null;
	private BluetoothGattDescriptor descriptor = null;
	private boolean writeStatus = false;
	private Activity tempActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file);
		viewinit();
		mHandler = new MyHandler();
		woperation = new WriterOperation();
		bleclass = DeviceScanActivity.getInstance().mBLE;
		bleclass.setOnRecvDataListener(mOnRecvData);
		bleclass.setOnWriteDataListener(mOnWriteData);

		verifyStoragePermissions();
		tempActivity = this;
		List<BluetoothGattService> gattServices = new ArrayList<>();
		for (BluetoothGattService gatt : gattServices) {
			gattCharacteristics = gatt.getCharacteristics();
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				if ((gattCharacteristic.getUuid().toString()
						.equals(UUID_SEND_DATA)) || (gattCharacteristic.getUuid().toString()
						.equals(UUID_SEND_DATA_H))) {
					mgattCharacteristic = gattCharacteristic;
					setTitle("找到端口");
				} else if ((gattCharacteristic.getUuid().toString()
						.equals(UUID_RECV_DATA)) || (gattCharacteristic.getUuid().toString()
						.equals(UUID_RECV_DATA_H))) {
					descriptor = gattCharacteristic.getDescriptor(UUID
							.fromString(UUID_DES));
					if (descriptor != null) {
						bleclass.setCharacteristicNotification(
								gattCharacteristic, true);
						descriptor
								.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						bleclass.writeDescriptor(descriptor);
					}
				} 

			}

		}
		
		
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
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		bleclass.disconnect();
	}
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

	@TargetApi(Build.VERSION_CODES.M)
	public void verifyStoragePermissions() {
		int permission = this.checkSelfPermission(
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			this.requestPermissions( PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

		if (requestCode == REQUEST_EXTERNAL_STORAGE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				Toast.makeText(this, "存储权限已打开", Toast.LENGTH_SHORT).show();
			} else {

				Toast.makeText(this, "需要打开存储权限才可以OTA", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void showDialog(){
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.loading_process_dialog_anim,null);
		precenttv = (TextView) view.findViewById(R.id.precenttv);
		mDialog = new Dialog(this, R.style.dialog);
		// mDialog.setOnKeyListener(keyListener);
		mDialog.setCancelable(false);
		mDialog.setContentView(view);
		mDialog.show();
	}
	private OnRecvDataListerner mOnRecvData = new OnRecvDataListerner() {

		@Override
		public void OnCharacteristicRecv(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			recvValue = characteristic.getValue();
			setRecv_data(1);
			
		}
	};
	private OnWriteDataListener mOnWriteData = new OnWriteDataListener() {

		@Override
		public void OnCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			    //System.out.println("status " + status);
				if(status == 0){
					writeStatus = true;
				}
		}

	};
	void viewinit(){
		localbt = (Button) findViewById(R.id.localbt);
		updatebt = (Button) findViewById(R.id.updatebt);
		_txtRead = (EditText) findViewById(R.id.etShow);
		pathet = (EditText)findViewById(R.id.pathet);
		localbt.setOnClickListener(new LocalbtOnClickListenerimp());
		updatebt.setOnClickListener(new UpdatebtOnClickListenerimp());
		
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SelectFileActivity.RESULT_CODE) {
			// 请求为 "选择文件"
			try {
				// 取得选择的文件名
				String sendFileName = data.getStringExtra(SelectFileActivity.SEND_FILE_NAME);
				editor.putString("path", sendFileName);
				editor.commit();
				pathet.setText(sendFileName);
			} catch (Exception e) {

			}
		}

	}
	class LocalbtOnClickListenerimp implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(OtaActiviy.this,
					SelectFileActivity.class);
			intent.putExtra("filepatch", pathet.getText().toString());
			startActivityForResult(intent, SelectFileActivity.RESULT_CODE);
		}

	}
	class UpdatebtOnClickListenerimp implements OnClickListener {
		@Override
		public void onClick(View v) {
			//_txtRead.append("count -->" + bleclass.i);
			byte[] Buffer = new byte[4];
			if(bleclass.isDisconnected) {
				Toast.makeText(OtaActiviy.this, "连接已断开",
						Toast.LENGTH_LONG).show();
				return;
			}
			filePath = pathet.getText().toString().trim();
			File file = new File(filePath);
			if (file.length() < 100) {
				Toast.makeText(OtaActiviy.this, "请选择有效的配置文件",
						Toast.LENGTH_LONG).show();
				return;
			}
//			try {
//				FileInputStream infile = new FileInputStream(file);
//				try {
//					infile.skip(0x167);
//					input = new BufferedInputStream(infile);
//					input.read(Buffer, 0, 4);
//					
//					if((Buffer[0] != 0x52) || (Buffer[1] != 0x51) || (Buffer[2] != 0x51) || (Buffer[3] != 0x52)){
//						Toast.makeText(OtaActiviy.this, "请选择正确的文件",
//								Toast.LENGTH_LONG).show();
//						_txtRead.setText("请选择正确的文件");
//						return;
//					}
//					checkSum = checksumEt.getText().toString().trim();
//					checkSumLength = checkSum.length();
//					infile.close();
//					file = new File(filePath);
//					infile = new FileInputStream(file);
//					input = new BufferedInputStream(infile);
//					infile.skip(file.length() - 8);
//					byte[] readCheckSum = new byte[6];
//					input.read(readCheckSum, 0, 6);
//					if(!Arrays.equals(checkSum.getBytes(),readCheckSum)){
//						//System.out.println("bytedd: " + readCheckSum[0] + " " + readCheckSum[1]+" "+ readCheckSum[2]);
//						Toast.makeText(OtaActiviy.this, "请选择正确的文件",
//								Toast.LENGTH_LONG).show();
//						_txtRead.setText("请选择正确的文件");
//						return;
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			bleclass.mtuChange = false;
			mHandler.sendEmptyMessage(3);
			new Thread(new Runnable() {
				@Override
				public void run() {

					try {
						doSendFileByBluetooth(filePath);
						//mHandler.sendEmptyMessage(1);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}

	}
	private int page_erase(int addr, long length,
			BluetoothGattCharacteristic mgattCharacteristic,
			BluetoothLeClass bleclass) {

		long count = length / 0x1000;
		if ((length % 0x1000) != 0) {
			count++;
		}
		for (int i = 0; i < count; i++) {
			woperation.send_data(OTA_CMD_PAGE_ERASE, addr, null, 0,
					mgattCharacteristic, bleclass);
			while (getRecv_data() != 1);
			setRecv_data(0);
			addr += 0x1000;
		}
		return 0;
	}
	public int getRecv_data() {
		return recv_data;
	}

	public void setRecv_data(int recv_data) {
		this.recv_data = recv_data;
	}
    boolean checkDisconnect(){
    	if(bleclass.isDisconnected){
    		mHandler.sendEmptyMessage(2);
    		return true;
    	}
    	return false;
    }
	public void doSendFileByBluetooth(String filePath)
			throws FileNotFoundException {
		if (!filePath.equals(null)) {
			int read_count;
			int i = 0;
			int addr;
			int lastReadCount = 0;
			int packageSize = 235;//bleclass.mtuSize - 3; //235;
			int send_data_count = 0;
			int deviceType;
			
			
			
			File file = new File(filePath);// 成文件路径中获取文件
			isfile = new FileInputStream(file);
			leng = file.length();
			input = new BufferedInputStream(isfile);

			// 先确定模块类型，设置mtu
			setRecv_data(0);
			woperation.send_data(OTA_CMD_NVDS_TYPE, 0, null, 0,
					mgattCharacteristic, bleclass);	
			while (getRecv_data() != 1){
				if(checkDisconnect()){
					return;
				}	
			}
			if ((woperation.bytetochar(recvValue) & 0x10) == 0) {
				deviceType = DEVICE_8010;
				bleclass.requestMtu(247);
			} else {
				deviceType = DEVICE_8010H;
				bleclass.requestMtu(512);
				
			}
			while(bleclass.mtuChange == false){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("mtuChange " + bleclass.mtuChange);
			}
			packageSize = bleclass.mtuSize - 3 - 9;
			byte[] inputBuffer = new byte[packageSize];

			// 再获取当前升级程序的存储起始地址
			setRecv_data(0);
			woperation.send_data(OTA_CMD_GET_STR_BASE, 0, null, 0,
					mgattCharacteristic, bleclass);
			while (getRecv_data() != 1){
				if(checkDisconnect()){
					return;
				}	
			}
			if(deviceType == DEVICE_8010){
				if (woperation.bytetoint(recvValue) == firstaddr) {
					addr = sencondaddr;
				} else {
					addr = firstaddr;
				}
			}else if(deviceType == DEVICE_8010H){
				addr = woperation.bytetoint(recvValue);
			}else{
				return;
			}

			// 按照上面的MTU和文件大小，计算具体需要发多少数据，并擦除模块对应的升级内存空间
			setRecv_data(0);
			page_erase(addr, leng, mgattCharacteristic, bleclass);
		
			try {
				// 最后开始发送升级文件
         		while (((read_count = input.read(inputBuffer, 0, packageSize)) != -1)) {
    					woperation.send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer,read_count, mgattCharacteristic, bleclass);
    					//for(delay_num = 0;delay_num < 10000;delay_num++);
    					addr += read_count;
    					lastReadCount = read_count;
    					send_data_count += read_count;
    					//System.out.println("times" + i + " " + read_count);
    					i ++;
    					// 更新升级进度%
    					writePrecent = (int)(((float)send_data_count / leng) * 100);
    					mHandler.sendEmptyMessage(1);
    					while(!writeStatus);
    					writeStatus = false;
						while (getRecv_data() != 1){
							if(checkDisconnect()){
								return;
							}	
						}
    					setRecv_data(0);				
    		    }
         		// 如果模块回复的数据大小和升级的不一致，则断开连接
         		while(woperation.bytetoint(recvValue) != (addr - lastReadCount)){
         			if(checkDisconnect()){
						return;
					}	
         		}
         		// 升级完成，则重启设备！
         		woperation.send_data(OTA_CMD_REBOOT, 0, null,
						0, mgattCharacteristic, bleclass);
				mHandler.sendEmptyMessage(0);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Toast.makeText(getApplicationContext(), "请选择要发送的文件!",
					Toast.LENGTH_LONG).show();
		}
	}
	
	private class MyHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mDialog.cancel();
				Toast.makeText(OtaActiviy.this, "写入成功",
						Toast.LENGTH_SHORT).show();
				_txtRead.setText("写入成功");
			break;
			case 1:
				precenttv.setText("写入.." + writePrecent + "%");
				break;
			case 2:
				mDialog.cancel();
				Toast.makeText(OtaActiviy.this, "连接断开",
						Toast.LENGTH_SHORT).show();
				tempActivity.finish();
				break;
			case 3:
				showDialog();
				break;
			default:
				break;
			}
		}
	}
}
