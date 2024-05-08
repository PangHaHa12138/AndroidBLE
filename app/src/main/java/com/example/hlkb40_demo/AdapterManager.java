package com.example.hlkb40_demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.example.hlkb40_demo.utilInfo.FileUtil;
import com.example.hlkb40_demo.adapter.FileListAdapter;


/**
 * 适配器管理器
 * @author 210001001427
 *
 */
public class AdapterManager {
	protected static final String string2 = null;

	private Context mContext;

	private FileListAdapter mFileListAdapter;    //文件列表adapter
	private List<BluetoothDevice> mDeviceList;   //设备集合
	private List<File> mFileList;    //文件集合
	private Handler mainHandler;   //主线程Handler
	
	public AdapterManager(Context context){
		this.mContext = context;
		
	}
	
	/**
	 * 取得设备列表adapter
	 * @return
	
	
	/**
	 * 取得文件列表adapter
	 * @return
	 */
	public FileListAdapter getFileListAdapter(){
		if(null == mFileListAdapter){
			mFileList = new ArrayList<File>();
			mFileListAdapter = new FileListAdapter(mContext, mFileList, R.layout.file_list_item);
		}
		
		return mFileListAdapter;
	}
	
	
	
	/**
	 * 清空设备列表
	 */
	public void clearDevice(){
		if(null != mDeviceList){
			mDeviceList.clear();
		}
	}
	
	/**
	 * 添加设备
	 * @param bluetoothDevice
	 */
	public void addDevice(BluetoothDevice bluetoothDevice){
		mDeviceList.add(bluetoothDevice);
	}
	
	/**
	 * 更新设备信息
	 * @param listId
	 * @param bluetoothDevice
	 */
	public void changeDevice(int listId, BluetoothDevice bluetoothDevice){
		mDeviceList.remove(listId);
		mDeviceList.add(listId, bluetoothDevice);
	}
	/**
	 * 更新文件列表
	 * @param path
	 */

	public void updateFileListAdapter(String path){
		mFileList.clear();
		mFileList.addAll(FileUtil.getFileList(path));
		if(null == mainHandler){
			mainHandler = new Handler(mContext.getMainLooper());
		}
		mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mFileListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 取得设备列表
	 * @return
	 */
	public List<BluetoothDevice> getDeviceList() {
		return mDeviceList;
	}

}
