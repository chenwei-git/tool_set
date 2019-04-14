package chenwei.tool_set.unit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 
 * @author chenwei
 * @since 0.0.1
 * 
 */
public class FileUtil {

  public static String getText(String path) throws Exception {
    return getText(path, StandardCharsets.UTF_8);
  }

  public static String getText(String path, Charset charset) throws Exception {
    try (InputStream is = FileUtil.class.getResourceAsStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, charset));) {
      String line = null;
      StringBuilder sb = new StringBuilder();
      while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    }
  }

  public static String getFileNameFromUrl(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
  }

  public static String getFileNameFromPath(String path) {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  public static void main(String[] args) throws Exception {
    try (Scanner sc = new Scanner(System.in);
        RandomAccessFile accessFile = new RandomAccessFile(new File("D:/a.txt"), "r");) {
      while (true) {
        String line = accessFile.readLine();
        System.out.println(accessFile.getFilePointer());
        if (line == null) {
          return;
        }
        System.out.println(new String(line.getBytes("ISO-8859-1"), "utf-8"));
        sc.nextLine();
      }
    }
  }
}
