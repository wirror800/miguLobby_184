package com.mykj.andr.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mykj.andr.model.NodeData;
import com.mykj.andr.ui.widget.Interface.CardZoneOnClickListener;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.utils.UtilHelper;

public class NewCardZoneGridViewAdapter extends BaseAdapter{
	private List<NodeData>  mDataNataList;
	private Activity mAct;
	private CardZoneOnClickListener mCallBack;
	private static long mills=0;
	private Resources mResource;
	public NewCardZoneGridViewAdapter(Activity act,List<NodeData> dataList){
		mAct=act;
		this.mResource = mAct.getResources();
		mDataNataList=dataList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataNataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDataNataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	private int getDrawableId(int type){
		switch(type){
	case 1:
		return R.drawable.cardzone_corner_01;
	case 2:
		return R.drawable.cardzone_corner_02;
	case 3:
		return R.drawable.cardzone_corner_03;
	case 4:
		return R.drawable.cardzone_corner_04;
	case 5:
		return R.drawable.cardzone_corner_05;
	case 6:
		return R.drawable.cardzone_corner_06;
	case 7:
		return R.drawable.cardzone_corner_07;
	case 8:
		return R.drawable.cardzone_corner_08;
	case 9:
		return R.drawable.cardzone_corner_09;
	case 10:
		return R.drawable.cardzone_corner_10;
	default:
		return 0;
	}
	}
	
	private int getTagFontColor(int type){
		switch(type){
		case 1:
			return 0xff663e00;
		case 2:
			return 0xff752f00;
		case 3:
			return 0xff460505;
		case 4:
			return 0xff5f0000;
		case 5:
			return 0xff412553;
		case 6:
			return 0xff261137;
		case 7:
			return 0xff101c3d;
		case 8:
			return 0xff022b41;
		case 9:
			return 0xff2e4514;
		case 10:
			return 0xff333333;
		default:
			return 0xff333333;
		}
	}
	
	int topPadding = 0;
	int leftPadding = 0;
	int topPadding2 = 0;
	int leftPadding2 = 0;
	int bottomPadding = 0;
	int rightPadding = 0;
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		final NodeData node=(NodeData) getItem(position);
		if (row == null) {
			LayoutInflater inflater = mAct.getLayoutInflater();
			row = inflater.inflate(R.layout.cardzone_gridview_item, null);
			holder = new ViewHolder();
			holder.tvName = (TextView)row.findViewById(R.id.tvName);
			holder.tvOnLineUsers = (TextView)row.findViewById(R.id.tvOnLineUsers);
			holder.tvInCondition = (TextView)row.findViewById(R.id.tvInCondition);
			holder.tvScore = (TextView)row.findViewById(R.id.tvScore);
			holder.iconImg = (TextView)row.findViewById(R.id.iconImg);
			holder.iconImg2 = (TextView)row.findViewById(R.id.iconImg2);
			holder.ryCardZoneGridViewItem = (RelativeLayout)row.findViewById(R.id.ryCardZoneGridViewItem); 
		}else {
			holder = (ViewHolder) row.getTag();
		}
		
		if(node==null){
			Toast.makeText(mAct, "数据加载出现错误，请您重新登录", Toast.LENGTH_SHORT).show();
			FiexedViewHelper.getInstance().goToReLoginView();
		}

		holder.tvName.setText(node.Name);
		String score=node.GRContent;
		holder.tvScore.setText(score);
		
		if(node.onLineUser < 10000){
			holder.tvOnLineUsers.setText(""+node.onLineUser+mResource.getString(R.string.ddz_person));
		}else if(node.onLineUser < 100000){
			int sub = node.onLineUser % 10000 / 1000;
			holder.tvOnLineUsers.setText(""+node.onLineUser / 10000 + (sub == 0 ? "" : "." + sub) + mResource.getString(R.string.ddz_wan_ren));
		}else{
			holder.tvOnLineUsers.setText(""+ node.onLineUser / 10000 + mResource.getString(R.string.ddz_wan_ren));
		}
		holder.tvInCondition.setText(node.GTContent);

		if (node.RoomTags != null && node.RoomTags.length != 0) {
			if (node.RoomTags.length > 1) {
				holder.iconImg2.setVisibility(View.VISIBLE);
				if(leftPadding == 0 || topPadding == 0){
					leftPadding = holder.iconImg2.getPaddingLeft();
					rightPadding = holder.iconImg2.getPaddingRight();
					topPadding = holder.iconImg2.getPaddingTop();
					bottomPadding = holder.iconImg2.getPaddingBottom();
					leftPadding2 = leftPadding * 3 / 2;
					topPadding2 = topPadding * 2;
				}
				
				holder.iconImg2.setBackgroundResource(getDrawableId(node.RoomTags[1].Type));
				holder.iconImg2.setTextColor(getTagFontColor(node.RoomTags[1].Type));
				
				String tip = node.RoomTags[1].Desc;
				if (tip != null) {
					int left = leftPadding;
					int top = topPadding;
					if(tip.length() < 2){
						top = topPadding2;
						left = leftPadding2;
					}else if(tip.length() <= 3){ //3个字符是2个字，中间是\n
						left = leftPadding2;
					}
					holder.iconImg2.setPadding(left, top, rightPadding, bottomPadding);
					holder.iconImg2.setText(tip);
				}
			} else {
				holder.iconImg2.setVisibility(View.GONE);
			}
			holder.iconImg.setVisibility(View.VISIBLE);
			if(leftPadding == 0 || topPadding == 0){
				leftPadding = holder.iconImg.getPaddingLeft();
				rightPadding = holder.iconImg.getPaddingRight();
				topPadding = holder.iconImg.getPaddingTop();
				bottomPadding = holder.iconImg.getPaddingBottom();
				leftPadding2 = leftPadding * 3 / 2;
				topPadding2 = topPadding * 2;
			}
			holder.iconImg.setBackgroundResource(getDrawableId(node.RoomTags[0].Type));
			holder.iconImg.setTextColor(getTagFontColor(node.RoomTags[0].Type));
			String tip = node.RoomTags[0].Desc;
			if (tip != null) {
				int left = leftPadding;
				int top = topPadding;
				if(tip.length() < 2){
					top = topPadding2;
					left = leftPadding2;
				}else if(tip.length() <= 3){ //3个字符是2个字，中间是\n
					left = leftPadding2;
				}
				holder.iconImg.setPadding(left, top, rightPadding, bottomPadding);
				holder.iconImg.setText(tip);
			}

		} else {
			holder.iconImg.setVisibility(View.GONE);
			holder.iconImg2.setVisibility(View.GONE);
		}
//		switch (node.IconID) { // 默认，热，荐，折图标
//		case 1:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_hot);
//			break;
//		case 2:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_recommend);
//			break;
//		case 3:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_discount);
//			break;
//		case 4:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_zhiyunhui);  //智运会角标
//			break;
//		case 5:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_songledou);  //送乐豆
//			break;
//		case 6:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_dashifen);  //大师分
//			break;
//		case 7:
//			holder.iconImg.setBackgroundResource(R.drawable.icon_bomb);  //炸弹
//			break;
//		default:
//			holder.iconImg.setBackgroundDrawable(null);
//			break;
//		}


		holder.ryCardZoneGridViewItem.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mCallBack!=null){
					long curtime=System.currentTimeMillis();
					if((curtime-mills)>2000){
						mills=curtime;
						mCallBack.invokeListItem(node, true);
					}

				}
			}
		});

		row.setTag(holder);


		return row;
	}

	public class ViewHolder {
		TextView tvName; // 名称
		TextView tvOnLineUsers; // 在线人数
		TextView tvInCondition; // 条件
		TextView tvScore;  //底分
		// Button btnComeIn; // 进入游戏/快速报名/退赛
		TextView iconImg; // 图标，默认，热门，推荐等
		TextView iconImg2; // 图标，默认，热门，推荐等
		RelativeLayout ryCardZoneGridViewItem; // 父容器
	}



	public void setCallBack(CardZoneOnClickListener mCallBack) {
		this.mCallBack = mCallBack;
	}



//    private String findBaseScore(String str){
//    	StringBuilder sb=new StringBuilder();
//    	Matcher m = Pattern.compile("基础积分[0-9]*分").matcher(str);
//    	if(m.find()){
//    	    String baseScore = m.group();
//    	    String score =baseScore.substring(4,baseScore.length()-1);
//    	    sb.append("(底分 ");
//    	    sb.append(score);
//    	    sb.append(")");
//    	    
//    	}
//    	return sb.toString();
//    }

}
