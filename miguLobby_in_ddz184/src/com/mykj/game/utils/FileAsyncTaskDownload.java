package com.mykj.game.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;


public class FileAsyncTaskDownload extends AsyncTask<String, Integer, String> {
	public static final String PARENT_PATH = "/.mingyouGames";

	public static final String APKS_PATH = PARENT_PATH + "/apks";

	private static final String TAG = "FileAsyncTaskDownload";

	private DownLoadingListener mProgressListener;

	private String mStrRate;

	private String mFileName; //下载完成保存的文件名


	public FileAsyncTaskDownload(DownLoadingListener listener) {
		this(listener,null);
	}


	public FileAsyncTaskDownload(DownLoadingListener listener,String filename) {
		super();
		mProgressListener=listener;
		mFileName=filename;
	}

	/**
	 * 从url解析出fileName
	 */
	private String getFileNameFromUrl(String strUrl) {
		String fileName = null;

		if (strUrl != null) {
			String[] tmpStrArray = strUrl.split("/");
			fileName = tmpStrArray[tmpStrArray.length - 1];
			if (fileName.trim().length() == 0) {
				fileName = null;
			}
		}

		return fileName;
	}


	/**
	 * 传入参数第一个,下载 url
	 * 传入参数第二个,下载文件保存的文件夹
	 * 传入参数第三个,下载文件md5校验码
	 */
	@Override
	protected String doInBackground(String... params) {
		Log.v(TAG, "doInBackground...");
		String url = params[0];
		String downloadpath = params[1];
		String md5 = params[2];
		Log.v(TAG, "url=" + url);
		Log.v(TAG, "downloadpath" + downloadpath);

		File path = new File(downloadpath);
		if (!path.exists()) {
			path.mkdirs();
		}
		String filename = mFileName;
		if(Util.isEmptyStr(filename)){
			filename = getFileNameFromUrl(url);
		}

		File downloadFile = new File(downloadpath, filename);
		if (downloadFile.exists()) {
			// 已经存在 不用下载
			Log.v(TAG, "file is exist,don't download");
			return downloadFile.getPath();
		}

		String downLoadFileTmpName = filename + ".tmp"; // 设置下载的临时文件名

		File tmpFile = new File(downloadpath, downLoadFileTmpName);

		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(httpParams, 30000);

			HttpGet httpGet = new HttpGet(url);

			long startPosition = tmpFile.length(); // 已下载的文件长度
			String start = "bytes=" + startPosition + "-";
			httpGet.addHeader("Range", start);

			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					|| httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
				HttpEntity entity = httpResponse.getEntity();
				long length = entity.getContentLength();// 请求文件长度

				InputStream inputStream = entity.getContent();
				byte[] b = new byte[1024];
				int readedLength = -1;

				OutputStream outputStream = new FileOutputStream(tmpFile, true);

				long fileSize=length + startPosition;
				long percentile = fileSize/20; // 文件每下载5%的长度
				int rate = 0;
				int count = 0;
				long downloadfile=startPosition;

				String strDownLoad;
				String strFileSize=Util.bytes2mb(fileSize);

				while (((readedLength = inputStream.read(b)) != -1)) {
					outputStream.write(b, 0, readedLength);
					downloadfile +=readedLength;

					startPosition += readedLength;
					if (startPosition >= percentile) // 每下载5%，计算进度条
					{
						count = (int) (startPosition / percentile) * 5;
						Log.v(TAG, "count=" + count);
						rate += count;

						startPosition = 0;
						count = 0;

						// 调用了这个方法之后会触发onProgressUpdate(Integer... values)
						strDownLoad= Util.bytes2mb(downloadfile);
						mStrRate=strDownLoad+"/"+strFileSize;

						publishProgress(rate);
						Log.v(TAG, "文件已下载" + rate + "%");

					}
				}
				strDownLoad= Util.bytes2mb(downloadfile);
				mStrRate=strDownLoad+"/"+strFileSize;
				inputStream.close();
				outputStream.close();
				if(!Util.isEmptyStr(md5)){
					// 下载文件MD5检测
					if (Util.downloadFileMD5Check(tmpFile, md5)) {
						Log.v(TAG, "download file md5 check success");
						tmpFile.renameTo(downloadFile);
					} else {
						Log.e(TAG, "download file md5 check fail");
						tmpFile.delete();
						return null;

					}
				}else{
					Log.v(TAG, "download file md5 check success");
					tmpFile.renameTo(downloadFile);
				}


			}
		} catch (ClientProtocolException e) {
			mProgressListener.downloadFail(e.toString());
			return null;
		} catch (IOException e) {
			mProgressListener.downloadFail(e.toString());
			return null;
		}
		return downloadFile.getPath();
	}



	@Override
	protected void onPostExecute(String strPath) {
		super.onPostExecute(strPath);
		if (strPath != null) {
			mProgressListener.onProgress(100,mStrRate);
			mProgressListener.downloadSuccess(strPath);
		}
	}


	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		int rate = progress[0].intValue();

		mProgressListener.onProgress(rate,mStrRate);

	}




	/**
	 * 客户端升级下载监听器
	 * @author Jason
	 *
	 */
	public interface DownLoadingListener {

		void onProgress(int rate,String strRate);

		void downloadFail(String err);
		
		void downloadSuccess(String path);

	}


}
