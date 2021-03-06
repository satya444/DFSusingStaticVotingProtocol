package com.utd.dfs.fs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.utd.dfs.Constants;
import com.utd.dfs.DFSMain;
import com.utd.dfs.statustrackers.Status;
import com.utd.dfs.statustrackers.StatusGetFile;
import com.utd.dfs.statustrackers.StatusReadWriteQuorumRequest;
import com.utd.dfs.utils.FileFeatures;
import com.utd.dfs.utils.FileOperationsCount;
import com.utd.dfs.utils.NodeDetails;


public class FileSystem {
	
	/**
	 * Map of File name and file object
	 */
	 public static Map<String,DFSFile> fsobject=new HashMap<String,DFSFile>();
	
	 /**
		 * Map of File name and its versions.
		 * Initially viersion =0,
		 * check and remove 
		 */
	 public static HashMap<String,Integer> myFileVersions=new HashMap<String,Integer>();
	


	 
	 /**
	 * Set determines the status of the files that are currently locked 
	 * @param fname
	 * @return
	 */
	public static Map<String,String> map_filestatus= new HashMap<String,String>();//
	
	 /**
		 * Fills the File system with data
		 */
	public static boolean buildFileSystem(){
		
		
		BufferedReader bReader = null;
		
		try {
			bReader = new BufferedReader(new FileReader(Constants.FILECONFIG));
		
		String line = bReader.readLine();
		while(line!=null){
				StringTokenizer st = new StringTokenizer(line, ",");
				String fileName=st.nextToken();
				String content=st.nextToken();
		
				DFSFile file=new DFSFile(fileName, 0, content);
				fsobject.put(fileName, file);
				FileFeatures.appendText("fs"+Constants.FILESEPARATOR+DFSMain.currentNode.getNodeID()+Constants.FILESEPARATOR+fileName, content);
			line = bReader.readLine();
			if(line!=null && line.length()==0)
				break;
			}
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public static boolean getStatus(String fname){
		
		
		if(map_filestatus.get(fname).equals("pending")){
			releaseTimeOutLock(fname);
	
			return false;
		}
		else{
		return true;
		}
	}
	
	public static void releaseTimeOutLock(String fName){
		long time=System.currentTimeMillis();
		DFSFile fObj=fsobject.get(fName);
		if(time-fObj.lockedTime>5000l){
			if(fObj.readLockCount>0)
				fObj.relaseRead();
			/*if(fObj.rwl.isWriteLocked())
				fObj.releaseWrite();*/
			
		}
	}
	
	public static void releaseAllLocks(){
		synchronized(FileSystem.map_filestatus){
		map_filestatus.clear();
		}
		for(String s:fsobject.keySet()){
			DFSFile fObj=fsobject.get(s);
			fObj.readLockCount=0;
			fObj.releaseWrite();
		}
		System.out.println("RELEASING ALL LOCKS");
		
	}
	public static synchronized boolean lock(String file_name, String lock_type){
		DFSFile file_obj=null;
		try{
			file_obj= fsobject.get(file_name);
			System.out.println("1...");
			if(lock_type.equals("R")){
				//if(file_obj.readLockCount==0)
					//file_obj.rwl.readLock().lock();
				file_obj.readLockCount++;
				file_obj.lockedTime=System.currentTimeMillis();
				return true;
			}
			else{
				System.out.println("2...");
				file_obj.cacheddata=file_obj.getData();
				if(!file_obj.rwl.writeLock().tryLock()){
					System.out.println("Lock Status"+FileSystem.getStatus(file_name));
					//releaseTimeOutLock(file_name);
					return false;
				}
				file_obj.lockedTime=System.currentTimeMillis();
				System.out.println("2...");
				return true;
			}
			
			
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
	}
	
	public static String getCachedData(String filename){
		DFSFile file_obj= fsobject.get(filename);
		return file_obj.cacheddata;
	}
	public static void checkout(Status foc,String mapKeyIdentifier){
		
		
		if(DFSMain.currentNode.getNodeID()!=foc.getMaxVersionNodeId()){
			
			System.out.println("OOPS I don't have the Latest.. Will ask from network");
			System.out.println(foc.getMaxVersionNodeId() +" Has the latest, with version ");
			
			//get the latest version from node.. call function in consistency manager class
			int version=foc.getMaxVersionNodeId();
			
			Object o=new Object();
			Status objStatus=new StatusGetFile(foc.getFileName(),o);
			if(mapKeyIdentifier!=null)
				DFSCommunicator.mapFileStatus.put(mapKeyIdentifier, objStatus);
			else{
				System.out.println("************error*********");
				System.out.println("************map key is null*********");
			}
	
			DFSCommunicator.unicastGetlatestForRead(foc.getMaxVersionNodeId(), foc.getFileName(),objStatus,mapKeyIdentifier);
			synchronized(o){
				try {
					o.wait();
					
					String data=objStatus.getContentOfFile(foc.getMaxVersionNodeId());
					/*bup(foc.getFileName());
					fsobject.get(foc.getFileName()).setFile_version(version);
					fsobject.get(foc.getFileName()).setData(data);*/
					System.out.println("Check out gives Version "+objStatus.getVersionOfFile(foc.getMaxVersionNodeId()));
					System.out.println("Check out gives Date "+data);
					write(foc.getFileName(),data,objStatus.getVersionOfFile(foc.getMaxVersionNodeId()));
					DFSCommunicator.mapFileStatus.remove(mapKeyIdentifier);
					
					}catch(InterruptedException ex){
						ex.printStackTrace();
						DFSCommunicator.mapFileStatus.remove(mapKeyIdentifier);
					}
			}
		}else{
			System.out.println("CheckOut I have the Latest .. Hurray.");
		}
	
	}
	
	public static String read(String file_name){
		DFSFile file_obj= fsobject.get(file_name);
		return file_obj.read();
	}
	
	public static void append(String file_name,String data){
		DFSFile file_obj= fsobject.get(file_name);
		 file_obj.append( data);
	}
	
	public static void write(String file_name,String data,int version){
		DFSFile file_obj= fsobject.get(file_name);
		 file_obj.write(data,version);
	}
	public static  void checkin(){
	 
	}
	
	public static void bup(String file_name){
		DFSFile file_obj= fsobject.get(file_name);
		 file_obj.backup_original();
	}
	
	public static void restorePreviousVersion(String file_name){
		DFSFile file_obj= fsobject.get(file_name);
		file_obj.restorePreviousVersion();
	}
	
	public static void releaseWriteLock(String fileName){
		DFSFile file_obj= fsobject.get(fileName);
		 file_obj.releaseWrite();
	}
	public static void releaseReadLock(String fileName){
		DFSFile file_obj= fsobject.get(fileName);
		if(file_obj!=null)
			file_obj.relaseRead();
		else{
			System.out.print("!!!OOPs File Object is null");
		}
	}
	
	
	public static boolean getWriteLockStatus(String fileName){
		releaseTimeOutLock(fileName);
		DFSFile file_obj= fsobject.get(fileName);
		return file_obj.rwl.isWriteLocked();
	}
	
	public static boolean getReadLockStatus(String fileName){
		DFSFile file_obj= fsobject.get(fileName);
		if( file_obj.readLockCount>0)
			return true;
		else 
			return false;
	}
	
	public static int getVersionForFile(String File){
		DFSFile file_obj= fsobject.get(File);
		return file_obj.getFile_version();
	}

	public static void setVersionForFile(String File,int version){
		DFSFile file_obj= fsobject.get(File);
		file_obj.file_version_old=file_obj.getFile_version();
		file_obj.setFile_version(version);
	}
	
	public static DFSFile getFileObject(String File){
		return fsobject.get(File);
	}
	
	public static void printLockStatus(){
		System.out.println("Printing Lock Status");
		for (String s:fsobject.keySet()){
			System.out.println("FileName"+s);
			System.out.println("Read Lock Status"+fsobject.get(s).readLockCount);
			System.out.println("Write Lock Status"+fsobject.get(s).rwl.isWriteLocked());
		}
		
	}
}
