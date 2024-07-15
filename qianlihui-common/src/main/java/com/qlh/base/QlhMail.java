package com.qlh.base;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class QlhMail {

    private Config config;

    public void send(Content content) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", config.smtp);//设置邮件服务器主机名
        props.put("mail.smtp.auth", config.smtpAuth);//发送服务器需要身份验证
        props.setProperty("mail.transport.protocol", "smtp"); //协议
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        Session session = Session.getDefaultInstance(props);//设置环境信息
        Message msg = new MimeMessage(session);
        QlhException.runtime(() -> {
            msg.setFrom(new InternetAddress(config.from));
            InternetAddress[] addresses = content.to.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(e -> {
                        try {
                            return new InternetAddress(e);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }).collect(Collectors.toList()).toArray(new InternetAddress[0]);

            msg.setRecipients(MimeMessage.RecipientType.TO, addresses);

            msg.setSubject(content.subject);

            msg.addHeader("X-Priority", "3");
            msg.addHeader("X-MSMail-Priority", "Normal");
            msg.addHeader("X-Mailer", "Microsoft Outlook Express 6.00.2900.2869");
            msg.addHeader("X-MimeOLE", "Produced By Microsoft MimeOLE V6.00.2900.2869");
            msg.addHeader("ReturnReceipt", "1");

            MimeMultipart mm = new MimeMultipart();
            content.attachments.forEach(file -> {
                QlhException.runtime(() -> {
                    MimeBodyPart attachment = new MimeBodyPart();
                    DataHandler dh2 = new DataHandler(new FileDataSource(file.getAbsolutePath()));
                    attachment.setDataHandler(dh2);
                    attachment.setFileName(MimeUtility.encodeText(dh2.getName()));
                    mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
                });
            });

            MimeBodyPart text = new MimeBodyPart();
            text.setContent(content.content, "text/html;charset=utf-8");
            mm.addBodyPart(text);
            mm.setSubType("mixed");

            msg.setContent(mm);
            Transport trans = session.getTransport();
            try {
                trans.connect(config.user, config.password); // 邮件的账号密码
                trans.sendMessage(msg, msg.getAllRecipients());
            } finally {
                trans.close();
            }
        });
    }

    @Data
    public static class Content {

        private String subject;
        private String content;
        private List<String> to = new ArrayList<>(0);
        private List<File> attachments = new ArrayList<>(0);

        public Content setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Content setContent(String content) {
            this.content = content;
            return this;
        }

        public Content addTo(String address) {
            to.add(address);
            return this;
        }

        public Content addTo(List<String> addresses) {
            to.addAll(addresses);
            return this;
        }

        public Content addAttachment(File attachment) {
            attachments.add(attachment);
            return this;
        }

        public Content addAttachment(List<File> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }
    }

    @Data
    public static class Config {
        private String smtp;
        private String smtpAuth;
        private String from;
        private String user;
        private String password;

        public QlhMail build() {
            QlhMail mail = new QlhMail();
            mail.config = this;
            return mail;
        }

        public Config setSmtp(String smtp) {
            this.smtp = smtp;
            return this;
        }

        public Config setSmtpAuth(String smtpAuth) {
            this.smtpAuth = smtpAuth;
            return this;
        }

        public Config setFrom(String from) {
            this.from = from;
            return this;
        }

        public Config setUser(String user) {
            this.user = user;
            return this;
        }

        public Config setPassword(String password) {
            this.password = password;
            return this;
        }
    }
}
