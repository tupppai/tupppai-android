package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.psgod.R;
import com.psgod.model.SelectImage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultiImageSelectAdapter extends BaseAdapter {
	private final static String TAG = MultiImageSelectAdapter.class
			.getSimpleName();

	private static final int TYPE_CAMERA = 0;
	private static final int TYPE_NORMAL = 1;

	private Context mContext;
	private LayoutInflater mInflater;
	private List<SelectImage> mImages = new ArrayList<SelectImage>();
	private List<SelectImage> mSelectedImages = new ArrayList<SelectImage>();

	private int mItemSize;
	private GridView.LayoutParams mItemLayoutParams;

	public MultiImageSelectAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemLayoutParams = new GridView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	/**
	 * 选择某个图片，改变选择状态
	 * 
	 * @param image
	 */
	public void select(SelectImage image) {
		if (mSelectedImages.contains(image)) {
			mSelectedImages.remove(image);
		} else {
			mSelectedImages.add(image);
		}
		notifyDataSetChanged();
	}

	/**
	 * 通过图片路径设置默认选择
	 * 
	 * @param resultList
	 */
	public void setDefaultSelected(ArrayList<String> resultList) {
		for (String path : resultList) {
			SelectImage image = getImageByPath(path);
			if (image != null) {
				mSelectedImages.add(image);
			}
		}
		if (mSelectedImages.size() > 0) {
			notifyDataSetChanged();
		}
	}

	public SelectImage getImageByPath(String path) {
		if (mImages != null && mImages.size() > 0) {
			for (SelectImage image : mImages) {
				if (image.path.equalsIgnoreCase(path)) {
					return image;
				}
			}
		}
		return null;
	}

	/**
	 * 设置数据集
	 * 
	 * @param images
	 */
	public void setData(List<SelectImage> images) {
		mSelectedImages.clear();

		if (images != null && images.size() > 0) {
			mImages = images;
		} else {
			mImages.clear();
		}
		notifyDataSetChanged();
	}

	/**
	 * 重置每个Column的Size
	 * 
	 * @param columnWidth
	 */
	public void setItemSize(int columnWidth) {

		if (mItemSize == columnWidth) {
			return;
		}

		mItemSize = columnWidth;

		mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
	}

	@Override
	public int getCount() {
		return mImages.size() + 1;
	}

	@Override
	public SelectImage getItem(int position) {
		if (position == 0) {
			return null;
		} else {
			return mImages.get(position - 1);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		int type = getItemViewType(position);
		if (type == TYPE_CAMERA) {
			view = mInflater
					.inflate(R.layout.item_select_camera, parent, false);
		} else if (type == TYPE_NORMAL) {
			ViewHolde holde;
			if (view == null) {
				view = mInflater.inflate(R.layout.item_select_image, parent,
						false);
				holde = new ViewHolde(view);
			} else {
				holde = (ViewHolde) view.getTag();
				if (holde == null) {
					view = mInflater.inflate(R.layout.item_select_image,
							parent, false);
					holde = new ViewHolde(view);
				}
			}
			if (holde != null) {
				holde.bindData(getItem(position));
			}
		}

		/** Fixed View Size */
		GridView.LayoutParams lp = (GridView.LayoutParams) view
				.getLayoutParams();
		if (lp.height != mItemSize) {
			view.setLayoutParams(mItemLayoutParams);
		}

		return view;
	}

	class ViewHolde {
		ImageView image;
		ImageView indicator;
		ImageView cover;

		ViewHolde(View view) {
			image = (ImageView) view.findViewById(R.id.image);
			indicator = (ImageView) view.findViewById(R.id.checkmark);
			cover = (ImageView) view.findViewById(R.id.image_cover);
			view.setTag(this);
		}

		void bindData(final SelectImage data) {
			if (data == null)
				return;
			// 处理单选和多选状态
			if (mSelectedImages.contains(data)) {
				// 设置选中状态
				image.setBackgroundResource(R.drawable.shape_multi_image_selected);
				indicator
						.setImageResource(R.drawable.ic_multi_image_select_select);

				if (cover.getVisibility() == View.GONE) {
					cover.setVisibility(View.VISIBLE);
				}
			} else {
				// 未选择
				image.setBackgroundResource(R.drawable.shape_multi_image_unselect);
				indicator
						.setImageResource(R.drawable.ic_multi_image_select_normal);
				if (cover.getVisibility() == View.VISIBLE) {
					cover.setVisibility(View.GONE);
				}
			}
			File imageFile = new File(data.path);

			if (mItemSize > 0) {
				// 显示图片
				Picasso.with(mContext).load(imageFile)
						.placeholder(R.drawable.default_error)
						// .error(R.drawable.default_error)
						.resize(mItemSize, mItemSize).centerCrop().into(image);
			}
		}
	}

}
