package com.jfinal.plugin.cron4j;

/**
 * 实现 ITask 接口的 Task，多了一个 stop 方法，插件在停止时会进行回调
 */
public interface ITask extends Runnable {
  abstract void stop();
}
