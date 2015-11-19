package com.psgod.ui.widget.dialog;

/**
 * 弹出确认框
 * @author brandwang
 */
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.psgod.R;

public class CustomDialog extends Dialog {

	public CustomDialog(Context context) {
		super(context);

		// this.addContentView(LayoutInflater.from(context).inflate(R.layout.dialog_custom,
		// null), new LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		// this.setCustomTitle(null);
		// this.addContentView(LayoutInflater.from(context).inflate(R.layout.dialog_custom,
		// null), new LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	public static class Builder {
		private Context context;
		// dialog上方的message header
		private String message;
		// //dialog正文的title
		// private String title;
		// //dialog正文的描述
		// private String detail;

		private String leftBtnText;
		private String rightBtnText;
		// private View contentView;
		private DialogInterface.OnClickListener leftBtnOnClickListener;
		private DialogInterface.OnClickListener rightBtnOnClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		// /**
		// * Set the Dialog title from resource
		// *
		// * @param title
		// * @return
		// */
		// public Builder setTitle(int title) {
		// this.title = (String) context.getText(title);
		// return this;
		// }
		//
		// /**
		// * Set the Dialog title from String
		// *
		// * @param title
		// * @return
		// */
		//
		// public Builder setTitle(String title) {
		// this.title = title;
		// return this;
		// }

		// public Builder setDetail(String detail) {
		// this.detail = detail;
		// return this;
		// }
		// public Builder setDetail(int detail) {
		// this.detail = (String) context.getDetail(detail);
		// return this;
		// }

		// public Builder setContentView(View v) {
		// this.contentView = v;
		// return this;
		// }

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @return
		 */
		public Builder setRightButton(int rightButtonText,
				DialogInterface.OnClickListener listener) {
			this.rightBtnText = (String) context.getText(rightButtonText);
			this.rightBtnOnClickListener = listener;
			return this;
		}

		public Builder setRightButton(String rightButtonText,
				DialogInterface.OnClickListener listener) {
			this.rightBtnText = rightButtonText;
			this.rightBtnOnClickListener = listener;
			return this;
		}

		public Builder setLeftButton(int leftButtonText,
				DialogInterface.OnClickListener listener) {
			this.leftBtnText = (String) context.getText(leftButtonText);
			this.leftBtnOnClickListener = listener;
			return this;
		}

		public Builder setLeftButton(String leftButtonText,
				DialogInterface.OnClickListener listener) {
			this.leftBtnText = leftButtonText;
			this.leftBtnOnClickListener = listener;
			return this;
		}

		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(context,
					R.style.Dialog);
			View layout = inflater.inflate(R.layout.dialog_custom, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			// set the left button
			if (leftBtnText != null) {
				Button leftBtn = (Button) layout
						.findViewById(R.id.dialog_custom_left_btn);
				leftBtn.setText(leftBtnText);
				if (leftBtnOnClickListener != null) {
					leftBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							leftBtnOnClickListener.onClick(dialog,
									DialogInterface.BUTTON_POSITIVE);
							dialog.dismiss();
						}
					});
				} else {
					leftBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.dialog_positive_button).setVisibility(
						View.GONE);
			}

			// set the cancel button
			if (rightBtnText != null) {
				Button rightBtn = (Button) layout
						.findViewById(R.id.dialog_custom_right_btn);
				rightBtn.setText(rightBtnText);
				if (rightBtnOnClickListener != null) {
					rightBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							rightBtnOnClickListener.onClick(dialog,
									DialogInterface.BUTTON_NEGATIVE);
							dialog.dismiss();
						}
					});
				} else {
					rightBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.dialog_negative_button).setVisibility(
						View.GONE);
			}

			if (message != null) {
				((TextView) layout.findViewById(R.id.dialog_custom_content_tv))
						.setText(message);
			}

			return dialog;
		}
	}
}
