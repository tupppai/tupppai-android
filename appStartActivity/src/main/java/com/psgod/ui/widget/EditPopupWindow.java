package com.psgod.ui.widget;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.Comment;
import com.psgod.model.Comment.ReplyComment;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PostCommentRequest;
import com.psgod.ui.view.FaceRelativeLayout;
import com.psgod.ui.view.FaceRelativeLayout.OnEmojiClickListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditPopupWindow implements Callback {

	private Context mContext;
	private Comment comment;
	private PhotoItem photoItem;
	private FaceRelativeLayout contentView;
	private EditText commentEdit;
	private PopupWindow commentWindow;
	private View parent;
	private OnResponseListener onResponseListener;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

	public EditPopupWindow(Comment comment, Context mContext,
			PhotoItem photoItem, View parent) {
		super();
		this.comment = comment;
		this.mContext = mContext;
		this.photoItem = photoItem;
		this.parent = parent;
	}

	public void show(int Y) {
		init();
		commentWindow.showAsDropDown(parent, 0, Utils.dpToPx(mContext, Y),
				Gravity.BOTTOM);
	}

	public void dismiss() {
		commentWindow.dismiss();
	}

	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public FaceRelativeLayout getContentView() {
		return contentView;
	}

	public void setContentView(FaceRelativeLayout contentView) {
		this.contentView = contentView;
	}

	public EditText getCommentEdit() {
		return commentEdit;
	}

	public void setCommentEdit(EditText commentEdit) {
		this.commentEdit = commentEdit;
	}

	public PopupWindow getCommentWindow() {
		return commentWindow;
	}

	public void setCommentWindow(PopupWindow commentWindow) {
		this.commentWindow = commentWindow;
	}

	public View getParent() {
		return parent;
	}

	public void setParent(View parent) {
		this.parent = parent;
	}

	private void init() {
		contentView = (FaceRelativeLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.widge_face_relativelayout, null);

		commentWindow = new PopupWindow(contentView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);
		commentEdit = (EditText) contentView
				.findViewById(R.id.activity_comment_list_input_panel);

		TextView sendTxt = (TextView) contentView
				.findViewById(R.id.activity_comment_list_post_btn);
		commentEdit.setFocusableInTouchMode(true);

		String authorName = comment.getNickname();
		// 评论人id
		Long authorId = comment.getUid();
		// 评论id
		Long cid = comment.getCid();

		StringBuilder sb = new StringBuilder();
		Long replyToCid = 0l;

		LoginUser user = LoginUser.getInstance();
		// 和自己的id做比较
		if (authorId != user.getUid()) {
			commentEdit.setHint("回复@" + authorName + ":");
			replyToCid = cid;

			sb.append("//@" + comment.getNickname() + ":"
					+ comment.getContent());
			List<ReplyComment> mReplyComments = comment.getReplyComments();
			for (int i = 0; i < mReplyComments.size(); i++) {
				sb.append("//@" + mReplyComments.get(i).mNick + ":");
				sb.append(mReplyComments.get(i).mContent);
			}
		} else {
			commentEdit.setHint("添加评论");
			replyToCid = 0l;
		}

		contentView.setOnEmojiClickListener(onEmojiClickListener);

		fixedThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(200);
					Message msg = mHandler.obtainMessage();
					msg.obj = commentEdit;
					mHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		sendTxt.setTag(commentEdit);
		sendTxt.setOnClickListener(sendCommentClick);

		commentWindow.setTouchable(true);
		commentWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return false;
			}
		});
		commentWindow.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.color.white));
		// commentWindow.setAnimationStyle(R.style.popwindow_com_anim_style);
	}

	private OnEmojiClickListener onEmojiClickListener = new OnEmojiClickListener() {

		@Override
		public void onEmojiShowListener(View emoji) {
			commentWindow.dismiss();
			commentWindow.showAsDropDown(parent, 0,
					Utils.dpToPx(mContext, -125), Gravity.BOTTOM);
		}

		@Override
		public void onEmojiHideListener(View emoji) {
			commentWindow.dismiss();
			commentWindow.showAsDropDown(parent, 0, Utils.dpToPx(mContext, 0),
					Gravity.BOTTOM);
			fixedThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(200);
						Message msg = mHandler.obtainMessage();
						msg.obj = commentEdit;
						mHandler.sendMessage(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
		}
	};

	private OnClickListener sendCommentClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			EditText mCommentEditText = (EditText) view.getTag();
			String commentContent = mCommentEditText.getText().toString();
			if (TextUtils.isEmpty(commentContent)) {
				mCommentEditText.requestFocus();
			} else {
				// 后台发送评论
				PostCommentRequest.Builder builder = new PostCommentRequest.Builder()
						.setContent(commentContent).setCid(comment.getCid())
						.setPid(photoItem.getPid())
						.setType(photoItem.getType())
						.setListener(sendCommentListener)
						.setErrorListener(new ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								if (onResponseListener != null) {
									onResponseListener.onErrorResponse(error,
											EditPopupWindow.this);
								}
							}
						});

				PostCommentRequest request = builder.build();
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);

				commentWindow.dismiss();

			}
		}
	};

	public interface OnResponseListener {
		void onResponse(Long response, EditPopupWindow window);

		void onErrorResponse(VolleyError error, EditPopupWindow window);
	}

	private Listener<Long> sendCommentListener = new Listener<Long>() {
		@Override
		public void onResponse(Long response) {
			if (response != null) {
				if (onResponseListener != null) {
					onResponseListener.onResponse(response,
							EditPopupWindow.this);
				}
			}
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		callInputPanel((EditText) msg.obj);
		return true;
	}

	private void callInputPanel(View edit) {
		// 唤起输入键盘 并输入框取得焦点
		edit.setFocusableInTouchMode(true);
		edit.requestFocus();

		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(edit, 0);
	}

	public void setOnResponseListener(OnResponseListener onResponseListener) {
		this.onResponseListener = onResponseListener;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public PhotoItem getPhotoItem() {
		return photoItem;
	}

	public void setPhotoItem(PhotoItem photoItem) {
		this.photoItem = photoItem;
	}

}
