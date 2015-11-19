package com.psgod;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public final class ThreadManager {
	// public static final boolean DEBUG_THREAD = false &&
	// AppSetting.isDebugVersion;
	private static final String TAG = ThreadManager.class.getSimpleName();

	/**
	 * AsyncTask的默认线程池Executor. 负责长时间的任务(网络访问) 默认3个线程
	 */
	public static final Executor NETWORK_EXECUTOR;

	/**
	 * 副线程的Handle, 只有一个线程 可以执行比较快但不能在ui线程执行的操作. 文件读写不建议在此线程执行,
	 * 请使用FILE_THREAD_HANDLER 此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR
	 */
	private static Handler SUB_THREAD_HANDLER;

	private static HandlerThread SUB_THREAD;

	/**
	 * 文件读写线程的Handle, 只有一个线程 可以执行文件读写操作, 如图片解码等 此线程禁止进行网络操作.如果需要进行网络操作.
	 * 请使用NETWORK_EXECUTOR
	 */
	private static Handler FILE_THREAD_HANDLER;
	/**
	 * 文件读写用的线程
	 */
	private static HandlerThread FILE_THREAD;

	/**
	 * 负责消息处理
	 */
	private static Handler MESSAGE_THREAD_HANDLER;

	/**
	 * 负责消息tab的刷新，建议其他不要用
	 */
	private static Handler RECENT_THREAD_HANDLER;

	/**
	 * 负责电话Tab的刷新
	 */
	private static Handler CALL_THREAD_HANDLER;

	/**
	 * 公共Timer
	 */
	private static Timer TIMER;

	static {
		NETWORK_EXECUTOR = initNetworkExecutor();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static Executor initNetworkExecutor() {
		Executor result = null;

		if (VersionUtils.isHoneycomb()) {
			result = AsyncTask.THREAD_POOL_EXECUTOR;
		} else {
			// 3.0以下, 反射获取
			try {
				Field field = AsyncTask.class.getDeclaredField("sExecutor");
				field.setAccessible(true);
				result = (Executor) field.get(null);
			} catch (Exception e) {
				Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
						e.getMessage());
				result = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>());
			}
		}

		if (result instanceof ThreadPoolExecutor) {
			// core size减少为3个
			ThreadPoolExecutor tmp = (ThreadPoolExecutor) result;
			tmp.setCorePoolSize(3);
			tmp.setRejectedExecutionHandler(new AbortPolicy());
		}
		return result;
	}

	/**
	 * 在网络线程上执行异步操作. 该线程池负责网络请求等操作 长时间的执行(如网络请求使用此方法执行) 当然也可以执行其他 线程和AsyncTask公用
	 * 
	 * @param run
	 */
	public static void executeOnNetWorkThread(Runnable run) {
		try {
			NETWORK_EXECUTOR.execute(run);
		} catch (RejectedExecutionException e) {
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
					e.getMessage());
		}
	}

	// private static class DebugableHandlerThread extends HandlerThread {
	// public DebugableHandlerThread(String name) {
	// super(name);
	// }
	//
	// @Override
	// protected void onLooperPrepared() {
	// try {
	// Method m = MessageQueue.class.getDeclaredMethod("next");
	// m.setAccessible(true);
	// final MessageQueue queue = Looper.myQueue();
	//
	// // Make sure the identity of this thread is that of the local
	// // process,
	// // and keep track of what that identity token actually is.
	// Binder.clearCallingIdentity();
	// final long ident = Binder.clearCallingIdentity();
	//
	// for (;;) {
	// // Message msg = queue.next(); // might block
	// Message msg = (Message) m.invoke(queue);
	// if (msg == null) {
	// // No message indicates that the message queue is
	// // quitting.
	// return;
	// }
	//
	// if (msg.getCallback() != null) {
	// TraceUtils.traceBegin(msg.getCallback().getClass()
	// .getName()
	// + "." + "run");
	// msg.getCallback().run();
	// TraceUtils.traceEnd();
	// } else {
	// final Handler handler = msg.getTarget();
	//
	// // String clzName = null;
	// Field f = Handler.class.getDeclaredField("mCallback");
	// f.setAccessible(true);
	// Handler.Callback callback = (Callback) f.get(handler);
	//
	// if (callback != null) {
	// TraceUtils.traceBegin(callback.getClass().getName()
	// + "." + "dispatchMsg");
	// callback.handleMessage(msg);
	// TraceUtils.traceEnd();
	// } else {
	// TraceUtils
	// .traceBegin(handler + "." + "dispatchMsg");
	// handler.handleMessage(msg);
	// TraceUtils.traceEnd();
	// }
	// }
	// // Make sure that during the course of dispatching the
	// // identity of the thread wasn't corrupted.
	// final long newIdent = Binder.clearCallingIdentity();
	// msg.recycle();
	// }
	// } catch (Exception e) {
	// // TODO 自动生成的 catch 块
	// e.printStackTrace();
	// }
	// }
	// }

	/**
	 * 获得文件线程的Handler.<br>
	 * 副线程可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
	 * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
	 * 
	 * @return handler
	 */
	public static Handler getFileThreadHandler() {
		if (FILE_THREAD_HANDLER == null) {
			synchronized (ThreadManager.class) {
				FILE_THREAD = new HandlerThread("QQ_FILE_RW");
				FILE_THREAD.start();
				FILE_THREAD_HANDLER = new Handler(FILE_THREAD.getLooper());
			}
		}
		return FILE_THREAD_HANDLER;
	}

	public static Looper getFileThreadLooper() {
		return getFileThreadHandler().getLooper();
	}

	public static Thread getSubThread() {
		if (SUB_THREAD == null) {
			getSubThreadHandler();
		}
		return SUB_THREAD;
	}

	/**
	 * 获得副线程的Handler.<br>
	 * 副线程可以执行比较快但不能在ui线程执行的操作.<br>
	 * 另外, 文件读写建议放到FileThread中执行 <b>此线程禁止进行网络操作.如果需要进行网络操作.
	 * 请使用NETWORK_EXECUTOR</b>
	 * 
	 * @return handler
	 */
	public static Handler getSubThreadHandler() {
		if (SUB_THREAD_HANDLER == null) {
			synchronized (ThreadManager.class) {
				SUB_THREAD = new HandlerThread("QQ_SUB");
				SUB_THREAD.start();
				SUB_THREAD_HANDLER = new Handler(SUB_THREAD.getLooper());
			}
		}
		return SUB_THREAD_HANDLER;
	}

	public static Looper getSubThreadLooper() {
		return getSubThreadHandler().getLooper();
	}

	/**
	 * 在副线程执行. <br>
	 * 可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
	 * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
	 * 
	 * @return
	 */
	public static void executeOnSubThread(Runnable run) {
		getSubThreadHandler().post(run);
	}

	/**
	 * 副线程可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
	 * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
	 */
	public static void executeOnFileThread(Runnable run) {
		getFileThreadHandler().post(run);
	}

	/**
	 * 获取用来消息处理的Handler<br>
	 * <b>只提供给消息处理线程使用. 其他人不要用</b>
	 */
	public static Handler getMessageThreadHandler() {
		if (MESSAGE_THREAD_HANDLER == null) {
			synchronized (ThreadManager.class) {
				HandlerThread thread = new HandlerThread("Msg_Handler");
				thread.start();
				MESSAGE_THREAD_HANDLER = new Handler(thread.getLooper());
			}
		}
		return MESSAGE_THREAD_HANDLER;
	}

	/**
	 * 用来获取消息tab的刷新数据的线程Looper <b>只提供给消息Tab，用来刷新数据用的，其他人不要用</b>
	 */
	public static Looper getRecentThreadLooper() {
		if (RECENT_THREAD_HANDLER == null) {
			synchronized (ThreadManager.class) {
				HandlerThread thread = new HandlerThread("Recent_Handler");
				thread.start();
				RECENT_THREAD_HANDLER = new Handler(thread.getLooper());
			}
		}
		return RECENT_THREAD_HANDLER.getLooper();
	}

	// /**
	// * 用来获取电话Tab的刷新数据的线程Looper
	// */
	// /**
	// * 获取全局的Timer
	// *
	// * @return
	// */
	// public static Timer getTimer() {
	// if (TIMER == null) {
	// synchronized (ThreadManager.class) {
	// TIMER = new Timer("QQ_Timer") {
	// @Override
	// public void cancel() {
	// if (QLog.isColorLevel()) {
	// QLog.e("ThreadManager", QLog.CLR,
	// "Can't cancel Global Timer");
	// }
	// // 全局Timer禁止cancel
	// // 为了保证安全, 发布版本添加容错处理,
	// // 非发布版, 抛异常通知开发
	// if (!AppSetting.isPublicVersion) {
	// throw new RuntimeException(
	// "Can't cancel Global Timer");
	// }
	// }
	//
	// @Override
	// public void schedule(TimerTask task, long delay) {
	// try {
	// super.schedule(task, delay);
	// } catch (Exception ex) {
	// if (QLog.isColorLevel()) {
	// QLog.d("ThreadManager", QLog.CLR,
	// "timer schedule err", ex);
	// }
	// }
	// }
	//
	// @Override
	// public void schedule(TimerTask task, long delay, long period) {
	// try {
	// super.schedule(task, delay, period);
	// } catch (Exception ex) {
	// if (QLog.isColorLevel()) {
	// QLog.d("ThreadManager", QLog.CLR,
	// "timer schedule2 err", ex);
	// }
	// }
	// }
	//
	// };
	// if (DEBUG_THREAD) {
	// TIMER.schedule(new TimerTask() {
	//
	// @Override
	// public void run() {
	// TraceUtils.traceBegin("Timer_thread");
	// System.out.println();
	// TraceUtils.traceEnd();
	// }
	// }, 0);
	// }
	// }
	// }
	// return TIMER;
	// }

	/**
	 * 返回一个"线性"的Executor. <br>
	 * 通过此对象的execute()方法执行的任务会串行执行. 但不会新建线程, 而是使用线程池中的线程, 因此仍然会受到线程池的限制.<br>
	 * 
	 * 注意每次调用该方法都会返回一个新的对象.只有同一个对象的execute()方法才会串行执行,不同对象的execute方法是不会串行访问的.
	 * 这一点需要注意<br>
	 * 
	 * @return
	 */
	public static Executor newSerialExecutor() {
		return new SerialExecutor();
	}

	private static class SerialExecutor implements Executor {
		final Queue<Runnable> mTasks = new LinkedList<Runnable>();
		Runnable mActive;

		@Override
		public synchronized void execute(final Runnable r) {
			mTasks.offer(new Runnable() {
				@Override
				public void run() {
					try {
						r.run();
					} finally {
						scheduleNext();
					}
				}
			});
			if (mActive == null) {
				scheduleNext();
			}
		}

		protected synchronized void scheduleNext() {
			if ((mActive = mTasks.poll()) != null) {
				NETWORK_EXECUTOR.execute(mActive);
			}
		}
	}

	// public static class ShowQueueAbortPolicy extends AbortPolicy {
	// @Override
	// public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
	// BlockingQueue<Runnable> bq = e.getQueue();
	// if (bq != null && !bq.isEmpty()) {
	// try {
	// for (Runnable bu : bq) {
	// try {
	// Field field;
	// field = bu.getClass().getDeclaredField("this$0");
	// field.setAccessible(true);
	// TraceUtils.traceBegin("Queue details."
	// + field.get(bu).getClass());
	// } catch (NoSuchFieldException e1) {
	// TraceUtils.traceBegin("Queue details."
	// + bu.getClass());
	// }
	// }
	// } catch (IllegalArgumentException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// } catch (IllegalAccessException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// }
	// super.rejectedExecution(r, e);
	// }
	// }
}
