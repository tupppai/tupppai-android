package com.psgod.network.request;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.model.Comment;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.CommentListRequest.CommentListWrapper;

public class CommentListRequest extends BaseRequest<CommentListWrapper> {
	private static final String TAG = CommentListRequest.class.getSimpleName();

	public CommentListRequest(int method, String url,
			Listener<CommentListWrapper> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected CommentListWrapper doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");

		JSONArray hotCommentsData = data.getJSONArray("hot_comments");
		JSONArray newCommentsData = data.getJSONArray("new_comments");

		int hotCommentsLength = hotCommentsData.length();
		int recentCommentsLength = newCommentsData.length();

		List<Comment> hotCommentsList = new ArrayList<Comment>(
				hotCommentsLength);
		for (int i = 0; i < hotCommentsLength; i++) {
			hotCommentsList.add(Comment.createComment(hotCommentsData
					.getJSONObject(i)));
		}

		List<Comment> recentCommentList = new ArrayList<Comment>(
				recentCommentsLength);
		for (int n = 0; n < recentCommentsLength; n++) {
			recentCommentList.add(Comment.createComment(newCommentsData
					.getJSONObject(n)));
		}

		CommentListWrapper wrapper = new CommentListWrapper();

		if (data.has("thread")) {
			JSONObject photoItem = data.getJSONObject("thread");
			PhotoItem item = PhotoItem.createPhotoItem(photoItem);

			wrapper.photoItem = item;
		}

		wrapper.hotCommentList = hotCommentsList;
		wrapper.recentCommentList = recentCommentList;

		return wrapper;
	}

	public static class Builder implements IGetRequestBuilder {
		public static final int TYPE_ASK = 1;
		public static final int TYPE_REPLY = 2;

		private long lastUpdated = -1;
		private int type = TYPE_ASK;
		private long pid;
		private int page = 1;
		private int size = 10;
		private long commentId = -1;
		private int needPhotoItem = 0;
		private Listener<CommentListWrapper> listener;
		private ErrorListener errorListener;

		public Builder setLastUpdated(long lastUpdated) {
			this.lastUpdated = lastUpdated;
			return this;
		}

		/**
		 * @param type
		 *            TYPE_ASK TYPE_REPLY
		 * @return
		 */
		public Builder setType(int type) {
			this.type = type;
			return this;
		}

		public Builder setCommentId(long commentId) {
			this.commentId = commentId;
			return this;
		}

		public Builder setPid(long pid) {
			this.pid = pid;
			return this;
		}

		public Builder setPage(int page) {
			this.page = page;
			return this;
		}

		public Builder setSize(int size) {
			this.size = size;
			return this;
		}

		public Builder setNeedPhotoItem(int need) {
			this.needPhotoItem = need;
			return this;
		}

		public Builder setListener(Listener<CommentListWrapper> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public CommentListRequest build() {
			String url = createUrl();
			CommentListRequest request = new CommentListRequest(METHOD, url,
					listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("comment/index");

			sb.append("?type=").append(type);
			sb.append("&target_id=").append(pid);
			sb.append("&page=").append(page);
			sb.append("&size=").append(size);
			sb.append("&comment_id=").append(commentId);

			// 是否需要返回图片信息
			if (needPhotoItem == 1) {
				sb.append("&need_photoitem=").append(needPhotoItem);
			}

			if (lastUpdated != -1) {
				sb.append("&last_updated=").append(lastUpdated);
			}

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}

	public static class CommentListWrapper {
		public PhotoItem photoItem;
		public List<Comment> hotCommentList;
		public List<Comment> recentCommentList;
	}
}
