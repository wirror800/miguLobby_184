package com.mykj.game.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.login.utils.DensityConst;


public class ImageAsyncTaskDownload extends AsyncTask<Void, Void, Bitmap> {
	private String mUrl;
	private String mFileName;
	private ImageView mImgView;
	private String mDownloadDir;

	public ImageAsyncTaskDownload(String url,String fileName,ImageView image) {  
		mUrl = url;  
		mFileName=fileName;
		mImgView = image;  
		mDownloadDir=Util.getIconDir();
		File file=new File(mDownloadDir);
		if(!file.exists()){
			file.mkdirs();
		}
	} 


	@Override
	protected Bitmap doInBackground(Void... params) {
		Bitmap bmp = null;
		long curPosition = 0;
		try {
			File iconFile = new File(mDownloadDir,mFileName);
			File iconTmpFile = new File(mDownloadDir, mFileName+ "_tmp");

			HttpGet httpGet = new HttpGet(mUrl);
			httpGet.addHeader("Range", "bytes=" + iconTmpFile.length() + "-");
			HttpClient httpClient = new DefaultHttpClient();
			HttpEntity httpEntity = null;
			FileOutputStream fos = null;
			InputStream is = null;

			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode=httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK
					|| statusCode == HttpStatus.SC_PARTIAL_CONTENT)
			{
				httpEntity = httpResponse.getEntity();
				long fileSize= httpEntity.getContentLength();//请求文件长度
				if(httpEntity != null)
				{
					fos = new FileOutputStream(iconTmpFile, true);
					is = httpEntity.getContent();
					byte[] buff = new byte[1024];
					int recved = 0;
					while((recved = is.read(buff, 0, buff.length)) != -1)
					{
						fos.write(buff, 0, recved);
						curPosition += recved;
					}
					fos.flush();
					fos.close();
					is.close();
					if(fileSize==curPosition){
						iconTmpFile.renameTo(iconFile);
						bmp=BitmapFactory.decodeFile(iconFile.getPath());
					}else{
						iconTmpFile.delete();
						bmp=null;
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap != null) {
			//此处更新图片
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int disWidth = DensityConst.getWidthPixels();
			Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width
					* disWidth / 800, height * disWidth / 800, true);
			if(mImgView != null){
				mImgView.setImageBitmap(scaleBitmap);
			}

		}
	}



}
