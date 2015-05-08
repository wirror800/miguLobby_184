package com.mykj.andr.ui.widget.Interface;

import com.mykj.andr.model.NodeData;

public interface CardZoneOnClickListener {
	/**
	 * @Title: invokeListItem
	 * @Description:      点击列表某项事件回调
	 * @param item        条目数据
	 * @param invokeState 调用状态
	 * @version: 2012-12-21 下午02:00:50
	 */
	public void invokeListItem(NodeData node,boolean isQuickEntry);
}
