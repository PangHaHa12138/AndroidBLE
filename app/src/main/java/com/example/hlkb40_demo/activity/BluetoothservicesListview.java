package com.example.hlkb40_demo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.utilInfo.Utils;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BluetoothservicesListview extends Activity{ 
	private List<Map<String,String>> list;
	private ListView serviceslv = null;
	static List<BluetoothGattCharacteristic> gattCharacteristics;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActionBar().setTitle(R.string.connected);
        setContentView(R.layout.bluetoothservices);
        registerBoradcastReceiver(); 
        serviceslv = (ListView) findViewById(R.id.serviceslv);
        list = new ArrayList<Map<String,String>>();
        List<BluetoothGattService> gattServices = new ArrayList<>();
        for(BluetoothGattService gatt : gattServices){
        	gattCharacteristics = gatt.getCharacteristics();
        	for (final BluetoothGattCharacteristic  gattCharacteristic: gattCharacteristics) {
        		int property = gattCharacteristic.getProperties();
	        	Map<String,String> map = new HashMap<String, String>();
	        	int type = gatt.getType();
	        	map.put("service", Utils.getServiceType(type));
	        	map.put("uuid","UUID: 0x" + gatt.getUuid().toString().substring(4, 8));
	        	map.put("permiss",Utils.getCharPropertie(property));
	        	list.add(map);	
        	}
        }
//        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.serviceslistview,new String[]{"service","uuid","permiss"},new int[]{R.id.servicetv,R.id.uuidtv,R.id.permisstv});
//
//        serviceslv.setAdapter(simpleAdapter);
        serviceslv.setOnItemClickListener(new OnItemClickListenerimp());
	}
	  public void registerBoradcastReceiver(){  
	        IntentFilter myIntentFilter = new IntentFilter();  
	        myIntentFilter.addAction("state");  
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
		        }  
		          
		    };
	private class OnItemClickListenerimp implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int  position,
				long arg3) {
			Intent intent = new Intent(BluetoothservicesListview.this,ReadWriteActivity.class);
			intent.putExtra("value", position);
			startActivity(intent);
		}
		
		
	}
	    
}
