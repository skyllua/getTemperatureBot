package org.example;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {
    public static File fileChatList = new File("src/main/resources/ChatList.txt");
    public static File fileProperties = new File("src/main/resources/settings.properties");
    public static float WARNING_TEMPERATURE = 30.00f;
    public static float CRITICAL_TEMPERATURE = 40.00f;
    public static float FATAL_TEMPERATURE = 50.00f;
    public static long INTERVAL = 5 * 60 * 1000;
    public static long LAST_TIME_NOTIFICATION = 0;
    public static String COMPORT = "COM4";
    public static boolean PRINT_STACK_TRACE = true;
    public static Map<String, Boolean> mainChatsID = new HashMap<String, Boolean>();

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (message.getText().startsWith("/info")) {
            SendMessage msg = new SendMessage()
                    .enableMarkdown(true)
                    .setChatId(message.getChatId())
                    .setText("*Humidity: *" + ArduinoRead.getHumidity() + " %" +
                            "\n*Temperature: *" + ArduinoRead.getTemperature() + " °C" +
                            "\n*Heat Index: *" + ArduinoRead.getHeatIndex() + " °C");

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        if (message.getText().startsWith("/unmute")) {
            mainChatsID.put(message.getChatId().toString(), true);
        }

        if (message.getText().startsWith("/mute")) {
            mainChatsID.put(message.getChatId().toString(), false);
        }

        if (message.getText().startsWith("/start")) {
            DeleteMessage deleteMessage = new DeleteMessage()
                    .setMessageId(message.getMessageId())
                    .setChatId(message.getChatId().toString());

            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            if (!mainChatsID.containsKey(message.getChatId().toString())) {
                mainChatsID.put(message.getChatId().toString(), true);
                try {
                    FileWriter fw = new FileWriter(fileChatList);
                    for (String chat : mainChatsID.keySet()) {
                        fw.write(chat + "\n");
                    }

                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SendMessage msg = new SendMessage()
                        .enableMarkdown(true)
                        .setChatId(message.getChatId())
                        .setText("*Hi! I'll be notificate you about high temperature level in server room!\n\nNow:\n*" +
                                "*Humidity: *" + ArduinoRead.getHumidity() + " %" +
                                "\n*Temperature: *" + ArduinoRead.getTemperature() + " °C" +
                                "\n*Heat Index: *" + ArduinoRead.getHeatIndex() + " °C");

                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(message.getChatId() + " :: " + message.getText());
    }

    public String getBotUsername() {
        return "UKCTemperatureBot";
    }

    public String getBotToken() {
        return "1176094835:AAEiLZ4jZQ3Gy0pDkwz8Z5MzSvOnBL_qPwk";
    }
}