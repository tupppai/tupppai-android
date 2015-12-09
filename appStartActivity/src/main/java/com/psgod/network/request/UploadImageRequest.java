package com.psgod.network.request;

import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.network.request.UploadImageRequest.ImageUploadResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class UploadImageRequest extends BaseRequest<ImageUploadResult> {
	private static final String TAG = UploadImageRequest.class.getSimpleName();

	private static final int INITIAL_TIMEOUT_MS = 5000; // 重传时间

	private static final String BOUNDARY = "psgod";
	private static final String MULTIPART_FORM_DATA = "multipart/form-data";
	private static final String FIRST_LINE = "--" + BOUNDARY + "\r\n";
	private static final String END_LINE = "--" + BOUNDARY + "--" + "\r\n";

	private Bitmap mBitmap;

	public UploadImageRequest(int method, String url,
			Listener<ImageUploadResult> listener, ErrorListener errorListener,
			Bitmap bitmap) {
		super(method, url, listener, errorListener);
		setShouldCache(false);
		setRetryPolicy(new DefaultRetryPolicy(INITIAL_TIMEOUT_MS,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		mBitmap = bitmap;
	}

	@Override
	protected ImageUploadResult doParseNetworkResponse(JSONObject response)
			throws JSONException {
		ImageUploadResult result = new ImageUploadResult();
		JSONObject res = response.getJSONObject("data");
		result.id = res.getLong("id");
		result.url = res.getString("url");
		result.name = res.getString("name");
		return result;
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		if (mBitmap == null) {
			return super.getBody();
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StringBuilder sb = new StringBuilder();

		sb.append(FIRST_LINE);
		sb.append("Content-Disposition: form-data; name=\"file1\"; filename=\"uploadImage\"\r\n");
		sb.append("Content-Type: ").append("image/png").append("\r\n");
		sb.append("\r\n");

		ByteArrayOutputStream imageBos = new ByteArrayOutputStream();
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageBos); // TODO

		try {
			bos.write(sb.toString().getBytes("utf-8"));
			bos.write(imageBos.toByteArray());
			bos.write("\r\n".getBytes("utf-8"));
			bos.write(END_LINE.toString().getBytes("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bos.toByteArray();
	}

	// Content-Type: multipart/form-data; boundary=psgod
	@Override
	public String getBodyContentType() {
		StringBuilder sb = new StringBuilder();
		sb.append(MULTIPART_FORM_DATA).append("; boundary=").append(BOUNDARY);
		return sb.toString();
	}

	public static class Builder implements IPostRequestBuilder {
		private Bitmap bitmap;
		private Listener<ImageUploadResult> listener;
		private ErrorListener errorListener;

		public Builder setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
			return this;
		}

		public Builder setListener(Listener<ImageUploadResult> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public UploadImageRequest build() {
			String url = createUrl();
			UploadImageRequest request = new UploadImageRequest(METHOD, url,
					listener, errorListener, bitmap);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("image/upload");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			return null;
		}
	}

	public static class ImageUploadResult {
		public long id; // 图片ID，跟求P和作品ID一致
		public String url; // 图片url
		public String name; // 图片名字
	}
}
