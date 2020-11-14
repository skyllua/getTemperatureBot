package org.example;

import com.fazecast.jSerialComm.SerialPort;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.io.*;
import java.util.Properties;

public class Runner extends Bot {

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(fileProperties));
            WARNING_TEMPERATURE = Float.parseFloat(properties.getProperty("WARNING_TEMPERATURE"));
            CRITICAL_TEMPERATURE = Float.parseFloat(properties.getProperty("CRITICAL_TEMPERATURE"));
            FATAL_TEMPERATURE = Float.parseFloat(properties.getProperty("FATAL_TEMPERATURE"));
            INTERVAL = Long.parseLong(properties.getProperty("INTERVAL"));
            COMPORT = properties.getProperty("COMPORT");
            PRINT_STACK_TRACE = Boolean.parseBoolean(properties.getProperty("PRINT_STACK_TRACE"));
            System.out.println("=========================================");
            System.out.println("-- Properties is loaded");
            System.out.println("=========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileReader fr = new FileReader(fileChatList);
            BufferedReader reader = new BufferedReader(fr);
            String line = "";

            while ((line = reader.readLine()) != null) {
                mainChatsID.put(line, true);
            }

            reader.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ApiContextInitializer.init();
        TelegramBotsApi bot = new TelegramBotsApi();
        try {
            bot.registerBot(new Bot());
            System.out.println("-- Bot is started");
            System.out.println("=========================================");
//            Bot.logger.info("-- Bot is started");

            ArduinoRead arduinoRead = new ArduinoRead();
            Thread arduino = new Thread(arduinoRead);
            arduino.start();

            System.out.println("-- Start reading RX TX");
            System.out.println("=========================================");
//            Bot.logger.info("=========================================");
//            Bot.logger.info("-- Start reading RX TX");

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

}
