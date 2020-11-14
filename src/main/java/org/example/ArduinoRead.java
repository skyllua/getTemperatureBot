package org.example;

import com.fazecast.jSerialComm.SerialPort;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;


public class ArduinoRead extends Bot implements Runnable {
    private SerialPort comPort;
    private static String temperature;
    private static String humidity;
    private static String heatIndex;

    public ArduinoRead() {
        comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
    }

    public static String getTemperature() {
        return temperature;
    }

    public static String getHumidity() {
        return humidity;
    }

    public static String getHeatIndex() {
        return heatIndex;
    }

    public synchronized void close() {
        if (comPort != null) {
            comPort.closePort();
        }
    }

    public void run() {
        InputStream in = comPort.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (true) {
            try {
                String inputLine = reader.readLine();
                if (inputLine.contains("Temperature")) {
                    temperature = inputLine.substring(inputLine.indexOf("<Temperature>") + "<Temperature>".length(), inputLine.indexOf("</"));
                }
                if (inputLine.contains("Humidity")) {
                    humidity = inputLine.substring(inputLine.indexOf("<Humidity>") + "<Humidity>".length(), inputLine.indexOf("</"));
                }
                if (inputLine.contains("HeatIndex")) {
                    heatIndex = inputLine.substring(inputLine.indexOf("<HeatIndex>") + "<HeatIndex>".length(), inputLine.indexOf("</"));
                }

                /**
                 * NOTIFICATION
                 */

                if (temperature != null && heatIndex != null) {
                    if ((Float.parseFloat(temperature) > WARNING_TEMPERATURE) && (System.currentTimeMillis() - LAST_TIME_NOTIFICATION) > INTERVAL) {
                        for (Map.Entry<String, Boolean> pair : mainChatsID.entrySet()) {
                            if (pair.getValue()) {
                                String warning = "⚠️";
                                String critical = "❗️";
                                String fatal = "❌️";
                                String ico = warning;

                                if ((Float.parseFloat(temperature) > CRITICAL_TEMPERATURE || Float.parseFloat(heatIndex) > CRITICAL_TEMPERATURE))
                                    ico = critical;
                                if ((Float.parseFloat(temperature) > FATAL_TEMPERATURE || Float.parseFloat(heatIndex) > FATAL_TEMPERATURE))
                                    ico = fatal;

//                                System.out.println("-- -- sent msg to " + pair.getKey());

                                    SendMessage message = new SendMessage()
                                            .enableMarkdown(true)
                                            .setText(ico + " *Humidity: *" + ArduinoRead.getHumidity() + " %\n" +
                                                    ico + " *Temperature: *" + ArduinoRead.getTemperature() + " °C\n" +
                                                    ico + " *Heat Index: *" + ArduinoRead.getHeatIndex() + " °C")
                                            .setChatId(pair.getKey());
                                try {
                                    execute(message);
                                } catch (TelegramApiException e) {
                                    System.out.println("-- can't sand message to user: " + pair.getKey());
                                }

                            }
                        }
                        System.out.println(" *Humidity: *" + ArduinoRead.getHumidity() + " %\n" +
                                " *Temperature: *" + ArduinoRead.getTemperature() + " °C\n" +
                                " *Heat Index: *" + ArduinoRead.getHeatIndex() + " °C");
                        LAST_TIME_NOTIFICATION = System.currentTimeMillis();
//                            Bot.logger.warn(" *Humidity: *" + ArduinoRead.getHumidity() + " %\n" +
//                                    " *Temperature: *" + ArduinoRead.getTemperature() + " °C\n" +
//                                    " *Heat Index: *" + ArduinoRead.getHeatIndex() + " °C");
                    }
                }

            } catch (Exception e) {
                if (PRINT_STACK_TRACE) e.printStackTrace();
                else System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
}