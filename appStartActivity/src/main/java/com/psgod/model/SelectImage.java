package com.psgod.model;

/**
 * 图片实体 多图选择 Created by ZouMengyuan
 */
public class SelectImage {

	public String path;
	public String name;
	public long time;

	public SelectImage(String path, String name, long time) {
		this.path = path;
		this.name = name;
		this.time = time;
	}

	@Override
	public boolean equals(Object o) {
		try {
			SelectImage other = (SelectImage) o;
			return this.path.equalsIgnoreCase(other.path);
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return super.equals(o);
	}

}
