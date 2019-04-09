package chenwei.tool_set.protobuf;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import chenwei.tool_set.protobuf.MailSendTriggerProto.MailSendTriggerReq;
import chenwei.tool_set.unit.SequenceUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class HttpTool {

  public static final MediaType PROTOBUF = MediaType.get("application/x-protobuf; charset=utf-8");

  public static OkHttpClient client =
      new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build();

  public static String post(String url, MailSendTriggerReq req) throws IOException {
    RequestBody body = new RequestBody() {
      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        sink.write(req.toByteArray());
      }

      @Override
      public MediaType contentType() {
        return MediaType.parse("application/x-protobuf");
      }
    };
    Request request = new Request.Builder().url(url).header("App-Id", "mail-app-uca-0")
        .header("Msg-Id", SequenceUtil.getUUID32()).post(body).build();
    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }

  public static MailSendTriggerReq buildRequest() {
    MailSendTriggerReq.Builder builder = MailSendTriggerReq.newBuilder();
    builder.setSubject("hello world 2057");
    builder.setTemplateCode("mail-temp-0");
    builder.addAllTo(Arrays.asList("wechen@glprop.com"));
    return builder.build();
  }

  public static void main(String[] args) throws IOException {
    System.out.println(post("http://localhost:9061/mail/send/trigger", buildRequest()));
  }
}
