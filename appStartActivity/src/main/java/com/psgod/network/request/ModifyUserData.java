package com.psgod.network.request;

/**
 * 编辑用户资料请求
 * @author brandwang
 */
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class ModifyUserData extends BaseRequest<Boolean> {
	private static final String TAG = ModifyPassWordRequest.class
			.getSimpleName();

	public ModifyUserData(int method, String url, Listener<Boolean> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject response)
			throws JSONException {
		return true;
	}

	public static class Builder implements IPostRequestBuilder {
		private int mProvinceId = -1;
		private int mCityId = -1;
		private String mNickName;
		private int mGender = -1;
		private String mAvatar;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setAvatar(String avatar) {
			this.mAvatar = avatar;
			return this;
		}

		public Builder setGender(int gender) {
			this.mGender = gender;
			return this;
		}

		public Builder setNickName(String nickname) {
			this.mNickName = nickname;
			return this;
		}

		public Builder setProvince(int province) {
			this.mProvinceId = province;
			return this;
		}

		public Builder setCity(int city) {
			this.mCityId = city;
			return this;
		}

		public Builder setListener(Listener<Boolean> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public ModifyUserData build() {
			String url = createUrl();
			ModifyUserData request = new ModifyUserData(METHOD, url, listener,
					errorListener) {
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
					.append("profile/update");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();

			if (!TextUtils.isEmpty(mNickName)) {
				params.put("nickname", mNickName);
			}
			// 更新了头像
			if (!TextUtils.isEmpty(mAvatar)) {
				params.put("avatar", mAvatar);
			}
			if (mGender != -1) {
				params.put("sex", Integer.toString(mGender));
			}
			if (mCityId != -1) {
				params.put("city", Integer.toString(mCityId));
			}
			if (mProvinceId != -1) {
				params.put("province", Integer.toString(mProvinceId));
			}
			return params;
		}
	}
}
