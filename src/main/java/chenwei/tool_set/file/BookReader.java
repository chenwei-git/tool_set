package chenwei.tool_set.file;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author chenwei
 * @since 0.0.1
 */
public class BookReader {

  public static void main(String[] args) throws Exception {
    System.out.println("please wait to notice");
    try (RandomAccessFile book = getBook("D:/all.txt");
        Scanner commander = getCommander();) {
      processBook(book, commander);
    }
  }

  private static void processBook(RandomAccessFile book, Scanner commander)
      throws NumberFormatException, IOException, AWTException {
    String command = null;
    List<Long> linePointerList = getLinePointerList(book);
    System.out.println("please type a num , or q to quit");
    for (; !(command = commander.nextLine()).equals("q");) {
      if (isNum(command)) {
        jumpTo(book, linePointerList, Integer.valueOf(command) - 1);
      }
      clearConsole();
      read(book);
    }
    clearConsole();
  }

  public static List<Long> LinePointerList = null;
  /**
   * @param book
   * @return
   * @throws IOException
   */
  private static List<Long> getLinePointerList(RandomAccessFile book)
      throws IOException {
    if (LinePointerList != null) {
      return LinePointerList;
    }
    LinePointerList = new ArrayList<>();
    LinePointerList.add(0L);
    for (; book.readLine() != null;) {
      LinePointerList.add(book.getFilePointer());
    }
    return LinePointerList;
  }

  public static Robot robot = null;
  static {
    try {
      robot = new Robot();
    } catch (AWTException e) {
    }
  }

  public static void clearConsole() throws AWTException {
    robot.mousePress(InputEvent.BUTTON3_MASK); // 按下鼠标右键
    robot.mouseRelease(InputEvent.BUTTON3_MASK); // 释放鼠标右键
    robot.keyPress(KeyEvent.VK_CONTROL); // 按下Ctrl键
    robot.keyPress(KeyEvent.VK_R); // 按下R键
    robot.keyRelease(KeyEvent.VK_R); // 释放R键
    robot.keyRelease(KeyEvent.VK_CONTROL); // 释放Ctrl键
    robot.delay(100);
  }

  /**
   * @param book
   * @param lienPointerList
   * @param valueOf
   * @throws IOException
   */
  private static void jumpTo(RandomAccessFile book, List<Long> lienPointerList,
      int toLineIndex) throws IOException {
    int lastLineIndex = lienPointerList.size() - 1;
    if (toLineIndex > lastLineIndex) {
      toLineIndex = lastLineIndex;
    }
    book.seek(lienPointerList.get(toLineIndex));
  }

  /**
   * @param book
   * @throws IOException
   */
  private static void read(RandomAccessFile book) throws IOException {
    System.out
        .println(new String(book.readLine().getBytes("ISO-8859-1"), "utf-8"));
  }

  public static Pattern patthern = Pattern.compile("[0-9]+");

  public static boolean isNum(String str) {
    return patthern.matcher(str).matches();
  }

  private static Scanner getCommander() {
    return new Scanner(System.in);
  }

  private static RandomAccessFile getBook(String path) throws Exception {
    File file = new File(path);
    if (!file.exists()) {
      throw new Exception(
          String.format("can not find this book, path=%s", path));
    }
    RandomAccessFile accessFile = new RandomAccessFile(file, "r");
    return accessFile;
  }
}
