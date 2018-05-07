package ru.tradition.lockeymobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

public class FeedbackActivity extends AppCompatActivity {

    //Объявляем используемые переменные:
    private EditText receiver_Email;
    private EditText message_theme;
    private EditText message_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //to add up button
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        }

        toolbar.setTitle(R.string.feedback_activity_title);

        //Инициализируем переменные, привязываем их к нашим объектам:
        receiver_Email = (EditText) findViewById(R.id.receiver_Email);
        message_theme = (EditText) findViewById(R.id.message_theme);
        message_text = (EditText) findViewById(R.id.message_text);

    }

    //Стандартный метод для реализации меню приложения:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    //Описываем функционал кнопок меню:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //В случае кнопки "Очистить", для всех элементов EditText настраиваем пустые строки для ввода,
            //то есть очищаем все поля от введенного текста:
            case R.id.menu_clear:
                receiver_Email.setText("");
                message_text.setText("");
                message_theme.setText("");
                break;

            //Для кнопки "Отправить" создаем 3 строковых объекта для контакта, темы и текста сообщения
            case R.id.menu_send:
                String contact = receiver_Email.getText().toString();
                String subject = message_theme.getText().toString();
                String message = message_text.getText().toString();

                //С помощью намерения Intent вызываем стандартный пакет приложения для отправки e-mail,
                //передаем в него данные с полей "адрес", "тема" и "текст сообщения",
                //заполняя ими соответствующие поля стандартного e-mail приложения
                //и запускаем процесс перехода с нашего приложения в стандартную программу для обмена e-mail:
                Intent emailIntent = getPackageManager().getLaunchIntentForPackage("com.android.email");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{contact});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(emailIntent);
                break;
        }
        return true;
    }


    final String username = "username@gmail.com";
    final String password = "password";

    private Session createSessionObject() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from-email@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("to-email@gmail.com"));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler,"
                    + "\n\n No spam to my email, please!");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            String file = "path of file to be attached";
            String fileName = "attachmentName"
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
