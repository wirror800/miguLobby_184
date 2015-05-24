package com.MyGame.Midlet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class HttpUtil {

	private HttpUtil() {
		throw new AssertionError();
	}

	public static void httpGet(String url, IGetRequest request) {
		new HttpGetAsyncTask(request).execute(url);
	}

	private static class HttpGetAsyncTask extends
			AsyncTask<String, Void, String> {

		private IGetRequest mRequest;

		public HttpGetAsyncTask(IGetRequest request) {
			mRequest = request;
		}

		@Override
		protected String doInBackground(String... params) {
			String result = null;
			String httpUrl = params[0];
			HttpURLConnection urlConnection = null;
			try {
				URL url = new URL(httpUrl);
				urlConnection = (HttpURLConnection) url.openConnection();
				InputStream in = urlConnection.getInputStream();
				result = readStream(in);

			} catch (IOException e) {
				result = "";
			} finally {
				urlConnection.disconnect();
			}
			return result;

		}

		@Override
		protected void onPostExecute(String result) {
			mRequest.httpReqResult(result);
		}

	}

	public static String readStream(InputStream is) throws IOException {
		StringBuilder result = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String line;
		while ((line = in.readLine()) != null) {
			result.append(line);
			result.append('\n');
		}
		in.close();
		return result.toString();
	}

}
