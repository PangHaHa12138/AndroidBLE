/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.hlkb40_demo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import com.example.hlkb40_demo.activity.BluetoothservicesListview;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLeClass {
	private final static String TAG = BluetoothLeClass.class.getSimpleName();
	private String mBluetoothDeviceAddress;
	/** 搜索BLE终端 */
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	public byte[] temp = null;
	public boolean isDisconnected = false;
	public int mtuSize  = 247;
	public boolean mtuChange = false;

	public static String strResultInfoByStatus(int status) {
		String strInfo = "";
		switch (status) {
			case 0:strInfo = "A GATT operation completed successfully";break;
			case 2:strInfo = "GATT read operation is not permitted";break;
			case 3:strInfo = "GATT write operation is not permitted";break;
			case 5:strInfo = "Insufficient authentication for a given operation";break;
			case 6:strInfo = "The given request is not supported";break;
			case 7:strInfo = "A read or write operation was requested with an invalid offset";break;
			case 0xf:strInfo = "Insufficient encryption for a given operation";break;
			case 0xd:strInfo = "A write operation exceeds the maximum length of the attribute";break;
			case 0x8f:strInfo = "A remote device connection is congested.";break;
			case 0x101:strInfo = "A GATT operation failed, errors other than the above";break;
		}
		return strInfo;
	}

	public interface OnConnectListener {
		public void onConnectting(BluetoothGatt gatt,int status,int newState);
		public void onConnected(BluetoothGatt gatt,int status,int newState);
		public void onDisconnect(BluetoothGatt gatt,int status,int newState);
	}

	public interface OnConnectingListener {
		public void onConnecting(BluetoothGatt gatt);
	}

	public interface OnDisconnectListener {
		public void onDisconnect(BluetoothGatt gatt);
	}

	public interface OnServiceDiscoverListener {
		public void onServiceDiscover(BluetoothGatt gatt);
	}
    public interface OnRecvDataListerner{
    	public void OnCharacteristicRecv(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic);
    	
    }
	public interface OnDataAvailableListener {
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status);

//		public void onCharacteristicWrite(BluetoothGatt gatt,
//				BluetoothGattCharacteristic characteristic);
	}
	public interface OnWriteDataListener{
		public void OnCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status);
		
	}
	public interface OnChangeMTUListener {
		public void onChangeMTUListener(Boolean isResult,String strMsg,int iMTU);
	}
	private OnConnectListener mOnConnectListener;
	private OnServiceDiscoverListener mOnServiceDiscoverListener;
	private OnDataAvailableListener mOnDataAvailableListener;
	private OnRecvDataListerner mOnRecvDataListerner;
	private OnWriteDataListener mOnWriteDataListerner;
	private OnChangeMTUListener mOnChangeMTUListener;
	private Context mContext;

	public void setOnConnectListener(OnConnectListener l) {
		mOnConnectListener = l;
	}

	public void setOnWriteDataListener(OnWriteDataListener l){
		mOnWriteDataListerner = l;
		
	}

	public void setOnServiceDiscoverListener(OnServiceDiscoverListener l) {
		mOnServiceDiscoverListener = l;
	}

	public void setOnChangeMTUListener(OnChangeMTUListener l) {
		mOnChangeMTUListener = l;
	}

	public void setOnDataAvailableListener(OnDataAvailableListener l) {
		mOnDataAvailableListener = l;
	}
    public void setOnRecvDataListener(OnRecvDataListerner l){
    	mOnRecvDataListerner = l;
    	
    }


	public void setUnConnectListener() {
		mOnConnectListener = null;
	}

	public void setUnWriteDataListener(){
		mOnWriteDataListerner = null;

	}

	public void setUnServiceDiscoverListener() {
		mOnServiceDiscoverListener = null;
	}

	public void setUnChangeMTUListener() {
		mOnChangeMTUListener = null;
	}

	public void setUnDataAvailableListener() {
		mOnDataAvailableListener = null;
	}
	public void setUnRecvDataListener(){
		mOnRecvDataListerner = null;

	}

	public BluetoothLeClass(Context c,BluetoothAdapter mBluetoothAdapter) {
		mContext = c;
		this.mBluetoothAdapter = mBluetoothAdapter;
	}

	void updatastata(String action) {
		Intent intent = new Intent(action);
		mContext.sendBroadcast(intent);

	}
	int j = 0;
	public int i = 0;
	@SuppressLint("MissingPermission")
	public boolean requestMtu(int size) {
		if (mBluetoothGatt != null && Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) {
			return mBluetoothGatt.requestMtu(size);
		}
		return false;
	}

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		/**
		 * 断开或连接 状态发生变化时调用
		 * */
		@SuppressLint("MissingPermission")
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.e(TAG, "onConnectionStateChange:status: "+status+",newState:"+newState);
			mBluetoothGatt = gatt;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				if (mOnConnectListener != null)
					mOnConnectListener.onConnected(gatt,status,newState);
				Log.e(TAG, "Connected to GATT server.");

				isDisconnected = false;

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.e(TAG, "Disconnected from GATT server.");
				if (mOnConnectListener != null)
					mOnConnectListener.onDisconnect(gatt,status,newState);
				isDisconnected = true;
				gatt.close();
				updatastata("state");
			} else if (newState == BluetoothProfile.STATE_CONNECTING) {
				Log.e(TAG, "Connecting from GATT server.");
				if (mOnConnectListener != null) {
					mOnConnectListener.onConnectting(gatt,status,newState);
				}
			}
		}

		/**
		 * 发现指定设备的服务信息（真正建立连接）
		 * */
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.e(TAG, "onServicesDiscovered:status: "+status);
			if (status == BluetoothGatt.GATT_SUCCESS
					&& mOnServiceDiscoverListener != null) {
				mOnServiceDiscoverListener.onServiceDiscover(gatt);
			} else {
				Log.e(TAG, "onServicesDiscovered received: " + status);
			}
		}

		/**
		 * 读操作的回调
		 * */
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (mOnDataAvailableListener != null)
				mOnDataAvailableListener.onCharacteristicRead(gatt,
						characteristic, status);
		}
		/**
		 * 写操作的回调
		 * */
	    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
	    	if(mOnWriteDataListerner != null){
	    		mOnWriteDataListerner.OnCharacteristicWrite(gatt, characteristic, status);
	    	}
	    }
		/**
		 * 接收到硬件返回的数据
		 * */
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			if(mOnRecvDataListerner != null){	
				mOnRecvDataListerner.OnCharacteristicRecv(gatt, characteristic);
			}
//			 if (mOnDataAvailableListener!=null)
//			 mOnDataAvailableListener.onCharacteristicWrite(gatt,
//			characteristic);
			// rf.savefile(temp, temp.length);
		}
		@SuppressLint("SuspiciousIndentation")
		@Override
		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
		super.onMtuChanged(gatt, mtu, status);
			String strValue = "";
		    System.out.println("onMtuChanged "+mtu + " " + status);
		    Boolean isResult = (BluetoothGatt.GATT_SUCCESS == status);
			if (BluetoothGatt.GATT_SUCCESS == status) {
				mtuSize = mtu;
				strValue = "onMtuChanged success MTU = " + mtu;
			}else {
				mtuSize = 20;
			    Log.d("BleService", "onMtuChanged fail ");
				strValue = "onMtuChanged fail ";
			}

			if (mOnChangeMTUListener != null) {
				mOnChangeMTUListener.onChangeMTUListener(isResult,strValue,mtuSize);
			}

		}
	};


	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	@SuppressLint("MissingPermission")
	public boolean connect(BluetoothDevice device, OnConnectListener l) {
		if (l != null)
			mOnConnectListener = l;

		mBluetoothGatt = device.connectGatt(mContext,false, mGattCallback);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			mBluetoothGatt = device.connectGatt(mContext,
//					true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
//			Log.e(TAG, "start connect!");
//		} else {
//			mBluetoothGatt = device.connectGatt(mContext,
//					true, mGattCallback);
//		}
		return true;
	}

	@SuppressLint("MissingPermission")
	public boolean getServiceByGatt() {
		Boolean isDiscover = false;
		if (mBluetoothGatt != null) {
			isDiscover = mBluetoothGatt.discoverServices();
			Log.i(TAG, "Attempting to start service discovery:" + isDiscover);
		}
		return isDiscover;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	@SuppressLint("MissingPermission")
	public void disconnect() {
		System.out.println("disconnect");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}

	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	@SuppressLint("MissingPermission")
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	@SuppressLint("MissingPermission")
	public Boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		return mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	@SuppressLint("MissingPermission")
	public Boolean setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}

		boolean isNotification = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		return isNotification;
	}

	@SuppressLint("MissingPermission")
	public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}

	@SuppressLint("MissingPermission")
	public boolean writeDescriptor(BluetoothGattDescriptor gattDescriptor) {
		return mBluetoothGatt.writeDescriptor(gattDescriptor);
	}
	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();
	}
}
