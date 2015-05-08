package com.mykj.andr.model;

import java.util.List;

public class NewUIDataStruct {

	public boolean showCard=false;  //显示为列表还是卡片    true 卡片， false 列表
	public NodeData mNodeData;   //房间节点数据
	public String Name="";       // 名称
	//2012-12-21 新增属性，用来判断报名节点标记<报名，退赛>
	public byte Type; // 节点类型
	public int ID;            // 节点标识
	public int ParentID;      // 父节点
	public List<NodeData> mSubNodeDataList;
	
	public NewUIDataStruct(NodeData nodeData){
		this.ID=nodeData.ID;
		this.ParentID=nodeData.ParentID;
		this.Name=nodeData.Name;
		this.Type=nodeData.Type;
		this.mNodeData=nodeData;
	}

}
