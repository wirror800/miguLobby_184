package com.mykj.andr.model;

import java.io.Serializable;

/****
 * 
 * @ClassName: CardZoneItem
 * @Description: 卡片区域(顶层节点)
 * @author zhanghuadong
 * @date 2012-9-11 上午11:00:17
 *
 */
public class CardZoneItem  implements Serializable{
	/**
	 * @Fields serialVersionUID:  
	 */
	private static final long	serialVersionUID	= 1L;

	//2012-12-21 新增属性，用来判断报名节点标记<报名，退赛>
	public byte Type; // 节点类型
	public int timeCompare=EQUAL;  //默认0，-1之前，1之后
	
	/**相等**/
    public static final int EQUAL=0;
    /**之前**/
    public static final int BEFORE=-1;  //已经报名，显示退赛
    /**之后**/
    public static final int AFTER=1;
	
	//-------------------------------------------------------------------
	//顶层节点实际包含数据(kjava一致)
	public NodeData mNodeData;
	public String Name="";       // 名称
	public int ID;            // 节点标识
	public int ParentID;      // 父节点
	//-------------------------------------------------------------------
	public CardZoneItem(){}
	public CardZoneItem(NodeData nodeData){
		this.ID=nodeData.ID;
		this.ParentID=nodeData.ParentID;
		this.Name=nodeData.Name;
		this.Type=nodeData.Type;
		this.mNodeData=nodeData;
	}
//	public CardZoneItem(int id,int parentID,String name,
//			NodeData nodeData){
//		this.ID=id;
//		this.ParentID=parentID;
//		this.Name=name;
//		this.Type=nodeData.Type;
//		this.mNodeData=nodeData;
//	}
	
}
