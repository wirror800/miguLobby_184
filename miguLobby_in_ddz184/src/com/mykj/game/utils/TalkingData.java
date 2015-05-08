package com.mykj.game.utils;

import android.content.Context;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.comm.util.TCAgentUtil;

public class TalkingData {
     private static TalkingData instance;
     
 	
 	
     private TalkingData(Context context){
    	 TCAgentUtil.initTCAgent(context);
     }
     
     
     public static TalkingData getInstance(Context context){
    	 if(instance==null){
    		 instance=new TalkingData(context);
    	 }
    	 return instance;
     }
     
     
     public void talkingData(String str){
    	  NodeData node=HallDataManager.getInstance().getCurrentNodeData();
    	  if(node!=null){
    		  String s=node.Name+"-"+str;
    		  TCAgentUtil.onTCAgentEvent(s);
    	  }
    	
     }
     
   
	
	
}
