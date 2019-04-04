package chenwei.tool_set.protobuf;

import java.util.UUID;

/**
 * @author chenwei
 * @since 0.0.1
 */
public class SequenceUtil {
  public static String getUUID32() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
}
