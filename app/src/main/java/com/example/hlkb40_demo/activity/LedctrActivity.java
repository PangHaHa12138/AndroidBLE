package com.example.hlkb40_demo.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.hlkb40_demo.BluetoothLeClass;
import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.activity.DeviceScanActivity;
import com.example.hlkb40_demo.utilInfo.Utils;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class LedctrActivity extends Activity {
	private BluetoothLeClass bleclass;
	private List<BluetoothGattCharacteristic> gattCharacteristics;
	private BluetoothGattCharacteristic mgattCharacteristic = null;
	private BluetoothGattDescriptor descriptor = null;
	private RadioGroup ledrg = null;
	private RadioButton typebreathe = null;
	private RadioButton typelight = null;
	private RadioButton typemarquee = null;
	private Button totalbton = null;
	private Button lightbton0 = null;
	private Button lightbton1 = null;
	private Button lightbton2 = null;
	private Button totalbtoff = null;
	private Button lightbtoff0 = null;
	private Button lightbtoff1 = null;
	private Button lightbtoff2 = null;
	private int buttonId;
	private TextView lighttv0 = null;
	private SeekBar ledseekBar0 = null;
	private TextView lighttv1 = null;
	private SeekBar ledseekBar1 = null;
	private TextView lighttv2 = null;
	private SeekBar ledseekBar2 = null;
	private byte[] light = new byte[5];
	private byte[] ledlihgt = new byte[5];
	private byte[] ledtype = new byte[5];
	private BluetoothGattCharacteristic readgattCharacteristic = null;
	private final static String UUID_LED_CTR_DATA = "0000ffd2-0000-1000-8000-00805f9b34fb";
	private final static String UUID_LED_CTR_ACK = "00002902-0000-1000-8000-00805f9b34fb";
	private final static String UUID_LED_CTR_NOTF = "0000ffd1-0000-1000-8000-00805f9b34fb";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ledctrlayout);
		lighttv0 = (TextView) findViewById(R.id.lighttv0);
		ledseekBar0 = (SeekBar) findViewById(R.id.ledseekBar0);
		ledseekBar0.setOnSeekBarChangeListener(new OnSeekBarChangeListenerimp());
		lighttv1 = (TextView) findViewById(R.id.lighttv1);
		ledseekBar1 = (SeekBar) findViewById(R.id.ledseekBar1);
		ledseekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListenerimp());
		lighttv2 = (TextView) findViewById(R.id.lighttv2);
		ledseekBar2 = (SeekBar) findViewById(R.id.ledseekBar2);
		ledseekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListenerimp());
		ledrg = (RadioGroup) findViewById(R.id.ledrg);
		typebreathe = (RadioButton) findViewById(R.id.typebreathe);
		typelight = (RadioButton) findViewById(R.id.typelight);
		typemarquee = (RadioButton) findViewById(R.id.typemarquee);
		totalbton = (Button) findViewById(R.id.totalbton);
		lightbton0 = (Button) findViewById(R.id.lightbton0);
		lightbton1 = (Button) findViewById(R.id.lightbton1);
		lightbton2 = (Button) findViewById(R.id.lightbton2);
		totalbtoff = (Button) findViewById(R.id.totalbtoff);
		lightbtoff0 = (Button) findViewById(R.id.lightbtoff0);
		lightbtoff1 = (Button) findViewById(R.id.lightbtoff1);
		lightbtoff2 = (Button) findViewById(R.id.lightbtoff2);
		totalbton.setOnClickListener(new OnclickListenerimp());
		lightbton0.setOnClickListener(new OnclickListenerimp());
		lightbton1.setOnClickListener(new OnclickListenerimp());
		lightbton2.setOnClickListener(new OnclickListenerimp());
		totalbtoff.setOnClickListener(new OnclickListenerimp());
		lightbtoff0.setOnClickListener(new OnclickListenerimp());
		lightbtoff1.setOnClickListener(new OnclickListenerimp());
		lightbtoff2.setOnClickListener(new OnclickListenerimp());
		ledrg.setOnCheckedChangeListener(new OnCkeckedChangeListenerimp());
		bleclass = DeviceScanActivity.getInstance().mBLE;
		List<BluetoothGattService> gattServices = new ArrayList<>();
		for (BluetoothGattService gatt : gattServices) {
			gattCharacteristics = gatt.getCharacteristics();
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				if (gattCharacteristic.getUuid().toString()
						.equals(UUID_LED_CTR_DATA)) {
					mgattCharacteristic = gattCharacteristic;
					setTitle("找到端口");
				} else if (gattCharacteristic.getUuid().toString()
						.equals(UUID_LED_CTR_NOTF)) {
					descriptor = gattCharacteristic.getDescriptor(UUID
							.fromString(UUID_LED_CTR_ACK));

				}
				if (descriptor != null) {
					readgattCharacteristic = gattCharacteristic;
					bleclass.setCharacteristicNotification(gattCharacteristic,
							true);
					descriptor
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					bleclass.writeDescriptor(descriptor);
				}
			}
		}
		registerBoradcastReceiver();
	}

	class OnCkeckedChangeListenerimp implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (typebreathe.getId() == checkedId) {
				ledtype[0] = 0x03;
				ledtype[1] = 0x00;
				ledtype[2] = 0x01;
				ledseekBar0.setEnabled(false);
				ledseekBar1.setEnabled(false);
				ledseekBar2.setEnabled(false);
				mgattCharacteristic.setValue(ledtype);
				bleclass.writeCharacteristic(mgattCharacteristic);
			} else if (typelight.getId() == checkedId) {
				ledtype[0] = 0x03;
				ledtype[1] = 0x00;
				ledtype[2] = 0x00;
				ledseekBar0.setEnabled(true);
				ledseekBar1.setEnabled(true);
				ledseekBar2.setEnabled(true);
				mgattCharacteristic.setValue(ledtype);
				bleclass.writeCharacteristic(mgattCharacteristic);
				mgattCharacteristic.setValue(light);
				bleclass.writeCharacteristic(mgattCharacteristic);
			} else if (typemarquee.getId() == checkedId) {
				ledtype[0] = 0x03;
				ledtype[1] = 0x01;
				ledtype[2] = 0x00;
				ledseekBar0.setEnabled(false);
				ledseekBar1.setEnabled(false);
				ledseekBar2.setEnabled(false);
				mgattCharacteristic.setValue(ledtype);
				bleclass.writeCharacteristic(mgattCharacteristic);

			}

		}

	}

	class OnclickListenerimp implements OnClickListener {

		@Override
		public void onClick(View v) {
			buttonId = v.getId();
			if (v.getId() == totalbton.getId()) {
				// totalbt.setText("同时亮");
				ledlihgt[0] = 0x01;
				ledlihgt[1] = 0x01;				
			} else if (v.getId() == totalbtoff.getId()) {
				ledlihgt[0] = 0x01;
				ledlihgt[1] = 0x00;
				ledlihgt[2] = 0x00;
				ledlihgt[3] = 0x00;
			} else if (v.getId() == lightbton0.getId()) {
				ledlihgt[0] = 0x02;
				ledlihgt[1] = 0x01;
			} else if (v.getId() == lightbtoff0.getId()) {
				ledlihgt[0] = 0x02;
				ledlihgt[1] = 0x00;
			} else if (v.getId() == lightbton1.getId()) {
				ledlihgt[0] = 0x02;
				ledlihgt[2] = 0x01;
			} else if (v.getId() == lightbtoff1.getId()) {
				ledlihgt[0] = 0x02;
				ledlihgt[2] = 0x00;
			} else if (v.getId() == lightbton2.getId()) {
				ledlihgt[0] = 0x02;
				ledlihgt[3] = 0x01;
			} else if (v.getId() == lightbtoff2.getId()) {
				ledlihgt[0] = 0x02;
				ledlihgt[3] = 0x00;
			}
			mgattCharacteristic.setValue(ledlihgt);
			bleclass.writeCharacteristic(mgattCharacteristic);
		}

	}

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("state");
		myIntentFilter.addAction("recvdata");
		myIntentFilter.addAction("leddata");
		// myIntentFilter.addAction("ledack");
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
			if (action.equals("recvdata")) {
				System.out.println("recvdata"
						+ Utils.bytesToHexString(readgattCharacteristic
								.getValue()));
				// baseaddr = readgattCharacteristic.getValue();
				// setRecv_data(1);
				// recv_count += baseaddr.length;
				// rf.savefile(baseaddr, baseaddr.length);
				// System.out.println("borad -->" + count++);
				// System.out.println("ble -->" +
				// Utils.bytesToHexString(bleclass.temp));
				// _txtRead.append(" " + recv_count + '\n');
				//
			}
			if (action.equals("leddata")) {
				System.out.println("led ");

				light = mgattCharacteristic.getValue();
				int progress = light[0] & 0xFF;
				System.out.println("hhh " + light[1]);
				ledseekBar0.setProgress(progress);
				lighttv0.setText("当前亮度:" + progress);
			}
			
		}

	};

	@Override
	protected void onDestroy() {
		bleclass.disconnect();
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	class OnSeekBarChangeListenerimp implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (seekBar.getId() == ledseekBar0.getId()) {
				light[0] = 0x04;
				light[1] = (byte) (progress & 0x000000ff);
				System.out.println("progress" + progress);				
			} else if (seekBar.getId() == ledseekBar1.getId()) {
				light[0] = 0x04;
				light[2] = (byte) (progress & 0x000000ff);
				System.out.println("progress" + progress);				
			} else if (seekBar.getId() == ledseekBar2.getId()) {
				light[0] = 0x04;
				light[3] = (byte) (progress & 0x000000ff);
				System.out.println("progress" + progress);
			}
			mgattCharacteristic.setValue(light);
			bleclass.writeCharacteristic(mgattCharacteristic);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// System.out.println("ssssss");

		}

	}

}
