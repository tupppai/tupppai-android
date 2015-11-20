package com.psgod.network.request;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.network.request.UploadMultiRequest.MultiUploadResult;
import com.psgod.ui.activity.UploadMultiImageActivity;

/**
 * 上传求P或者作品
 * 
 * @author ZouMengyuan
 * 
 */

public class UploadMultiRequest extends BaseRequest<MultiUploadResult> {

	private static final String TAG = UploadMultiRequest.class.getSimpleName();

	public UploadMultiRequest(int method, String url,
			Listener<MultiUploadResult> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected MultiUploadResult doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		MultiUploadResult result = new MultiUploadResult();
		JSONObject data = reponse.getJSONObject("data");
		if (data.has("id")) {
			result.mId = data.getLong("id");
		}

		return result;
	}

	public static class Builder implements IPostRequestBuilder {

		private ArrayList<Long> uploadIdList;
		private ArrayList<Float> scaleList;
		private ArrayList<Float> ratioList;
		private Listener<MultiUploadResult> listener;
		private ErrorListener errorListener;
		private String content;
		private String upload_type;
		private Long askId = 0l;

		public Builder setAskId(Long askId) {
			this.askId = askId;
			return this;
		}

		public Builder setUploadType(String upload_type) {
			this.upload_type = upload_type;
			return this;
		}

		public Builder setUploadIdList(ArrayList<Long> uploadIdList) {
			this.uploadIdList = uploadIdList;
			return this;
		}

		public Builder setScaleList(ArrayList<Float> scaleList) {
			this.scaleList = scaleList;
			return this;
		}

		public Builder setRatioList(ArrayList<Float> ratioList) {
			this.ratioList = ratioList;
			return this;
		}

		public Builder setListener(Listener<MultiUploadResult> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public Builder setContent(String content) {
			this.content = content;
			return this;
		}

		public UploadMultiRequest builder() {
			String url = createUrl().toString();
			UploadMultiRequest request = new UploadMultiRequest(METHOD, url,
					listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return createParameters();
				}
			};
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url + createParameters());
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			if (upload_type.equals(UploadMultiImageActivity.TYPE_ASK_UPLOAD)) {
				sb.append("ask/multi");
			} else if (upload_type
					.equals(UploadMultiImageActivity.TYPE_REPLY_UPLOAD)) {
				sb.append("reply/multi");
			}
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			if (askId != 0l) {
				params.put("ask_id", Long.toString(askId));
			}
			params.put("upload_ids", uploadIdList.toString());
			params.put("scales", scaleList.toString());
			params.put("ratios", ratioList.toString());
			params.put("desc", content);
			return params;
		}

	}

	public static class MultiUploadResult {
		public Long mId;
	}

}