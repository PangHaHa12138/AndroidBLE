package com.example.hlkb40_demo.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.hlkb40_demo.AdapterManager;
import com.example.hlkb40_demo.BluetoothApplication;
import com.example.hlkb40_demo.BluetoothLeClass;
import com.example.hlkb40_demo.OpAdapte;
import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.RecvFile;
import com.example.hlkb40_demo.WriterOperation;
import com.example.hlkb40_demo.BluetoothLeClass.OnDataAvailableListener;
import com.example.hlkb40_demo.activity.DeviceScanActivity;
import com.example.hlkb40_demo.activity.SelectFileActivity;


import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
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
import android.os.Handler;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

public class BleViewPage extends Activity {
	private TextView consoletv;
	private TextView txrxtv;
	private TextView filetv;
	private TextView abouttv;
	private ViewPager opviewpager;
	private List<View> layouts = null;
	private List<TextView> tvs = null;
	private View about;
	private View console;
	private View file;
	private View txrx;
	private Button localbt;
	private Button updatebt;
	private Button readbt;
	private BluetoothLeClass bleclass;
	private WriterOperation woperation;
	private List<BluetoothGattCharacteristic> gattCharacteristics;
	private BluetoothGattCharacteristic mgattCharacteristic = null;
	private BluetoothGattCharacteristic mledwritegattCharacteristic = null;
	private BluetoothGattCharacteristic readgattCharacteristic = null;
	private BluetoothGattCharacteristic ledreadgattCharacteristic = null;
	private BluetoothGattDescriptor descriptor = null;
	private byte[] baseaddr = null;
	private int firstaddr = 0;
	private int sencondaddr = 0x14000;
	private int recv_data;
	private EditText _txtRead;
	private EditText pathet = null;
	private SharedPreferences sp;
	private String sharepath = null;
	private Editor editor;
	private String filePath;
	private boolean isrun = true;
	private FileInputStream isfile = null;
	private InputStream input;
	private long leng;
	private AdapterManager mAdapterManager;
	private BluetoothApplication mApplication;
	private Handler mHandler;
	private RecvFile rf;
	private TextView precenttv;
	static Dialog mDialog;
	private int recv_count = 0;
	private int count = 0;
	private TextView lighttv0 = null;
	private SeekBar ledseekBar0 = null;
	private TextView lighttv1 = null;
	private SeekBar ledseekBar1 = null;
	private TextView lighttv2 = null;
	private SeekBar ledseekBar2 = null;
	private byte[] light = new byte[3];
	public static final int RESULT_CODE = 1000;
	public static final String SEND_FILE_NAME = "sendFileName";
	private final static String UUID_KEY_DATA = "0000ff01-0000-1000-8000-00805f9b34fb";
	private final static String UUID_RECV_DATA = "0000ff02-0000-1000-8000-00805f9b34fb";
	private final static String UUID_SERVER = "0000fe00-0000-1000-8000-00805f9b34fb";
	private final static String UUID_CHARA = "00002803-0000-1000-8000-00805f9b34fb";
	private final static String UUID_DES = "00002902-0000-1000-8000-00805f9b34fb";
	private final static String UUID_LED_CTR_DATA = "0000ff0c-0000-1000-8000-00805f9b34fb";
	private final static String UUID_LED_CTR_NOTF = "0000ff0d-0000-1000-8000-00805f9b34fb";
	private final static int OTA_CMD_GET_STR_BASE = 1;
	private final static int OTA_CMD_PAGE_ERASE = 3;
	private final static int OTA_CMD_CHIP_ERASE = 4;
	private final static int OTA_CMD_WRITE_DATA = 5;
	private final static int OTA_CMD_READ_DATA = 6;
	private final static int OTA_CMD_WRITE_MEM = 7;
	private final static int OTA_CMD_READ_MEM = 8;
	private final static int OTA_CMD_REBOOT = 9;
	private final static int OTA_CMD_NULL = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myviewpage);
		bleclass = DeviceScanActivity.getInstance().mBLE;
		List<BluetoothGattService> gattServices = new ArrayList<>();
		for (BluetoothGattService gatt : gattServices) {
			gattCharacteristics = gatt.getCharacteristics();
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				if (gattCharacteristic.getUuid().toString()
						.equals(UUID_KEY_DATA)) {
					mgattCharacteristic = gattCharacteristic;
					setTitle("找到端口");
				}else if(gattCharacteristic.getUuid().toString()
						.equals(UUID_LED_CTR_DATA)){
					//System.out.println("ddddd");
					mledwritegattCharacteristic = gattCharacteristic;
					bleclass.readCharacteristic(mledwritegattCharacteristic);
				}else if(gattCharacteristic.getUuid().toString()
						.equals(UUID_RECV_DATA)){
						descriptor = gattCharacteristic.getDescriptor(UUID
								.fromString(UUID_DES));
						if (descriptor != null) {
							bleclass.setCharacteristicNotification(gattCharacteristic,
									true);
							descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							bleclass.writeDescriptor(descriptor);
						}
				}else if(gattCharacteristic.getUuid().toString()
						.equals(UUID_LED_CTR_NOTF)){
						descriptor = gattCharacteristic.getDescriptor(UUID
								.fromString(UUID_DES));
						if (descriptor != null) {
							bleclass.setCharacteristicNotification(gattCharacteristic,
									true);
							descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							bleclass.writeDescriptor(descriptor);
						}
					
				}
//				descriptor = gattCharacteristic.getDescriptor(UUID
//						.fromString(UUID_DES));
//				if (descriptor != null) {
//					bleclass.setCharacteristicNotification(gattCharacteristic,
//							true);
//					descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//					bleclass.writeDescriptor(descriptor);
//				}
				
			}
		}
		//rf = new RecvFile();
		//bleclass.setMtu(50);
		mHandler = new MyHandler();
		woperation = new WriterOperation();
		layouts = new ArrayList<View>();
		tvs = new ArrayList<TextView>();
		consoletv = (TextView) findViewById(R.id.consoletv);
		txrxtv = (TextView) findViewById(R.id.txrxtv);
		filetv = (TextView) findViewById(R.id.filetv);
		abouttv = (TextView) findViewById(R.id.abouttv);
		tvs.add(consoletv);
		tvs.add(txrxtv);
		tvs.add(filetv);
		tvs.add(abouttv);
		for (int i = 0; i < tvs.size(); i++) {
			tvs.get(i).setOnClickListener(new OnClickListenerimp());
		}
		opviewpager = (ViewPager) findViewById(R.id.opviewpager);
		viewinit();

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
		layouts.add(console);
		layouts.add(txrx);
		layouts.add(file);
		layouts.add(about);
		opviewpager.setAdapter(new OpAdapte(layouts));
		opviewpager.setCurrentItem(0);
		opviewpager.setOnPageChangeListener(new OnPageChangeListenerimp());
		
		registerBoradcastReceiver();
		bleclass.setOnDataAvailableListener(mOnDataAvailable);
		bleclass.readCharacteristic(mledwritegattCharacteristic);
	}

	private void viewinit() {
		LayoutInflater flater = getLayoutInflater();
		console = flater.inflate(R.layout.ledctrlayout, null);
		txrx = flater.inflate(R.layout.txrx, null);
		file = flater.inflate(R.layout.file, null);
		about = flater.inflate(R.layout.about, null);
		localbt = (Button) file.findViewById(R.id.localbt);
		updatebt = (Button) file.findViewById(R.id.updatebt);
		_txtRead = (EditText) file.findViewById(R.id.etShow);
		pathet = (EditText) file.findViewById(R.id.pathet);
		//readbt = (Button) file.findViewById(R.id.readbt);
		lighttv0 = (TextView) console.findViewById(R.id.lighttv0);
		ledseekBar0 = (SeekBar) console.findViewById(R.id.ledseekBar0);
		lighttv1 = (TextView) console.findViewById(R.id.lighttv1);
		ledseekBar1 = (SeekBar) console.findViewById(R.id.ledseekBar1);
		lighttv2 = (TextView) console.findViewById(R.id.lighttv2);
		ledseekBar2 = (SeekBar) console.findViewById(R.id.ledseekBar2);
		readbt.setOnClickListener(new ReadbtOnClickListenerimp());
		localbt.setOnClickListener(new LocalbtOnClickListenerimp());
		updatebt.setOnClickListener(new UpdatebtOnClickListenerimp());
		ledseekBar0.setOnSeekBarChangeListener(new OnSeekBarChangeListenerimp());
		ledseekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListenerimp());
		ledseekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListenerimp());
		
	}
	class OnSeekBarChangeListenerimp implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			if(seekBar.getId() == ledseekBar0.getId()){
				light[0] = (byte) (progress & 0x000000ff);
				light[1] = (byte) 0;
				System.out.println("propress" +  light[0]*2);
				lighttv0.setText("led0当前亮度:" + light[0]*2);	
			}else if(seekBar.getId() == ledseekBar1.getId()){
				light[0] = (byte) (progress & 0x000000ff);
				light[1] = (byte) 1;
				System.out.println("propress" +  light[0]*2);
				lighttv1.setText("led1当前亮度:" + light[0]*2);
			}else if(seekBar.getId() == ledseekBar2.getId()){
				light[0] = (byte) (progress & 0x000000ff);
				light[1] = (byte) 2;
				System.out.println("propress" +  light[0]*2);
				lighttv2.setText("led2当前亮度:" + light[0]*2);

			}
			mledwritegattCharacteristic.setValue(light);
			bleclass.writeCharacteristic(mledwritegattCharacteristic);
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			//System.out.println("ssssss");
			mledwritegattCharacteristic.setValue(light);
			bleclass.writeCharacteristic(mledwritegattCharacteristic);
		}
		
		
	}
	
	class ReadbtOnClickListenerimp implements OnClickListener{

		@Override
		public void onClick(View v) {
			_txtRead.append(" " + recv_count + '\n');
			recv_count = 0;
		}

		
		
	}
	public void showRoundProcessDialog(Context mContext, int layout) {
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.loading_process_dialog_anim,null);
		precenttv = (TextView) view.findViewById(R.id.precenttv);
		mDialog = new Dialog(BleViewPage.this, R.style.dialog);
		// mDialog.setOnKeyListener(keyListener);
		mDialog.setCancelable(true);
		mDialog.setContentView(view);
		mDialog.show();

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

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("state");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int i = intent.getIntExtra("state", 0);
			if (action.equals("state")) {
				getActionBar().setTitle(R.string.disconnected);
			}
			
		}

	};

	public int getRecv_data() {
		return recv_data;
	}

	public void setRecv_data(int recv_data) {
		this.recv_data = recv_data;
	}


/**
* 收到BLE终端数据交互的事件
*/
private OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener(){

	/**
	 * BLE终端数据被读的事件
	 */
	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
		if (status == BluetoothGatt.GATT_SUCCESS){
			light = characteristic.getValue();
			int progress =  light[0] & 0xFF;
			System.out.println("led0当前亮度: " + light[0]);	
			ledseekBar0.setProgress(progress);
			lighttv0.setText("led0当前亮度:" + progress*2);
			progress =  light[1] & 0xFF;
			System.out.println("led1当前亮度: " + light[1]);	
			ledseekBar1.setProgress(progress);
			lighttv1.setText("led1当前亮度:" + progress*2);
			progress =  light[2] & 0xFF;
			System.out.println("led2当前亮度: " + light[2]);	
			ledseekBar2.setProgress(progress);
			lighttv2.setText("led2当前亮度:" + progress*2);
			
			
		}
			//System.out.println("onCharRead "+gatt.getDevice().getName()
			//		+" read "
			//		+characteristic.getUuid().toString()
			//		+" -> "
			//		+Utils.bytesToHexString(characteristic.getValue()));
	}
	
   /**
    * 收到BLE终端写入数据回调
    */
	public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
			baseaddr = characteristic.getValue();
			setRecv_data(1);
			//System.out.println("base"+ baseaddr);
			//rf.savefile(baseaddr, baseaddr.length);
		    //_txtRead.append(count++ +" " + '\n');
	}
};
	@Override
	protected void onDestroy() {
		bleclass.disconnect();
		isrun = false;
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	class OnClickListenerimp implements OnClickListener {

		@Override
		public void onClick(View view) {
			if (view == consoletv) {
				opviewpager.setCurrentItem(0);
			} else if (view == txrxtv) {
				opviewpager.setCurrentItem(1);
			} else if (view == filetv) {
				opviewpager.setCurrentItem(2);
			} else if (view == abouttv) {
				opviewpager.setCurrentItem(3);
			}

		}

	}

	class LocalbtOnClickListenerimp implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(BleViewPage.this,
					SelectFileActivity.class);
			intent.putExtra("filepatch", pathet.getText().toString());
			startActivityForResult(intent, BleViewPage.RESULT_CODE);
		}

	}

	class UpdatebtOnClickListenerimp implements OnClickListener {
		@Override
		public void onClick(View v) {
			//_txtRead.append("count -->" + bleclass.i);
			filePath = pathet.getText().toString().trim();
			File file = new File(filePath);
			if (file.length() < 100) {
				Toast.makeText(BleViewPage.this, "请选择有效的配置文件",
						Toast.LENGTH_LONG).show();
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {

					try {
						isrun = true;
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

	public void doSendFileByBluetooth(String filePath)
			throws FileNotFoundException {

		if (!filePath.equals(null)) {

			int read_count;
			int i = 0;
			int addr;
			byte[] inputBuffer = new byte[256];
			File file = new File(filePath);// 成文件路径中获取文件
			isfile = new FileInputStream(file);
			leng = file.length();
			input = new BufferedInputStream(isfile);
			setRecv_data(0);
			woperation.send_data(OTA_CMD_GET_STR_BASE, 0, null, 0,
					mgattCharacteristic, bleclass);
			while (getRecv_data() != 1);
			if (woperation.bytetoint(baseaddr) == firstaddr) {
				addr = sencondaddr;
			} else {
				addr = firstaddr;
			}
			setRecv_data(0);

			page_erase(addr, leng, mgattCharacteristic, bleclass);
			try {
				while (((read_count = input.read(inputBuffer, 0, 256)) != -1)
						&& isrun) {
					woperation.send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer,
							read_count, mgattCharacteristic, bleclass);
					while (getRecv_data() != 1);
					setRecv_data(0);
					addr += read_count;
					System.out.println("radrr " + " " + read_count);
				}
				mHandler.sendEmptyMessage(0);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Toast.makeText(getApplicationContext(), "请选择要发送的文件!",
					Toast.LENGTH_LONG).show();
		}
	}

	class OnPageChangeListenerimp implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int position) {
			for (int i = 0; i < layouts.size(); i++) {
				if (i == position) {
					tvs.get(position).setBackgroundResource(R.drawable.active);
				} else {
					tvs.get(i).setBackgroundResource(0);
				}

			}

		}

	}
	private class MyHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 0:
				//mDialog.cancel();
				Toast.makeText(BleViewPage.this, "写入成功",
						Toast.LENGTH_SHORT).show();
			break;
			case 1:
				showRoundProcessDialog(BleViewPage.this,
						 R.layout.loading_process_dialog_anim);
				break;
			default:
				break;
			}
		}
	}

}
