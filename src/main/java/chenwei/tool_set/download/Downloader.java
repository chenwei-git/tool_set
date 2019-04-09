package chenwei.tool_set.download;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import chenwei.tool_set.unit.FileUtil;

public class Downloader {

  ReadWriteLock lock = new ReentrantReadWriteLock();
  DownloadConfiguer configuer = null;
  ConcurrentHashMap<String, DownloadTarget> targetMap = null;
  ConcurrentHashMap<String, Future<Boolean>> resultMap = null;
  ForkJoinPool pool = null;

  public Downloader(DownloadConfiguer configuer) {
    this.configuer = configuer;
    this.targetMap = new ConcurrentHashMap<>();
    this.resultMap = new ConcurrentHashMap<>();
    this.pool = new ForkJoinPool(configuer.getThreadNum());
  }

  public boolean add(DownloadTarget target) {
    lock.writeLock().lock();
    try {
      if (targetMap.containsKey(target.getUrl())) {
        return false;
      }
      if (target.path == null) {
        target.path =
            this.configuer.getDefaultDir() + "\\" + FileUtil.getFileNameFromUrl(target.getUrl());
      }
      if (target.unit == 0) {
        target.unit = this.configuer.getBlockSize();
      }
      if (target.pool == null) {
        target.pool = this.pool;
      }
      targetMap.putIfAbsent(target.getUrl(), target);
      return true;
    } finally {
      lock.writeLock().unlock();
    }
  }

  public boolean del(DownloadTarget target) {
    lock.writeLock().lock();
    try {
      if (!targetMap.containsKey(target.url)) {
        return false;
      }
      targetMap.remove(target.url);
      return true;
    } finally {
      lock.writeLock().unlock();
    }
  }

  public boolean start(DownloadTarget target) {
    lock.readLock().lock();
    try {
      if (!targetMap.containsKey(target.url)) {
        return false;
      }
      resultMap.put(target.url, targetMap.get(target.url).start());
      return true;
    } finally {
      lock.readLock().unlock();
    }
  }
}
