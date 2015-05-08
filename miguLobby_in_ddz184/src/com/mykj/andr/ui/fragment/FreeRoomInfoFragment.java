package com.mykj.andr.ui.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.ui.fragment.LoadingFragment.NodeDataType;
import com.mykj.andr.ui.widget.CardZoneProtocolListener;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;

public class FreeRoomInfoFragment extends FragmentModel{
	public static final String TAG="FreeRoomInfoFragment";


	private Activity mAct;
	private Resources mResource;

	private TextView tvRoomInfo;
	private TextView tvTaskContent;
	private TextView tvBack;
	
	private Button btnContinue;
	private String textInfo;
	private String textTask;
	private int nodeid;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mAct=activity;
		this.mResource = mAct.getResources();
	}


	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * The Fragment's UI is just a simple text view showing its
	 * instance number.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.free_room_info,null);
		((TextView)view.findViewById(R.id.tvTitle)).setText(R.string.roominfo);
		tvRoomInfo=(TextView) view.findViewById(R.id.tvRoomOption);
		tvTaskContent=(TextView) view.findViewById(R.id.tvTaskContent);
		
		if(textInfo != null){
			tvRoomInfo.setText(textInfo);
		}
		if(textTask != null){
			tvTaskContent.setText(textTask);
		}
		
		

		
		tvBack = (TextView) view.findViewById(R.id.tvBack);
		tvBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CardZoneProtocolListener.getInstance(mAct).exitFreeRoom();
			}
		});
		
		btnContinue = (Button)view.findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HallDataManager.getInstance().setGameSitDown(true);
				if (FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated != null) {
					FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated.requestGameSitDown();
					// 2013-1-22
					FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
					if(FiexedViewHelper.getInstance().loadingFragment!=null){
						FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.ddz_into_fanzuobi),NodeDataType.NODE_TYPE_101);
					}
				}
			}
		});
		
		return view;

	}

	@Override
	public int getFragmentTag() {
		// TODO Auto-generated method stub
		return FiexedViewHelper.FREE_ROOM;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		CardZoneProtocolListener.getInstance(mAct).exitFreeRoom();
	}
	
	public void setRoomInfo(String str){
		textInfo = str;
		if(tvRoomInfo != null && textInfo != null){
			tvRoomInfo.setText(textInfo);
		}
	}
	
	public void setTaskInfo(String str){
		textTask=str;
		if(tvTaskContent != null && textTask != null){
			tvTaskContent.setText(textTask);
		}
	}
	
	
	
	public void setNodeId(int id){
		nodeid = id;
	}
	
	public int nodeId(){
		return nodeid;
	}
}
