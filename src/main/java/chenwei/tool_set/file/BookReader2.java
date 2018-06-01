package chenwei.tool_set.file;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author chenwei
 * @since 0.0.1
 */
public class BookReader2 {

  public static void main(String[] args) throws Exception {
    try (Scanner commander = new Scanner(System.in);) {
      Supplier<Stream<String>> streamSupplier = () -> {
        try {
          return Files.lines(Paths.get("D:/all.txt"));
        } catch (IOException e) {
        }
        return null;
      };
      dodo(streamSupplier, commander);
    }
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
   * @param streamSupplier.get()
   * @param commander
   * @throws AWTException
   */
  private static void dodo(Supplier<Stream<String>> streamSupplier,
      Scanner commander) throws AWTException {
    String command = null;
    int lineNum = 0;

    for (; !(command = commander.nextLine()).equals("q");) {
      clearConsole();
      if (isNum(command)) {
        lineNum = Integer.valueOf(command) - 1;
        helper(streamSupplier, lineNum);
      } else {
        helper(streamSupplier, lineNum);
      }
      lineNum++;
    }
    clearConsole();
  }

  /**
   * @param streamSupplier
   * @param lineNum
   */
  private static void helper(Supplier<Stream<String>> streamSupplier,
      int lineNum) {
    streamSupplier.get().skip(lineNum).findFirst()
        .filter((s) -> s != null && !s.isEmpty())
        .ifPresent((s) -> System.out.println(getSplit(s, 80)));
  }

  /**
   * @param s
   * @param i
   * @return
   */
  private static String getSplit(String s, int size) {
    int length = s.length();
    int startIndex = 0;
    int nextIndex = 0;

    StringBuilder sb = new StringBuilder();
    for (; startIndex < length;) {
      nextIndex = startIndex + size;
      if (nextIndex > length) {
        nextIndex = length;
      }
      sb.append(s.subSequence(startIndex, nextIndex)).append("\n");
      startIndex = nextIndex;
    }
    return sb.toString();
  }

  public static Pattern patthern = Pattern.compile("[0-9]+");

  public static boolean isNum(String str) {
    return patthern.matcher(str).matches();
  }
}
