package chenwei.tool_set;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DownloaderManager {

  public static AtomicReference<Downloader> context = new AtomicReference<>();

  public static void main1(String[] args) throws Exception {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

    DownloadConfiguer configuer = new DownloadConfiguer();
    configuer.setThreadNum(8);
    configuer.setDefaultDir("C:\\mine\\user-data");

    Downloader downloader = new Downloader(configuer);
    context.set(downloader);

    List<String> input = helper1();
    for (String url : input) {
      DownloadTarget target = new DownloadTarget(url, null, null);
      context.get().add(target);
      context.get().start(target);
    }

    Map<String, Boolean> output = new HashMap<>();
    for (String url : input) {
      boolean r = retry(() -> {
        boolean ret = false;
        try {
          ret = context.get().resultMap.get(url).get(30, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
        if (!ret) {
          DownloadTarget target = new DownloadTarget(url, null, null);
          context.get().add(target);
          context.get().start(target);
          throw new Exception();
        }
        return true;
      }, 4, 2000, false);
      if (r) {
        output.put(url, true);
      } else {
        output.put(url, false);
      }
    }
    helper2(output);
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

    DownloadConfiguer configuer = new DownloadConfiguer();
    configuer.setThreadNum(8);
    configuer.setDefaultDir("C:\\mine\\user-data");

    Downloader downloader = new Downloader(configuer);
    context.set(downloader);

    retry(() -> {
      boolean ret = false;
      try {
        String url = "https://v01.rrmyjj.xyz/file/ts/13000/12596/d/index1004.ts";
        DownloadTarget target = new DownloadTarget(url, null, null);
        context.get().add(target);
        context.get().start(target);
        ret = context.get().resultMap.get(url).get(30, TimeUnit.MINUTES);
      } catch (Exception e) {
      }
      if (!ret) {
        throw new Exception();
      }
      return true;
    }, 4, 2000, false);
  }

  public static void main4(String[] args) {
    String dir = "C:\\mine\\user-data\\index";
    String suffix = ".ts";
    for (int i = 0; i <= 1485; i++) {
      if (!new File(dir + i + suffix).exists()) {
        System.out.println(dir + i + suffix);
      }
    }
  }

  public static void main5(String[] args) throws Exception {
    File of = new File("C:\\mine\\user-data\\new.ts");
    if (of.exists()) {
      of.delete();
    }
    String dir = "C:\\mine\\user-data\\index";
    String suffix = ".ts";
    try (FileOutputStream os = new FileOutputStream("C:\\mine\\user-data\\new.ts", true);
        FileChannel oc = os.getChannel();) {
      for (int i = 0; i <= 1485; i++) {
        try (FileInputStream is = new FileInputStream(dir + i + suffix);
            FileChannel ic = is.getChannel();) {
          oc.transferFrom(ic, oc.size(), ic.size());
        }
      }
    }
  }


  private static void helper2(Map<String, Boolean> output) throws Exception {
    Path path = Paths.get("C:\\mine\\user-data\\output.txt");
    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      for (Map.Entry<String, Boolean> entry : output.entrySet()) {
        writer.append(entry.getKey() + "," + entry.getValue() + "\n");
      }
    }
  }

  public static List<String> helper1() throws Exception {
    List<String> fileList = new ArrayList<>();
    try (Scanner sc = new Scanner(new File("C:\\mine\\user-data\\index.m3u8"))) {
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        if (line == null || line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        fileList.add("https://v01.rrmyjj.xyz/file/ts/13000/12596/d/" + line);
      }
    }
    return fileList;
  }

  public static class Downloader {
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
          target.path = this.configuer.getDefaultDir() + "\\" + getFileName(target.getUrl());
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

  public static class DownloadConfiguer {
    int threadNum;
    String defaultDir;

    public int getThreadNum() {
      return threadNum;
    }

    public void setThreadNum(int threadNum) {
      this.threadNum = threadNum;
    }

    public String getDefaultDir() {
      return defaultDir;
    }

    public void setDefaultDir(String defaultDir) {
      this.defaultDir = defaultDir;
    }
  }

  public static class DownloadTarget {
    String url;
    String path;
    ForkJoinPool pool;

    public DownloadTarget(String url, String path, ForkJoinPool pool) {
      super();
      this.url = url;
      this.path = path;
      this.pool = pool;
    }

    public Future<Boolean> start() {
      Future<Boolean> future =
          pool.submit(new DownloadTargetTask(-1, -1, 1000 * 300, -1, url, path));
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

    public ForkJoinPool getPool() {
      return pool;
    }

    public void setPool(ForkJoinPool pool) {
      this.pool = pool;
    }
  }

  public static AtomicInteger count_run = new AtomicInteger(0);
  public static AtomicInteger count_ok = new AtomicInteger(0);

  @SuppressWarnings("serial")
  public static class DownloadTargetTask extends RecursiveTask<Boolean> {
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

    @Override
    protected Boolean compute() {
      // 1 初始化
      if (amount == -1) {
        amount = retry(() -> {
          return init(url, path);
        }, 50, 2000, -1);
        if (amount == -1) {
          return false;
        }
        start = 0; // 闭区间
        end = amount % unit == 0 ? amount / unit : amount / unit + 1; // 开区间
      }
      if (end - start <= THRESHOLD) {
        return retry(() -> {
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
  }

  public static int init(String url, String path) throws Exception {
    HttpURLConnection conn = null;
    int amount = -1;
    try {
      // 1.1 获取amount
      System.out.println(
          "init +, to conn = " + count_run.incrementAndGet() + ", file = " + getFileName(url));
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
          + getFileName(url) + ", amount = " + amount);
      return amount;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("init -, to conn = " + count_run.decrementAndGet() + ", file = "
          + getFileName(url) + ", amount = " + amount);
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
            + getFileName(url) + "." + _startPos);
        return;
      }
      System.out.println("process +, conn = " + count_run.incrementAndGet() + ", file = "
          + getFileName(url) + "." + _startPos);
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
          + getFileName(url) + "." + _startPos);
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
          + getFileName(url) + "." + _startPos);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("process -, conn = " + count_run.decrementAndGet() + ", file = "
          + getFileName(url) + "." + _startPos);
      throw e;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  public static String getFileName(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
  }

  public static <T> T retry(Callable<T> caller, int maxAttempts, long interval, T deafult) {
    int _maxAttempts = maxAttempts;
    while (_maxAttempts-- > 0) {
      try {
        T t = caller.call();
        return t;
      } catch (Exception e) {
      }
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
      }
    }
    return deafult;
  }
}
