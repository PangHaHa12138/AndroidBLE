package com.example.hlkb40_demo.utilInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Environment;

public class FileUtil {
	/**
	 * 
	 * @param path
	 * @return
	 */
	private static String sdcardPath = null;
	public static List<File> getFileList(String path){
		
		List<File> fileList = new ArrayList<File>();
		File[] files = new File(path).listFiles();
		if(files == null){
			sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		    files = new File(sdcardPath).listFiles();
		}
		if(files.length > 0){
			List<File> allFolder = new ArrayList<File>();
			List<File> allFile = new ArrayList<File>();
			for(File file : files){
				if(file.isFile()){
					if(!file.getName().startsWith(".")  && file.getName().endsWith(".bin")){
						allFile.add(file);
					}
				}else {
					if(!file.getName().startsWith(".")){
						allFolder.add(file);
					}
				}
			}
			Collections.sort(allFolder,new Comparator<File>() {
	            public int compare(File arg0, File arg1) {
	                return arg0.getName().compareToIgnoreCase(arg1.getName());
	            }
			});
			Collections.sort(allFile,new Comparator<File>() {
	            public int compare(File arg0, File arg1) {
	                return arg0.getName().compareToIgnoreCase(arg1.getName());
	            }
			});
			fileList.addAll(allFolder);
			fileList.addAll(allFile);
			
		}
		
		return fileList;
	}
	
	
}
