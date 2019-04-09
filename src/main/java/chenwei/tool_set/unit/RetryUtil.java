package chenwei.tool_set.unit;

import java.util.concurrent.Callable;

/**
 * @author chenwei
 * @since 0.0.1
 */
public class RetryUtil {

  public static <T> T invokeOfSimple(Callable<T> caller, int maxAttempts, long interval,
      T deafult) {
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
