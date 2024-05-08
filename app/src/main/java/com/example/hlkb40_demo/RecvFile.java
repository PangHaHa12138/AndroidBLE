package com.example.hlkb40_demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.os.Environment;

public class RecvFile {
	private String sdPath;
	private  String PATH;
	private OutputStream fos;
	public RecvFile() {
		sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		PATH = sdPath +"/Freqchip/pskeys.dat";
		 File dir = new File(sdPath +"/Freqchip/");
		 if(!dir.exists()){
			 dir.mkdirs();
		}
		 File file = new File(PATH);
		 try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	public void savefile(byte[] buffer,int length){
		try {
			fos.write(buffer,0,length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
