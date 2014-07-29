package com.utd.dfs.fs;
import java.util.concurrent.ConcurrentHashMap;

import com.utd.dfs.DFSMain;
import com.utd.dfs.msg.Message;
import com.utd.dfs.statustrackers.Status;
/**
 * This is the Main Communication Provider and Status Checker.
 * For Each  Read Or Write Requests,
 * This class maintains the status,
 * and has the functions called by STATIC VOTING PROTOCOL.
 * 
 * @author Anupam Gangotia Profile::http://en.gravatar.com/gangotia
 *         github::https://github.com/agangotia
 * 
 * @author Dilip Profile:: github::
 * 
 * @author Rashmi Profile:: github::
 */
public class DFSCommunicator {

	/**
	 * Map of FIle Operations Status.
	 * Used in :
	 * 1.ReadWrite thread -> Status entry is created
	 * 2.Receiver thread -> Status entry is Updated
	 * Upon Completion of an operation,
	 * Readwrite thread does the task of removing that key,value pair from this map.
	 */
	public static ConcurrentHashMap<String,Status> mapFileStatus=new ConcurrentHashMap<String,Status>();
	
	/**
	 * Function : Broadcast All nodes, asking for votes for a read operation.
	 */
	public static void broadcastReadRequestForVotes(String fileName){
		for (Integer key : DFSMain.mapNodes.keySet()) {
			if(key!=DFSMain.currentNode.getNodeID()){
				Message m=new Message("0", DFSMain.currentNode.getNodeID(), DFSMain.mapNodes.get(key).getNodeID(),
						0, "", fileName);
				DFSMain.sendQueue.add(m);
			}
		    
		}
	}
	
	/**
	 * Function : Broadcast All nodes, asking for votes for a write operation.
	 */
	public static void broadcastWriteRequestForVotes(String fileName){
		for (Integer key : DFSMain.mapNodes.keySet()) {
			if(key!=DFSMain.currentNode.getNodeID()){
				Message m=new Message("0", DFSMain.currentNode.getNodeID(), DFSMain.mapNodes.get(key).getNodeID(),
						10, "", fileName);
				DFSMain.sendQueue.add(m);
			}
		    
		}
	}
}