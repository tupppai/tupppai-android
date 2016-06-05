package com.pires.wesee;

public class PSGodToast implements Comparable {
	public static final long DURATION_FOREVER = -1;
	public static final long DURATION_DEFAULT = 2000; // 2秒

	public static final int PRIORITY_HIGHEST = 5;
	public static final int PRIORITY_HIGH = 4;
	public static final int PRIORITY_NORMAL = 3;
	public static final int PRIORITY_LOW = 2;
	public static final int PRIORITY_LOWEST = 1;

	private static long ID = 0;

	private long mId;
	private String mContent;
	private long mDuration;
	private int mPriority;
	private long mCreateTime;

	public PSGodToast(String content) {
		this(content, DURATION_DEFAULT, PRIORITY_NORMAL);
	}

	public PSGodToast(String content, long duration) {
		this(content, duration, PRIORITY_NORMAL);
	}

	public PSGodToast(String content, long duration, int priority) {
		mId = ID++;
		this.mContent = content;
		this.mDuration = duration;
		this.mPriority = priority;
		this.mCreateTime = System.currentTimeMillis();
	}

	public long getId() {
		return mId;
	}

	public String getContent() {
		return mContent;
	}

	public long getDuration() {
		return mDuration;
	}

	public int getPriority() {
		return mPriority;
	}

	public long getCreateTime() {
		return mCreateTime;
	}

	@Override
	public int compareTo(Object another) {
		if (another instanceof PSGodToast) {
			PSGodToast toast = (PSGodToast) another;
			if (mPriority > toast.mPriority) {
				return -1;
			} else if (mPriority < toast.mPriority) {
				return 1;
			} else if (mCreateTime < toast.mCreateTime) {
				return -1;
			} else if (mCreateTime > toast.mCreateTime) {
				return 1;
			} else {
				return 0;
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		Long createTime = mCreateTime;
		return createTime.hashCode();
	}
}
