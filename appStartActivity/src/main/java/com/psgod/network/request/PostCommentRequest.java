package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostCommentRequest extends BaseRequest<Long> {
	private static final String TAG = PostCommentRequest.class.getSimpleName();

	public PostCommentRequest(int method, String url, Listener<Long> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Long doParseNetworkResponse(JSONObject reponse)
			throws JSONException {
		JSONObject data = reponse.getJSONObject("data");
		return data.getLong("id");
	}

	public static class Builder implements IPostRequestBuilder {
		private static final int TYPE_ASK = 1;
		private static final int TYPE_REPLY = 2;
		// 评论的评论
		private static final int TYPE_COMMENT = 3;

		private String mContent;
		private int mType;
		// 回复的作品id
		private long mPid;
		// 回复的评论id(若有)
		private long mCid;
		private Listener<Long> mListener;
		private ErrorListener mErrorListener;

		public Builder setListener(Listener<Long> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public Builder setCid(long cid) {
			this.mCid = cid;
			return this;
		}

		public Builder setPid(long pid) {
			this.mPid = pid;
			return this;
		}

		public Builder setType(int type) {
			this.mType = type;
			return this;
		}

		public Builder setContent(String content) {
			this.mContent = content;
			return this;
		}

		public PostCommentRequest build() {
			String url = createUrl();
			PostCommentRequest request = new PostCommentRequest(METHOD, url,
					mListener, mErrorListener) {
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
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("comment/save");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();

			params.put("type", Integer.toString(mType));
			params.put("content", mContent);
			params.put("target_id", Long.toString(mPid));
			params.put("for_comment", Long.toString(mCid));

			return params;
		}
	}

}
