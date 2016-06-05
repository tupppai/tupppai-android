package com.pires.wesee.db;

/**
 * Comment Dao
 * @author brandwang
 */

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.pires.wesee.model.Comment;
import com.pires.wesee.model.PhotoItem;

import java.util.List;

public class CommentDao {
	private Dao<Comment, Long> commentDao;
	private DatabaseHelper helper;

	public CommentDao(Context context) {
		try {
			helper = DatabaseHelper.getHelper(context);
			commentDao = helper.getDao(Comment.class);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 添加一条Comment
	 */
	public void add(Comment comment) {
		try {
			commentDao.create(comment);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 通过id获得一条Comment 同时获取其对应的PhotoItem
	 */
	public Comment getCommentWithPhotoItem(long id) {
		Comment comment = null;
		try {
			comment = commentDao.queryForId(id);
			helper.getDao(PhotoItem.class).refresh(comment.getPhotoItem());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return comment;
	}

	/**
	 * 通过id获得一条Comment 不获取其对应的PhotoItem
	 */
	public Comment get(long id) {
		Comment comment = null;
		try {
			comment = commentDao.queryForId(id);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return comment;
	}

	/**
	 * 通过photoItem id获取全部Comment
	 */
	public List<Comment> getAllCommentsByPid(long pid) {
		try {
			return commentDao.queryBuilder().where().eq("pid", pid).query();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
}
