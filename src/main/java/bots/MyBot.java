package bots;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.postaddict.instagram.scraper.Instagram;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MyBot extends TelegramLongPollingBot {

    private String token = "YOUR_BOT_TOKEN";
    private String botUsername = "YOUR_BOT_USERNAME";
    private Instagram instagram;

    public MyBot() {
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if (isInstagramPostUrl(messageText)) {
                if (!messageText.equals("")) {
                    String extractedPostUrl = extractInstagramPostUrl(messageText);
                    String jsonUrl = getJsonUrl(extractedPostUrl);

                    try {
                        ArrayList<String> photoUrls = extractPhotoUrls(jsonUrl);

                        for (String photoUrl : photoUrls) {
                            sendPhotos(update, photoUrl);
                        }
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

    // Send photos to the user
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

    // Send a message to the user
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

    // Validate the Instagram post URL
    private boolean isInstagramPostUrl(String url) {
        // Check if the text matches the pattern of an Instagram post URL
        return url.matches("https?:\\/\\/(www\\.)?instagram\\.com\\/p\\/.*");
    }

    // Extract the URL of the Instagram post from the post link
    // Return the URL with short code
    private String extractInstagramPostUrl(String url) {
        // Find the index of '/p' in the URL
        int pIndex = url.indexOf("/p/");

        // Find the index of the second '/' after '/p'
        int secondSlashIndex = url.indexOf('/', pIndex + 3);

        String extractedUrl = (secondSlashIndex != -1) ? url.substring(0, secondSlashIndex + 1) : url;
        return extractedUrl;
    }

    // Get the URL of the photo in the Instagram post
    // Not used， can only get the first photo
    private String getPhotoUrl(String url) {
        return url + "media/?size=l";
    }

    // Get the URL of the json data
    private String getJsonUrl(String url) {
        return url + "?__a=1&__d=dis";
    }

    // Extract photo URLs from the json data
    private ArrayList<String> extractPhotoUrls(String jsonUrl) throws IOException {
        URL url = new URL(jsonUrl);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

        // Read the JSON data
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();

        // Parse the JSON data using Gson from reader
        JsonObject jsonObject = new Gson().fromJson(jsonBuilder.toString(), JsonObject.class);
        JsonArray jsonArray = jsonObject.get("graphql").getAsJsonObject().get("shortcode_media").getAsJsonObject().get("edge_sidecar_to_children").getAsJsonObject().get("edges").getAsJsonArray();

        ArrayList<String> urls = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            String display_url = jsonElement.getAsJsonObject().get("node").getAsJsonObject().get("display_url").getAsString();
            urls.add(display_url);
        }

        return urls;
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
