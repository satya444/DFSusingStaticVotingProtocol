package com.utd.dfs;

public interface Constants {
	
	String FILESEPARATOR="//";//Change this to \\ for windows 
	
	String TOPOLOGYFILE="data"+FILESEPARATOR+"topology.txt";
	String FILECONFIG="data"+FILESEPARATOR+"initialFileconfig.txt";
	String FAILUREFILE="data"+FILESEPARATOR+"failure.txt";
	
	boolean TESTSENDERRECEIVER=false;
	int SIZESENDQ=100;
	int SIZESRECVQ=100;
	Long timeOut=2000L;
	
	
	//for File System
	int FILEMININDEX=0;
	int FILEMAXINDEX=2;
	
	
	//For Logger
	String LOGFILERWTHREAD="log"+FILESEPARATOR+"logRWNode";
	String LOGFILEEND=".log";
	String LOGFILERCVR="log"+FILESEPARATOR+"logReceiverNode";
	String LOGFILEMAIN="log"+FILESEPARATOR+"logMAIN";
	String logFileRWThread="log"+FILESEPARATOR+"logThreads";
	
	//For Testing
	String LOGTEST="test"+FILESEPARATOR+"logRWNode";
	String LOGTESTEND=".log";
	
	boolean DISABLEMONITORTHREAD=false;
	
}