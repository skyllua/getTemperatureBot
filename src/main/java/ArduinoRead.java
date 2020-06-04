import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.Enumeration;
import java.util.Map;


public class ArduinoRead extends Bot implements SerialPortEventListener {
    SerialPort serialPort;
    private static String temperature;
    private static String humidity;
    private static String heatIndex;

    public static String getTemperature() {
        return temperature;
    }

    public static String getHumidity() {
        return humidity;
    }

    public static String getHeatIndex() {
        return heatIndex;
    }

    /** The port we're normally going to use. */
    private static final String PORT_NAMES[] = {"COM4"};
    private  BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    public void initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }


    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = null;
                if (input.ready()) {
                    inputLine = input.readLine();
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
                        if ((Float.parseFloat(temperature) > WARNING_TEMPERATURE || Float.parseFloat(heatIndex) > WARNING_TEMPERATURE) && (System.currentTimeMillis() - LAST_TIME_NOTIFICATION) > INTERVAL) {
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


                                    SendMessage message = new SendMessage()
                                            .enableMarkdown(true)
                                            .setText(ico + " *Humidity: *" + ArduinoRead.getHumidity() + " %\n" +
                                                    ico + " *Temperature: *" + ArduinoRead.getTemperature() + " °C\n" +
                                                    ico + " *Heat Index: *" + ArduinoRead.getHeatIndex() + " °C")
                                            .setChatId(pair.getKey());
                                    execute(message);

                                }
                            }
                            System.out.println(" *Humidity: *" + ArduinoRead.getHumidity() + " %\n" +
                                    " *Temperature: *" + ArduinoRead.getTemperature() + " °C\n" +
                                    " *Heat Index: *" + ArduinoRead.getHeatIndex() + " °C");
                            LAST_TIME_NOTIFICATION = System.currentTimeMillis();
                        }
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