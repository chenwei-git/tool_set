package chenwei.tool_set;

public class Calculator {
  public static void main(String[] args) {
    int currInput = 1;
    int input = currInput;
    double out = currInput;
    for (int i = 2; i <= 6; i++) {
      out = out * (1 - 0.08);
      currInput = 2 * currInput;
      input += currInput;
      out += currInput;
    }
    System.out.println(input);
    System.out.println(out);
    System.out.println(out / input);
  }
}
