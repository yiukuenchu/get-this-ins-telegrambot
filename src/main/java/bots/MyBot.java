package bots;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.interceptor.ErrorInterceptor;
import me.postaddict.instagram.scraper.model.*;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MyBot extends TelegramLongPollingBot {

    private String token = "6235652896:AAG2QeGrZ4tzRnE1Ck0P8Dgwj_l9m6nmokE";
    private String botUsername = "get_this_ins_bot";
    private Instagram instagram;

    public MyBot() {
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if (isInstagramPostUrl(messageText)) {
                if (messageText != null) {
                    String extractedPostUrl = extractInstagramPostUrl(messageText);
                    String photoUrl = getPhotoUrl(extractedPostUrl);

                    try {
                        URLConnection connection = new URL(photoUrl).openConnection();
                        connection.connect();
                        InputStream is = connection.getInputStream();
                        URL realPhotoUrl = connection.getURL();
                        sendMessags(update, "Here are the photos. Enjoy!");
                        sendPhotos(update, realPhotoUrl.toString());
                        is.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }
            } else {
                // Reply with an error message for invalid Instagram post URL
                sendMessags(update, "Invalid Instagram post URL!");
            }
        }
    }

    private void sendPhotos(Update update, String photoUrl) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .photo(new InputFile(photoUrl))
                    .build();
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessags(Update update, String message) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text(message)
                    .build();
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isInstagramPostUrl(String url) {
        // Check if the text matches the pattern of an Instagram post URL
        return url.matches("https?:\\/\\/(www\\.)?instagram\\.com\\/p\\/.*");
    }

    // Extract the URL of the Instagram post from the post link
    private String extractInstagramPostUrl(String url) {
        // Find the index of '/p' in the URL
        int pIndex = url.indexOf("/p/");

        // Find the index of the second '/' after '/p'
        int secondSlashIndex = url.indexOf('/', pIndex + 3);

        String extractedUrl = (secondSlashIndex != -1) ? url.substring(0, secondSlashIndex + 1) : url;
        return extractedUrl;
    }

    // Get the URL of the photo in the Instagram post
    private String getPhotoUrl(String url) {
        return url + "media/?size=l";
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

}
