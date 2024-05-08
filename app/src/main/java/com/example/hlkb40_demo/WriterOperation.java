package com.example.hlkb40_demo;

import java.io.FileInputStream;

import com.example.hlkb40_demo.activity.ReadWriteActivity;

import android.bluetooth.BluetoothGattCharacteristic;

public class WriterOperation {
	public final static int OTA_CMD_NVDS_TYPE = 0;
	public final static int OTA_CMD_GET_STR_BASE = 1;
	public final static int OTA_CMD_PAGE_ERASE = 3;
	public final static int OTA_CMD_CHIP_ERASE = 4;
	public final static int OTA_CMD_WRITE_DATA = 5;
	public final static int OTA_CMD_READ_DATA = 6;
	public final static int OTA_CMD_WRITE_MEM = 7;
	public final static int OTA_CMD_READ_MEM  = 8;
	public final static int OTA_CMD_REBOOT = 9;
	public final static int OTA_CMD_NULL = 10;
	byte [] context = new  byte[256]; 
	private ReadWriteActivity rw;
	private byte[] cmd_write_op(int opcode,int length,int addr,int datalenth){
		byte [] cmd;
		if(opcode == OTA_CMD_PAGE_ERASE){
			cmd = new byte[7];
		}else{
			cmd = new byte[9];	
		}
		cmd[0] = (byte) (opcode&0xff);
		cmd[1] = (byte) (length&0xff);
		cmd[2] = (byte) ((length&0xff) >> 8);
		cmd[3] = (byte) (addr&0xff);
		cmd[4] = (byte) ((addr&0xff00) >> 8);
		cmd[5] = (byte) ((addr&0xff0000) >> 16);
		cmd[6] = (byte) ((addr&0xff000000) >> 24);
		if(opcode != OTA_CMD_PAGE_ERASE){
			cmd[7] = (byte) (datalenth&0xff);
			cmd[8] = (byte) ((datalenth&0xff00)>>8);	
		}
		return cmd;
	}
	public byte[] cmd_operation(int type,int lenth,int addr){
		byte[] cmd = null;
		if((type == OTA_CMD_WRITE_MEM) || (type == OTA_CMD_WRITE_DATA)){
			cmd = cmd_write_op(type,9,addr,lenth);
		}else if((type  == OTA_CMD_GET_STR_BASE) || (type  == OTA_CMD_NVDS_TYPE)){
			cmd = cmd_write_op(type,3,0,0);
		}else if(type == OTA_CMD_PAGE_ERASE){
			cmd = cmd_write_op(type,7,addr,0);
		}
		return cmd;
	}
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
	}
	public boolean send_data(int type,int addr,byte[] buffer,int length,BluetoothGattCharacteristic mgattCharacteristic,BluetoothLeClass bleclass){
		byte[] cmd_write = null; 
		byte[] result_cmd = null;
		byte[] cmd = new byte[1];
		cmd_write = cmd_operation(type,length,addr);
		if((type  == OTA_CMD_GET_STR_BASE) || (type  == OTA_CMD_PAGE_ERASE) || (type  == OTA_CMD_NVDS_TYPE)){
		    result_cmd = cmd_write;
		}else if(type  == OTA_CMD_REBOOT){
			cmd[0] = (byte) (type&0xff);
			result_cmd = cmd;
		}else{
			result_cmd = byteMerger(cmd_write,buffer);
		}
		mgattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		mgattCharacteristic.setValue(result_cmd);
		return bleclass.writeCharacteristic(mgattCharacteristic);
		
	}
	public int bytetoint(byte[] data){
		int addr;
		addr = ((int)data[4] & 0x000000ff);
		addr |= (((int)data[5] & 0x0000ff) << 8);
		addr |= (((int)data[6] & 0x000000ff) << 16);
		addr |= (((int)data[7] & 0x000000ff) << 24);
		return addr;
	}	
	public int bytetochar(byte[] data){
		int value;
		value = ((int)data[4] & 0x000000ff);
		return value;
	}
}
