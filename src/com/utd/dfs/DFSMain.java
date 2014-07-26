package com.utd.dfs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.nio.sctp.SctpChannel;
import com.utd.dfs.logicalclock.LogicalClock;
import com.utd.dfs.utils.ConnectionManager;
import com.utd.dfs.utils.NodeDetails;


public class DFSMain {
	
	/**
	 * applicationRunning=true, Server will listen for requests
	 * applicationRunning=false, normally happens when u quit the application
	 */
	private boolean applicationRunning;
	
	/**
	 * This is the total number of nodes in topology read from config.
	 */
	private int totalNodes;
	/**
	 * contains information about current Node read from config.
	 */
	private  NodeDetails currentNode;
	
	/**
	 * contains information about coordinator Node read from config.
	 */
	private  NodeDetails coordinatorNode;
	
	/**
	 * This is the map of all Nodes Present in the Topology
	 */
	private  HashMap<Integer, NodeDetails> mapNodes;
	/**
	 * This is the map of all Nodes Present in the Topology
	 */
	 private HashMap<String, NodeDetails> mapNodesByAddress;
	
	/**
	 * This is the map of SCTP Connections.Each Process will contain the connection objects
	 */
	private ConcurrentHashMap<Integer, SctpChannel> connectionSocket;
	
	/**
	 * Lamport's logical clock
	 * initial value=0
	 * On send event : +1
	 * On recieve event : Max(currentval,valFromMessage)+1
	 */
	private LogicalClock LC;//Lamport's Logical Clock

	
	public DFSMain(){
		applicationRunning=true;
		totalNodes=0;//read from topology.txt later
		mapNodes=new HashMap<Integer, NodeDetails>();
		mapNodesByAddress=new HashMap<String, NodeDetails>();
		connectionSocket=new ConcurrentHashMap<Integer, SctpChannel>();
	}
	

	/**
	 * FileAppend This class writes to files
	 * 
	 * @author Anupam Gangotia Profile::http://en.gravatar.com/gangotia
	 *         github::https://github.com/agangotia
	 * 
	 * @author Dilip Profile:: github::
	 * 
	 * @author Rashmi Profile:: github::
	 */
	public static void main(String[] args) {
		if (args.length != 1) {

			System.out
					.println("Inappropriate arguement passed, please pass only 1 arguement");
			return;
		}
		
		DFSMain objMain=new DFSMain();

		if (!objMain.readConfig(Constants.TOPOLOGYFILE, Integer.parseInt(args[0]))) {
			System.out
			.println("Error in reading file "+Constants.TOPOLOGYFILE);
			System.out
			.println("Exit");
			return;
		}
		
		if(!ConnectionManager.createConnections(objMain.currentNode, objMain.connectionSocket,objMain.mapNodes)){
			System.out
			.println("Error in creating connections");
			System.out
			.println("Exit");
			return;
		}

	}
	
	
	public boolean readConfig(String fileName,int nodeID){
		System.out.println("Reading config for"+nodeID);
		BufferedReader bReader = null;
		int nodesCount=0;
		try {
			bReader = new BufferedReader(new FileReader(fileName));
			String line = bReader.readLine();
			boolean firstLine=true;
			while(line!=null){
				if(firstLine){
					firstLine=false;
				}else{
					StringTokenizer st = new StringTokenizer(line, ",");
					//1.NODE ID
					int nodeIDLoop=Integer.parseInt((String) st.nextElement());
					//2.IP ADDRESS
					String address=(String)st.nextElement();
					//3.PORT NO
					int portNumber=Integer.parseInt((String) st.nextElement());	
					//4.DELAYFAIL 
					int delayFail=Integer.parseInt((String) st.nextElement());	
					//5.MYVOTES 
					int myVotes=Integer.parseInt((String) st.nextElement());
					//6.TOTALVOTES
					int totalVotes=Integer.parseInt((String) st.nextElement());
					//8.IS_COORDINATOR
					char isCoordinator=((String) st.nextElement()).charAt(0);
					
					NodeDetails nodeObj=new NodeDetails(nodeIDLoop, portNumber, address,delayFail,myVotes,totalVotes,isCoordinator);
					mapNodes.put(nodeID,nodeObj);
					mapNodesByAddress.put(address+String.valueOf(portNumber),nodeObj);
					nodesCount++;
					
					if(isCoordinator=='Y')
						coordinatorNode=nodeObj;
				}
				line = bReader.readLine();
				if(line!=null && line.length()==0)
					break;
				}
			
			this.totalNodes=nodesCount;
			//System.out.println("Total Nodes"+totalNodes);
			//All the Node info has been filled
			if(mapNodes.containsKey(nodeID))
				currentNode=mapNodes.get(nodeID);
			else{
				
				System.out.println("*********************************************************");
				System.out.println("Please Supply the correct Process ID"+nodeID);
				System.out.println("*********************************************************");
				System.out.println("Exiting");
				return false;
			}
			}catch (IOException e) {
			e.printStackTrace();
			System.out.println("*********************************************************");
			System.out.println("Exception in reading config"+e.toString());
			return false;
		} finally {
			try {
				if (bReader != null)
					bReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		return true;
	}

}