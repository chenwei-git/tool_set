package chenwei.tool_set.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.protobuf.ByteString;

/**
 * @author chenwei
 *
 */
public class EmailProtoCase {
  public static void main(String[] args) throws Exception {

    EmailProto.Mail.Builder mailBuilder = EmailProto.Mail.newBuilder();
    mailBuilder.setSubject("test-subject-1");
    mailBuilder.setBody("test-body-1");
    mailBuilder.setTo("test-to-1");
    EmailProto.Attachment.Builder attachmentBuilder = EmailProto.Attachment
        .newBuilder();
    attachmentBuilder.setName("test-attachment-name-1");
    attachmentBuilder.setOriginalFilename("test-attachment-originalFilename-1");
    attachmentBuilder.setContentType("application/vnd.ms-excel");
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("protobuf/proto.txt")) {
      attachmentBuilder.setContent(ByteString.readFrom(is));
    }
    mailBuilder.addAttachments(attachmentBuilder);
    EmailProto.Mail mail = mailBuilder.build();

    byte[] byteArray = null;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
      mail.writeTo(bos);
      byteArray = bos.toByteArray();
    }

    try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        OutputStream os = new FileOutputStream(
            new File("C:/mine/work-core-temp/proto.txt"));) {
      EmailProto.Mail _mail = EmailProto.Mail.parseFrom(bis);
      System.out.println(_mail.getSubject());
      System.out.println(_mail.getBody());
      System.out.println(_mail.getTo());
      for (EmailProto.Attachment attachment : _mail.getAttachmentsList()) {
        System.out.println(attachment.getName());
        System.out.println(attachment.getOriginalFilename());
        System.out.println(attachment.getContentType());
        attachment.getContent().writeTo(os);
      }
    }
  }
}
