
package com.mykj.andr.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.mykj.andr.model.DateDetailInfo;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;

public class DetailDateDialog extends Dialog implements android.view.View.OnClickListener {

    Context ctx;

    ListView mDateList;

    public static String[] mListTitle;

    public static String[] mListStr;

    ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();

    protected Button btnCancel;

    public DetailDateDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        ctx = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_detail_dialog);
        initData();
        initUI();
    }

    private void initData() {
        // TODO Auto-generated method stub
    	Resources resource = AppConfig.mContext.getResources();
        DateDetailInfo[] detailInfos = DateDetailInfo.getDateDetailInfo();
        List<String> numList = new ArrayList<String>();
        numList.add(resource.getString(R.string.ddz_count));
        List<String> dateList = new ArrayList<String>();
        dateList.add(resource.getString(R.string.ddz_period_of_validity));
        for (int i = 0; i < detailInfos.length; i++) {
            if (null != detailInfos[i]){
                numList.add(detailInfos[i].propNum);
                dateList.add(detailInfos[i].deadText);
            }
        }
        mListTitle = numList.toArray(new String[numList.size()]);
        mListStr = dateList.toArray(new String[dateList.size()]);
    }

    private void initUI() {
        // TODO Auto-generated method stub
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        int lengh = mListTitle.length;
        for (int i = 0; i < lengh; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("title", mListTitle[i]);
            item.put("text", mListStr[i]);
            mData.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(ctx, mData, R.layout.detail_list_item,
                new String[] {
                        "title", "text"
                }, new int[] {
                        android.R.id.text1, android.R.id.text2
                });

        mDateList = (ListView) findViewById(R.id.date_list);
        mDateList.setAdapter(adapter);
        
    }

    @Override
    public void onClick(View v) {
    	int id=v.getId();
    	if(id==R.id.btnCancel){
    		dismiss();
    	}
    }
}
