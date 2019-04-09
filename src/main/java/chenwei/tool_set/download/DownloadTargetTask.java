package chenwei.tool_set.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import chenwei.tool_set.unit.FileUtil;
import chenwei.tool_set.unit.RetryUtil;

@SuppressWarnings("serial")
public class DownloadTargetTask extends RecursiveTask<Boolean> {
  static final int THRESHOLD = 1;
  int start;
  int end;
  int unit;
  int amount;
  String url;
  String path;

  public DownloadTargetTask(int start, int end, int unit, int amount, String url, String path) {
    super();
    this.start = start;
    this.end = end;
    this.unit = unit;
    this.amount = amount;
    this.url = url;
    this.path = path;
  }

  public static AtomicInteger count_run = new AtomicInteger(0);
  public static AtomicInteger count_ok = new AtomicInteger(0);

  @Override
  protected Boolean compute() {
    // 1 初始化
    if (amount == -1) {
      amount = RetryUtil.invokeOfSimple(() -> {
        return init(url, path);
      }, 50, 2000, -1);
      if (amount == -1) {
        return false;
      }
      start = 0; // 闭区间
      end = amount % unit == 0 ? amount / unit : amount / unit + 1; // 开区间
    }
    if (end - start <= THRESHOLD) {
      return RetryUtil.invokeOfSimple(() -> {
        process(start, end, unit, amount, url, path);
        return true;
      }, 50, 2000, false);
    }
    // 2 分
    int middle = (start + end) / 2;
    DownloadTargetTask left = new DownloadTargetTask(start, middle, unit, amount, url, path);
    DownloadTargetTask right = new DownloadTargetTask(middle, end, unit, amount, url, path);
    left.fork();
    right.fork();
    return left.join() & right.join();
  }

  public static int init(String url, String path) throws Exception {
    HttpURLConnection conn = null;
    int amount = -1;
    try {
      // 1.1 获取amount
      System.out.println("init +, to conn = " + count_run.incrementAndGet() + ", file = "
          + FileUtil.getFileNameFromUrl(url));
      URL $url = new URL(url);
      conn = (HttpURLConnection) $url.openConnection();
      conn.setConnectTimeout(1000 * 10);
      conn.setReadTimeout(1000 * 20);
      conn.setRequestMethod("GET");
      if (conn.getResponseCode() != 200) {
        throw new Exception("respcode != 200");
      }
      amount = conn.getContentLength();
      File file = new File(path);
      if (!file.exists()) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rwd");) {
          raf.setLength(amount);
        }
      }
      System.out.println("init -, to conn = " + count_run.decrementAndGet() + ", file = "
          + FileUtil.getFileNameFromUrl(url) + ", amount = " + amount);
      return amount;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("init -, to conn = " + count_run.decrementAndGet() + ", file = "
          + FileUtil.getFileNameFromUrl(url) + ", amount = " + amount);
      throw e;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  public static void process(int start, int end, int unit, int amount, String url, String path)
      throws Exception {
    int startPos = start * unit;
    int _startPos = startPos;
    int endPos = end * unit - 1;
    if (endPos >= amount) {
      endPos = amount - 1;
    }
    HttpURLConnection conn = null;
    try {
      File file = new File(path + "." + startPos);
      if (file.exists() && file.length() > 0) {
        try (BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
          String $startPos = br.readLine();
          if ($startPos != null && !$startPos.isEmpty()) {
            startPos = Integer.parseInt($startPos);
          }
        }
      }
      if (startPos >= endPos) {
        System.out.println("process, success = " + count_ok.incrementAndGet() + ", file = "
            + FileUtil.getFileNameFromUrl(url) + "." + _startPos);
        return;
      }
      System.out.println("process +, conn = " + count_run.incrementAndGet() + ", file = "
          + FileUtil.getFileNameFromUrl(url) + "." + _startPos);
      URL $url = new URL(url);
      conn = (HttpURLConnection) $url.openConnection();
      conn.setConnectTimeout(1000 * 10); // 与请求网址的服务器建立连接的超时时间
      conn.setReadTimeout(1000 * 20); // 建立连接后如果指定时间内服务器没有返回数据则超时
      conn.setRequestMethod("GET");
      if (endPos == amount - 1) {
        conn.setRequestProperty("Range", "bytes=" + startPos + "-");
      } else {
        conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
      }
      System.out.println("process ~, " + "bytes=" + startPos + "-" + endPos + ", file = "
          + FileUtil.getFileNameFromUrl(url) + "." + _startPos);
      if (conn.getResponseCode() != 206) {
        throw new Exception("respcode != 206");
      }
      try (InputStream is = conn.getInputStream();
          RandomAccessFile contRaf = new RandomAccessFile(new File(path), "rwd");
          RandomAccessFile posRaf = new RandomAccessFile(file, "rwd");) {
        byte[] bytes = new byte[1024];
        int size = 0;
        while ((size = is.read(bytes)) > 0) {
          contRaf.seek(startPos);
          contRaf.write(bytes, 0, size);
          startPos += size;
          posRaf.setLength(0);
          posRaf.write((startPos + "").getBytes());
        }
      }
      System.out.println("process -, conn = " + count_run.decrementAndGet() + ", file = "
          + FileUtil.getFileNameFromUrl(url) + "." + _startPos);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("process -, conn = " + count_run.decrementAndGet() + ", file = "
          + FileUtil.getFileNameFromUrl(url) + "." + _startPos);
      throw e;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }
}
