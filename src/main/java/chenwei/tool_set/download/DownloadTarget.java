package chenwei.tool_set.download;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class DownloadTarget {
  String url;
  String path;
  int unit;
  ForkJoinPool pool;

  public DownloadTarget(String url, String path, int unit, ForkJoinPool pool) {
    super();
    this.url = url;
    this.path = path;
    this.unit = unit;
    this.pool = pool;
  }

  public Future<Boolean> start() {
    Future<Boolean> future = pool.submit(new DownloadTargetTask(-1, -1, unit, -1, url, path));
    return future;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getUnit() {
    return unit;
  }

  public void setUnit(int unit) {
    this.unit = unit;
  }

  public ForkJoinPool getPool() {
    return pool;
  }

  public void setPool(ForkJoinPool pool) {
    this.pool = pool;
  }
}
