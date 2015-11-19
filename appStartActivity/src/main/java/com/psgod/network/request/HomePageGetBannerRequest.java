package com.psgod.network.request;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.model.BannerData;

public class HomePageGetBannerRequest extends BaseRequest<List<BannerData>> {
	
	private static final String TAG = HomePageGetBannerRequest.class.getSimpleName();

	public HomePageGetBannerRequest(int method, String url,
			Listener<List<BannerData>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<BannerData> doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONArray data = reponse.getJSONArray("data");
		List<BannerData> list = new ArrayList<BannerData>();
		for (int i = 0 ; i < data.length() ; i++) {
			list.add(BannerData.createBanner(data.getJSONObject(i)));
		}
		return list;
	}

	public static class Builder implements IGetRequestBuilder {
		private Listener<List<BannerData>> listener;
		private ErrorListener errorListener;

		public Builder setListener(Listener<List<BannerData>> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public HomePageGetBannerRequest build() {
			String url = createUrl();
			HomePageGetBannerRequest request = new HomePageGetBannerRequest(METHOD, url,
					listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("banner/get_banner_list");

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
	
}
