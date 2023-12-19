/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.kit;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.jfinal.log.Log;

/**
 * 调度工具类
 * <pre>
 * 1：scheduleWithFixedDelay 以上一次任务的 "结束时间" 为间隔调度任务
 * 2：scheduleAtFixedRate    以上一次任务的 "开始时间" 为间隔调度任务。当本次调度来临时，如果上一次任务未执行完，则等待它执行完成后再立即调度
 * 3：警告：必须要在被调度的任务(Runnable/Callable)中捕获异常，否则调度将会停止
 * </pre>
 */
public class ScheduledKit {

	private static ScheduledExecutorService executor = null;

	private ScheduledKit() {}

	/**
	 * 初始化
	 *
	 * @param corePoolSize the number of threads to keep in the pool, even if they are idle
	 */
	public synchronized static void init(int corePoolSize) {
		if (executor == null) {
			executor = Executors.newScheduledThreadPool(corePoolSize);
		} else {
			Log.getLog(ScheduledKit.class).warn(ScheduledKit.class.getName() + " 已经初始化");
		}
	}

	/**
	 * 传递 ScheduledExecutorService 对象进行初始化，从而完全掌控线程池参数
	 */
	public synchronized static void init(ScheduledExecutorService executor) {
		if (ScheduledKit.executor == null) {
			ScheduledKit.executor = executor;
		} else {
			Log.getLog(ScheduledKit.class).warn(ScheduledKit.class.getName() + " 已经初始化");
		}
	}

	public static ScheduledExecutorService getExecutor() {
		if (executor == null) {
			init(5);
		}
		return executor;
	}

	/**
	 * 以固定延迟执行任务
	 * @param initialDelay 第一次启动前的延迟
	 * @param delay 上次任务 "完成" 时间与本次任务 "开始" 时间的间隔
	 * @param unit 时间单位
	 * @param task 被执行的任务
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit, Runnable task) {
		return getExecutor().scheduleWithFixedDelay(task, initialDelay, delay, unit);
	}

	/**
	 * 任务添加 try catch ，避免 scheduleWithFixedDelay 方法在调度任务出现异常后会终止调度
 	 */
	public static ScheduledFuture<?> scheduleWithFixedDelayWithTryCatch(long initialDelay, long delay, TimeUnit unit, Runnable task) {
		return scheduleWithFixedDelay(initialDelay, delay, unit, () -> {
			try {
				task.run();
			} catch (Throwable t) {
				Log.getLog(ScheduledKit.class).error(t.getMessage(), t);
			}
		});
	}

	/**
	 * 以固定频率执行任务
	 * @param initialDelay 第一次启动前的延迟
	 * @param period 上次任务 "开始" 时间与本次任务 "开始" 时间的间隔，如果任务执行时长超出 period 值，则在任务执行完成后立即调度任务执行
	 * @param unit 时间单位
	 * @param task 被执行的任务
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable task) {
		return getExecutor().scheduleAtFixedRate(task, initialDelay, period, unit);
	}

	/**
	 * 任务添加 try catch ，避免 scheduleAtFixedRate 方法在调度任务出现异常后会终止调度
 	 */
	public static ScheduledFuture<?> scheduleAtFixedRateWithTryCatch(long initialDelay, long period, TimeUnit unit, Runnable task) {
		return scheduleAtFixedRate(initialDelay, period, unit, () -> {
			try {
				task.run();
			} catch (Throwable t) {
				Log.getLog(ScheduledKit.class).error(t.getMessage(), t);
			}
		});
	}

	/**
     * 创建一次性调度，在给定的 delay 时间后调度
     * @param delay 从现在开始的延迟时间
     * @param unit 时间单位
     * @param task 被执行任务
     */
	public static ScheduledFuture<?> schedule(long delay, TimeUnit unit, Runnable task) {
		return getExecutor().schedule(task, delay, unit);
	}

	/**
     * 创建一次性调度，在给定的 delay 时间后调度
     * @param delay 从现在开始的延迟时间
     * @param unit 时间单位
     * @param task 被执行任务
     */
	public static <V> ScheduledFuture<V> schedule(long delay, TimeUnit unit, Callable<V> task) {
		return getExecutor().schedule(task, delay, unit);
	}

	/**
	 * 等待正在执行的线程执行完毕以后，关闭线程池
	 */
	public static void shutdown() {
		if (executor != null) {
			executor.shutdown();
		}
	}

	/**
	 * 停掉正在执行的线程，关闭线程池
	 */
	public static void shutdownNow() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	/**
	 * 在 shutdown 线程池之后，阻塞等待所有任务执行完，或发生超时，或当前线程中断，以先发生者为准
	 */
	public static boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor == null || executor.awaitTermination(timeout, unit);
	}
}



