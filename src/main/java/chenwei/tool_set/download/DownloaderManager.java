package chenwei.tool_set.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import chenwei.tool_set.unit.RetryUtil;

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
      DownloadTarget target = new DownloadTarget(url, null, 1000 * 300, null);
      context.get().add(target);
      context.get().start(target);
    }

    Map<String, Boolean> output = new HashMap<>();
    for (String url : input) {
      boolean r = RetryUtil.invokeOfSimple(() -> {
        boolean ret = false;
        try {
          ret = context.get().resultMap.get(url).get(30, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
        if (!ret) {
          DownloadTarget target = new DownloadTarget(url, null, 1000 * 300, null);
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

    RetryUtil.invokeOfSimple(() -> {
      boolean ret = false;
      try {
        String url = "http://etorrent.click/torrents/a1a5-2515587.torrent";
        DownloadTarget target = new DownloadTarget(url, null, 1000 * 500, null);
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

  public static void main3(String[] args) {
    String dir = "C:\\mine\\user-data\\index";
    String suffix = ".ts";
    for (int i = 0; i <= 862; i++) {
      if (!new File(dir + i + suffix).exists()) {
        System.out.println(dir + i + suffix);
      }
    }
  }

  public static void main4(String[] args) throws Exception {
    File of = new File("C:\\mine\\user-data\\new.ts");
    if (of.exists()) {
      of.delete();
    }
    String dir = "C:\\mine\\user-data\\index";
    String suffix = ".ts";
    try (FileOutputStream os = new FileOutputStream("C:\\mine\\user-data\\new.ts", true);
        FileChannel oc = os.getChannel();) {
      for (int i = 0; i <= 862; i++) {
        try (FileInputStream is = new FileInputStream(dir + i + suffix);
            FileChannel ic = is.getChannel();) {
          oc.transferFrom(ic, oc.size(), ic.size());
        }
      }
    }
  }

  public static void main5(String[] args) throws Exception {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

    DownloadConfiguer configuer = new DownloadConfiguer();
    configuer.setThreadNum(8);
    configuer.setDefaultDir("C:/mine/user-data");

    Downloader downloader = new Downloader(configuer);
    context.set(downloader);

    Map<String, String> input = helper3();
    for (Map.Entry<String, String> entry : input.entrySet()) {
      DownloadTarget target = new DownloadTarget(entry.getKey(),
          configuer.getDefaultDir() + "/" + entry.getValue(), 1000 * 5000, null);
      context.get().add(target);
      context.get().start(target);
    }

    Map<String, Boolean> output = new HashMap<>();
    for (Map.Entry<String, String> entry : input.entrySet()) {
      boolean r = RetryUtil.invokeOfSimple(() -> {
        boolean ret = false;
        try {
          ret = context.get().resultMap.get(entry.getKey()).get(30, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
        if (!ret) {
          DownloadTarget target = new DownloadTarget(entry.getKey(),
              configuer.getDefaultDir() + "/" + entry.getValue(), 1000 * 5000, null);
          context.get().add(target);
          context.get().start(target);
          throw new Exception();
        }
        return true;
      }, 4, 2000, false);
      if (r) {
        output.put(entry.getValue() + "::" + entry.getKey(), true);
      } else {
        output.put(entry.getValue() + "::" + entry.getKey(), false);
      }
    }
    helper2(output);
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
        fileList.add("https://v01.rrmyjj.xyz/file/ts/11000/10518/d/" + line);
      }
    }
    return fileList;
  }

  public static Map<String, String> helper3() throws FileNotFoundException {
    Map<String, String> result = new HashMap<String, String>();
    try (Scanner sc = new Scanner(new File("C:\\mine\\user-data\\a.txt"))) {
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        if (line == null || line.isEmpty()) {
          continue;
        }
        String[] arry = line.split("\\:\\:");
        result.put(arry[1], arry[0]);
      }
    }
    return result;
  }
}
