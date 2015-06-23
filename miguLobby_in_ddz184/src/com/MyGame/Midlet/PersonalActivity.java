package com.MyGame.Midlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.MyGame.Midlet.R;
import com.mingyou.community.Community;

public class PersonalActivity extends Activity implements OnClickListener,
OnCheckedChangeListener {
	public static final String TAG = "PersonalActivity";
    private Context mContext;

	// 当前选择的Tab索引
	private static final int TAB_INDEX_BASIC = 0;
	private static final int TAB_INDEX_DETAIL = 1;
	// 承载View
	private ViewPager mViewPager;

	private TabPageAdapter mCenterPageAdapter;

	private LayoutInflater mInflater;

	private List<View> mViews;

	// 系统、个人 等内容Content
	private View mBasicView;
	private View mDetailView;

	protected TextView tvBasic, tvDetail;
	protected TextView tvAccount, tvID, tvNickName;

	protected EditText etNickName, etRealName, etCard, etEmail, etMobile;

	protected RadioGroup mRadioGroup;
	protected RadioButton btnMale, btnFemale;

	protected Button tvBack;
	protected ImageButton btnBasicSave, btnDetailSave;

	protected ImageView mImageView;

	/**初始化时候的性别*/
	public String sexId_ = "1";
	/**初始化时候的昵称 */
	public String nickname_="";

	public String sexId;

	public int userId;

	/**
	 * 个人中心数据 依次为 account + "#" + nickname + "#" + guid + "#" + sex + "#" +
	 * leDou + "#" + ZhouJiFen + "#" + LeiJiJiFen + "#" + ZhouPaiMing + "#" +
	 * YuanBao + "#" + HuaFeiQuan + "#" + DuiHuanURLId + "#" + PaiMingURLId +
	 * "#" + HuaFeiQuanURLId + "#" + XiugaiURLId + "#" + DuiHuanURLId + "#" +
	 * "#" + MorePaiPingUrl+ "#" + status + "#" + statusnote + "#"
	 */
	String[] userInfoArray = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal);
		mContext=this;
		userId = Community.getSelftUserInfo().userId;

		/**初始化控件*/
		init();
		initTabContent();
		initPager();

		GetUserInfoTask task = new GetUserInfoTask();
		task.execute(getInfoUrl(getBasicParamsStr(userId)));

		GetUserDetailTask detailTask = new GetUserDetailTask();
		detailTask.execute(getInfoUrl(getDetailParamsStr(userId)));
	}

	private void init() {
		tvBasic = (TextView) findViewById(R.id.tvBaseInfo);
		tvDetail = (TextView) findViewById(R.id.tvDetail);
		tvBasic.setOnClickListener(this);
		tvDetail.setOnClickListener(this);

		tvBack = (Button) findViewById(R.id.tvBack);
		tvBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/***
	 * 初始化TabContent内容布局
	 */
	private void initTabContent() {
		mViews = new ArrayList<View>();
		mInflater = getLayoutInflater();

		// 解析布局文件
		mBasicView = mInflater.inflate(R.layout.lobby_base_info, null);
		initLayBasic(mBasicView);

		mDetailView = mInflater.inflate(R.layout.lobby_detail_info, null);
		initLayDetail(mDetailView);

		// 添加进容器中
		mViews.add(mBasicView);
		mViews.add(mDetailView);
	}

	private void initLayDetail(View v) {
		etRealName = (EditText) v.findViewById(R.id.real_name);
		etCard = (EditText) v.findViewById(R.id.idnum_num);
		etEmail = (EditText) v.findViewById(R.id.email_text);
		etMobile = (EditText) v.findViewById(R.id.mobile_num);
		btnDetailSave = (ImageButton) v.findViewById(R.id.btn_dsave);
		btnDetailSave.setOnClickListener(this);
	}

	private void initLayBasic(View v) {
		tvAccount = (TextView) v.findViewById(R.id.account);
		tvID = (TextView) v.findViewById(R.id.id);
		etNickName = (EditText) v.findViewById(R.id.nick_name);

		mRadioGroup = (RadioGroup) v.findViewById(R.id.sex_radio);
		mRadioGroup.setOnCheckedChangeListener(this);
		btnMale = (RadioButton) v.findViewById(R.id.btn_male);
		btnFemale = (RadioButton) v.findViewById(R.id.btn_female);
		btnBasicSave = (ImageButton) v.findViewById(R.id.btn_bsave);
		btnBasicSave.setOnClickListener(this);
		mImageView = (ImageView) v.findViewById(R.id.user_img);
	}

	private void initPager() {
		mCenterPageAdapter = new TabPageAdapter(mViews);

		// 获取ViewPager并添加设配器，默认显示第1项，索引为0
		mViewPager = (ViewPager) findViewById(R.id.viewpagerTab);
		mViewPager.setAdapter(mCenterPageAdapter);

		setImageBackground(0);
		mViewPager.setCurrentItem(TAB_INDEX_BASIC);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pageIndex) {
				switch (pageIndex) {
				case 0:
					mViewPager.setCurrentItem(TAB_INDEX_BASIC);
					break;

				case 1:
					mViewPager.setCurrentItem(TAB_INDEX_DETAIL);
					break;
				}
				setImageBackground(pageIndex);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private void setImageBackground(int position) {
		switch (position) {
		case 0:
			tvBasic.setBackgroundResource(R.drawable.tab_selected);
			tvDetail.setBackgroundResource(R.drawable.tab_normal);
			break;

		case 1:
			tvBasic.setBackgroundResource(R.drawable.tab_normal);
			tvDetail.setBackgroundResource(R.drawable.tab_selected);
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvBaseInfo:
			setImageBackground(TAB_INDEX_BASIC);
			mViewPager.setCurrentItem(TAB_INDEX_BASIC);
			break;

		case R.id.tvDetail:
			setImageBackground(TAB_INDEX_DETAIL);
			mViewPager.setCurrentItem(TAB_INDEX_DETAIL);
			break;

		case R.id.btn_bsave:
			// 保存昵称和性别
			final String nickName = etNickName.getText().toString().trim();
			if(nickName == null ||nickname_==null ||sexId==null ||sexId_ == null){
				break;
			}
			if (nickName.equals(nickname_) && sexId.equals(sexId_)) {
				Toast.makeText(mContext,"请修改昵称或性别再保存...",Toast.LENGTH_SHORT).show();
			}else if(Configs.isIllegalCh(nickName)) {
				Toast.makeText(mContext,"昵称只允许输入字母，数字，下划线和中文",Toast.LENGTH_SHORT).show();
			}else{		
				String bUrl = getInfoUrl(getModifyParamsStr(userId,nickName,
						"", "", sexId));
				Log.v(TAG, "获取修改信息 url="+bUrl);
				ModifyUserInfoTask task = new ModifyUserInfoTask();			
				task.execute(bUrl);
			}


			break;

		case R.id.btn_dsave:
			// 保存真实姓名,身份证
			String idcardno = etCard.getText().toString();
			String realname = etRealName.getText().toString();
			String mobile= etMobile.getText().toString();
			
			if (idcardno.length() == 0 && realname.length() == 0 && mobile.length() == 0) {
				Toast.makeText(this,"修改信息要求至少一项不为空...",Toast.LENGTH_SHORT).show();
			} else {
				String dUrl = getInfoUrl(getModifyUserParams(userId,realname, idcardno, mobile));
				Log.v(TAG, "保存真实姓名,身份证 url="+dUrl);

				ModifyUserDetailTask mTask = new ModifyUserDetailTask();
				mTask.execute(dUrl);
			}

			break;

		default:
			break;
		}
	}


	public String getInfoUrl(String params) {
		StringBuffer sb = new StringBuffer();
		sb.append(AppConfig.PERSIONINFO_URL);// 现网地址
		sb.append(params);
		sb.append(getSign(params));
		String url=sb.toString();
		Log.v(TAG, "请求个人信息url="+url);
		return url;
	}

	
	
	/**
	 * 获取请求基本信息的url的参数
	 * 
	 * @param userId
	 * @return
	 */
	protected String getBasicParamsStr(int userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("method=getinfo_mobile");
		sb.append("&userid=").append(userId);
		sb.append("&clientid=").append(AppConfig.clientId);
		sb.append("&format=xml");
		sb.append("&op=").append("");
		return sb.toString();
	}

	/**
	 * 获取请求详细信息的url的参数
	 * 
	 * @param userId
	 * @return
	 */
	protected String getDetailParamsStr(int userId) {

		StringBuffer sb = new StringBuffer();
		sb.append("method=getinfo");
		sb.append("&weuid=").append(userId);
		sb.append("&format=xml");
		sb.append("&fields=mobile,email,realname,idcardno");
		sb.append("&op=").append("");
		return sb.toString();
	}

	/**
	 * 获取修改信息的url的参数
	 * 
	 * @param nickname
	 * @param realname
	 * @param idcardno
	 * @param sex
	 * @return
	 */
	protected String getModifyParamsStr(int userId, String nickname,
			String realname, String idcardno, String sex) {

		StringBuffer sb = new StringBuffer();
		sb.append("method=").append("updinfo");
		sb.append('&').append("weuid=").append(userId);
		sb.append('&').append("platid=").append("03");
		sb.append('&').append("nickname=").append(nickname);
		sb.append('&').append("realname=").append(realname);
		sb.append('&').append("idcardno=").append(idcardno);
		sb.append('&').append("sex=").append(sex);
		sb.append('&').append("faceid=").append("");
		sb.append('&').append("signature=").append("");
		sb.append('&').append("province=").append("");
		sb.append('&').append("city=").append("");
		sb.append('&').append("areacode=").append("");
		sb.append('&').append("schoolid=").append("");
		sb.append("&format=xml");
		sb.append("&op=").append("");
		return sb.toString();
	}

	
	
	/**
	 * 获取修改信息的url的参数
	 * 
	 * @param nickname
	 * @param realname
	 * @param idcardno
	 * @param sex
	 * @return
	 */
	protected String getModifyUserParams(int userId,String realname, String idcardno, String mobile) {
		StringBuffer sb = new StringBuffer();
		sb.append("method=").append("upd_idinfo");
		sb.append('&').append("weuid=").append(userId);
		sb.append('&').append("realname=").append(realname);
		sb.append('&').append("idcardno=").append(idcardno);
		sb.append('&').append("mobile=").append(mobile);
		sb.append("&format=xml");
		sb.append("&op=").append(System.currentTimeMillis());
		return sb.toString();
	}
	
	
	
	
	
	/**
	 * 采用MD5算法的校验串
	 */
	public static String getSign(String params) {
		// 去除"&"
		String[] s_A = params.split("&");
		// 排序
		Arrays.sort(s_A);
		// 组合
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s_A.length; i++) {
			sb.append(s_A[i]);
		}
		// 加上密钥
		sb.append("secret=MYTL!@#14yhl9tka");
		// 生成参数串
		StringBuffer result_sb = new StringBuffer();
		result_sb.append("&sig=");
		result_sb.append(Configs.md5(sb.toString()));

		return result_sb.toString();
	}

	// 获取个人中心数据
	class GetUserInfoTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String userInfo = Configs.getConfigXmlByHttp(params[0]);
			return userInfo;
		}

		@Override
		protected void onPostExecute(String result) {

			if (null != result && result.length() > 0) {
				String status = Configs.parseStatusXml(result, "status");
				if (null != status && status.equals("0")) {
					String account = Configs.parseStatusXml(result, "username");
					nickname_ = Configs.parseStatusXml(result, "nickname");
					String guid = Configs.parseStatusXml(result, "guid");
					sexId_ = Configs.parseStatusXml(result, "sex");

					tvAccount.setText(account);
					tvID.setText(String.valueOf(guid));
					etNickName.setText(nickname_);
					if (sexId_.equals("1")) {
						btnMale.setChecked(true);
						mImageView.setImageResource(R.drawable.user_man);
					} else {
						btnFemale.setChecked(true);
						mImageView.setImageResource(R.drawable.user_femal);
					}
				}

			}
		}
	}



	// 修改昵称 性别
	class ModifyUserInfoTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String httpInfo = Configs.getConfigXmlByHttp(params[0]);
			return httpInfo;
		}

		@Override
		protected void onPostExecute(String ret) {
			if (null != ret && ret.length() > 0) {
				String status = Configs.parseStatusXml(ret, "status");
				String statusNote = Configs.parseStatusXml(ret, "statusnote");
				Toast.makeText(getApplication(), statusNote, Toast.LENGTH_SHORT)
				.show();
				if (null != status && status.equals("0")) {
					String nickName = etNickName.getText().toString();
					Community.getSelftUserInfo().nickName = nickName;
					Community.getSelftUserInfo().cbGender = Byte.parseByte(sexId);
					if (sexId.equals("1")) {
						mImageView.setImageResource(R.drawable.user_man);
					} else {
						mImageView.setImageResource(R.drawable.user_femal);
					}
					sexId_ = sexId;
					nickname_ = nickName;
				}
				Log.v(TAG, ret);
			}


		}
	}

	// 获取个人中心数据
	class GetUserDetailTask extends AsyncTask<String, Void, String> {
		// 从网上下载图片
		@Override
		protected String doInBackground(String... params) {
			String httpInfo = Configs.getConfigXmlByHttp(params[0]);
			return httpInfo;
		}

		@Override
		protected void onPostExecute(String result) {
			if (null != result && result.length() > 0) {
				String status = Configs.parseStatusXml(result, "status");
				if (null != status && status.equals("0")) {
					String realname = Configs
							.parseStatusXml(result, "realname");
					String idcardno = Configs
							.parseStatusXml(result, "idcardno");
					String email = Configs.parseStatusXml(result, "email");
					String mobile = Configs.parseStatusXml(result, "mobile");

					etRealName.setText(realname);
					etCard.setText(idcardno);
					etEmail.setText(email);
					etMobile.setText(mobile);
				}

			}

		}
	}

	// 修改身份证 真实姓名
	class ModifyUserDetailTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String userInfo = Configs.getConfigXmlByHttp(params[0]);
			return userInfo;
		}

		@Override
		protected void onPostExecute(String ret_) {
			Log.v(TAG, ret_);
			if (null != ret_ && ret_.length() > 0) {
				//String status = Configs.parseStatusXml(ret_, "status");
				String statusNote = Configs.parseStatusXml(ret_, "statusnote");
				if (null != statusNote && statusNote.length() > 0) {
					Toast.makeText(mContext, statusNote,Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (group.getId()) {
		case R.id.sex_radio:
			if (checkedId == btnMale.getId()) {
				sexId = "1";
			} else if (checkedId == btnFemale.getId()) {
				sexId = "0";
			}
			break;

		default:
			break;

		}
	}

}
