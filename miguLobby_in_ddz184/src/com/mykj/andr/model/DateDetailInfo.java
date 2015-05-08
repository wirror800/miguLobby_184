
package com.mykj.andr.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.UtilHelper;

public class DateDetailInfo {
	public String propId;

	public String propNum;

	public String deadline;

	public String offDays;

	public String deadText;

	public String descText;

	public static int currDate;

	public static short dayOffMax;

	public static boolean isDateDetailRefresh;

	String dataStr = "";

	private Resources mResources;

	private Context mContext;

	public DateDetailInfo(TDataInputStream tdis, Context context) {
		if (tdis == null) {
			return;
		}
		this.mContext = context;
		mResources = mContext.getResources();
		short dataLen = tdis.readShort(); // 此数据块长度
		MDataMark mark=tdis.markData(dataLen);
		dataStr = tdis.readUTF(dataLen);

		propId = UtilHelper.parseAttributeByName("id", dataStr);
		propNum = UtilHelper.parseAttributeByName("c", dataStr);
		deadline = UtilHelper.parseAttributeByName("expi", dataStr);
		tdis.unMark(mark);
		long off = calOffDays(deadline, String.valueOf(currDate));
		if (null != deadline && deadline.length() == 8) {
			offDays = String.valueOf(off);
		} else {
			offDays = null;
		}
		deadText = addDateText(deadline);
		descText = addDescText(propNum, offDays, off);
	}

	static DateDetailInfo[] dateDetailInfo;

	public static DateDetailInfo[] getDateDetailInfo() {
		return dateDetailInfo;
	}

	public static void setDateDetailInfo(DateDetailInfo[] dateDetailInfo) {
		DateDetailInfo.dateDetailInfo = dateDetailInfo;
	}

	private long calOffDays(String deadDate, String currDate) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			Date d1 = df.parse(deadDate);
			Date d2 = df.parse(currDate);
			long diff = d1.getTime() - d2.getTime();
			return diff / (1000 * 60 * 60 * 24);
		} catch (Exception e) {
			return -1;
		}

	}

	private String addDateText(String deadline) {
		String year = mResources.getString(R.string.ddz_year);
		String month = mResources.getString(R.string.ddz_month);
		String day = mResources.getString(R.string.ddz_day);
		String permanence = mResources.getString(R.string.ddz_permanence);
		if (null != deadline && deadline.length() == 8) {
			return deadline.substring(0, 4) + year + deadline.substring(4, 6) + month
					+ deadline.substring(6, 8) + day;
		} else if (null != deadline && deadline.length() == 0){
			return permanence;
		} else {
			return "";
		}

	}

	private String addDescText(String propNum, String offDays, long off) {
		String youHave = mResources.getString(R.string.ddz_you_have); // 您有
		String huaFeiJuan = mResources.getString(R.string.ddz_huafeijuan); // 张话费券
		String overdue = mResources.getString(R.string.ddz_overdue); // 天后过期
		String dayOverDue = mResources.getString(R.string.ddz_today_overdue); // 在今天过期
		if ((offDays != null) && (null != propNum) && (off <= (long) dayOffMax) && (off > 0)) {
			return youHave + propNum + huaFeiJuan + offDays + overdue;
		} else if (off == 0){
			return youHave + propNum + huaFeiJuan + dayOverDue;
		} else {
			return null;
		}
	}
}
