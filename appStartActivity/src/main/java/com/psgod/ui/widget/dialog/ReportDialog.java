package com.psgod.ui.widget.dialog;

import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.ReportRequest;

/**
 * 举报dialog
 * 
 * @author Rayal
 * 
 */
public class ReportDialog extends Dialog implements View.OnClickListener {
	private static final String TAG = ReportDialog.class.getSimpleName();
	public static final int REQUEST_TAKE_PHOTO = 0x770;
	public static final int REQUEST_GET_IMAGE = 0x771;

	private static Map<Integer, Long> REPORT_ID_MAP = new HashMap<Integer, Long>();
	{
		REPORT_ID_MAP.put(R.id.dialog_report_porn, -1L);
		REPORT_ID_MAP.put(R.id.dialog_report_ad, -1L);
		REPORT_ID_MAP.put(R.id.dialog_report_law, -1L);
	}

	private final Context mContext;
	private final Button mPornBtn;
	private final Button mAdBtn;
	private final Button mLawBtn;
	private final Button mCancelBtn;

	// 被举报用户
	private final PhotoItem mPhotoItem;

	public ReportDialog(Context context, PhotoItem mPhotoItem) {
		super(context, R.style.ActionSheetDialog);

		setContentView(R.layout.dialog_report);
		getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
		setCanceledOnTouchOutside(true);

		// 初始化组件
		mContext = context;

		mPornBtn = (Button) findViewById(R.id.dialog_report_porn);
		mAdBtn = (Button) findViewById(R.id.dialog_report_ad);
		mLawBtn = (Button) findViewById(R.id.dialog_report_law);
		mCancelBtn = (Button) findViewById(R.id.dialog_report_cancel);

		this.mPhotoItem = mPhotoItem;

		mPornBtn.setOnClickListener(this);
		mAdBtn.setOnClickListener(this);
		mLawBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
	}

	@Override
	public void show() {
		super.show();
		getWindow().setGravity(Gravity.BOTTOM);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_report_cancel:
			dismiss();
			break;
		case R.id.dialog_report_porn:
			reportAction("porn");
			break;
		case R.id.dialog_report_ad:
			reportAction("ad");
			break;
		case R.id.dialog_report_law:
			reportAction("law");
			break;
		default:
			dismiss();
		}

	}

	// 进行举报操作
	private void reportAction(String content) {
		ReportRequest.Builder builder = new ReportRequest.Builder()
				.setPhotoItem(mPhotoItem).setContent(content)
				.setListener(reportListener).setErrorListener(errorListener);
		ReportRequest request = builder.build();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
				.getRequestQueue();
		requestQueue.add(request);
	}

	private final Listener<Boolean> reportListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			Toast.makeText(mContext, "举报成功", Toast.LENGTH_SHORT).show();
			dismiss();
		}
	};

	private final PSGodErrorListener errorListener = new PSGodErrorListener(
			ReportDialog.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			Toast.makeText(mContext, "举报失败，请稍后再试", Toast.LENGTH_SHORT).show();
			dismiss();
		}
	};
}