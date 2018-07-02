package com.cnilcb.main;

import com.pi4j.io.gpio.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {


    //Pins
    private static final Pin SCL_PIN = RaspiPin.GPIO_04;
    private static final Pin SDA_PIN = RaspiPin.GPIO_05;
    //private static final Pin LO_MINUS_PIN = RaspiPin.GPIO_27;
    //private static final Pin LO_PLUS_PIN = RaspiPin.GPIO_28;

    //How many seconds before the application dumps the recording to the file.
    private static final short BIG_CYCLE_LENGTH = 3600; //seconds
    //How many seconds before the application dumps the fresh data into the buffer file
    private static final short SMALL_CYCLE_LENGTH = 4; //seconds
    //Read frequency
    private static final short FREQUENCY = 65; //Hz
    //Initial read resolution
    private static final short INITIAL_RESOLUTION = 15; //bits
    private static final double INITIAL_MAXIMUM = Math.pow(2,INITIAL_RESOLUTION + 1) - 1;
    //Target resolution
    private static final short TARGET_RESOLUTION = 11; //bits
    private static final double TARGET_MAXIMUM = Math.pow(2, TARGET_RESOLUTION);



    private static GpioController gpio;
    private static GpioPinDigitalOutput scl;
    private static GpioPinDigitalMultipurpose sda;



    public static void main(String[] args) throws InterruptedException, IOException {
        gpio = GpioFactory.getInstance();

        //Instance init
        scl = gpio.provisionDigitalOutputPin(SCL_PIN, "SCL");
        sda = gpio.provisionDigitalMultipurposePin(SDA_PIN, "SDA", PinMode.DIGITAL_OUTPUT);

        //TODO implement 'leads-off'
        //GpioPinDigitalInput loMinus = gpio.provisionDigitalInputPin(LO_MINUS_PIN);
        //GpioPinDigitalInput loPlus = gpio.provisionDigitalInputPin(LO_PLUS_PIN);

        //Prepare ADS1110 chip (AD converter)
        setup();

        //Reset everything on exit
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            gpio.shutdown();
            scl.setState(PinState.HIGH);
            sda.setMode(PinMode.DIGITAL_OUTPUT);
            sda.setState(PinState.HIGH);
        }));

        ArrayList<Integer> buffer = new ArrayList<>();
        ArrayList<Integer> buf = new ArrayList<>();
        long iteration = 0;
        while(true) {
            for(int i=1; i<=FREQUENCY*SMALL_CYCLE_LENGTH; i++) {
                int read = readByte();
                int prc = (int) (((double) read / INITIAL_MAXIMUM) * TARGET_MAXIMUM);
                buffer.add(prc);
                buf.add(prc);
                Thread.sleep((long) 1000 / FREQUENCY);
            }

            if(iteration % BIG_CYCLE_LENGTH == 0){
                File file = new File("record" + iteration/ BIG_CYCLE_LENGTH + ".hp");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                for(Integer x : buffer)
                    writer.write(x.toString() + "\n");
                writer.flush();
                writer.close();
                buffer.clear();
                buf.clear();
            }else{
                //Dump everything to a buffer file for every small cycle
                // so it can be used by other software for processing.
                File file = new File("buf.txt");
                if(file.exists()) file.delete();
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                //Write an end tag
                writer.write("END\n");
                for(Integer x : buf)
                    writer.write(x.toString() + "\n");
                writer.flush();
                writer.close();
                buf.clear();
            }
            iteration+=SMALL_CYCLE_LENGTH;

        }

    }


    //Read the byte and check for acknowledge
    private static int readByte(){
        int result;

        //Frame 1
        if(!sda.getMode().equals(PinMode.DIGITAL_OUTPUT))
            sda.setMode(PinMode.DIGITAL_OUTPUT);
        scl.setState(PinState.HIGH);
        sda.setState(PinState.LOW); //Start by master
        sendAddress(true);

        if(!ack()) return -1;
        scl.setState(PinState.HIGH);

        //Frame 2
        sda.setMode(PinMode.DIGITAL_INPUT);
        result = 0;
        for(int i=1; i<=8; i++){
            result <<= 1;
            if(nextClk())
                result |= 0x1;
        }
        ack(true);

        //Frame 3
        for(int i=1; i<=8; i++){
            result <<=1;
            if(nextClk())
                result |= 0x1;
        }
        ack(true);

        //Frame 4 (partial)
        //if(!nextClk()) return -1; //new data check

        sda.setMode(PinMode.DIGITAL_OUTPUT);
        sda.setState(PinState.HIGH); //Stop by master
        return result;
    }

    private static void nextClk(boolean high){
        if(!sda.getMode().equals(PinMode.DIGITAL_OUTPUT))
            sda.setMode(PinMode.DIGITAL_OUTPUT);
        scl.setState(PinState.LOW);
        if(high) sda.setState(PinState.HIGH);
        else sda.setState(PinState.LOW);
        scl.setState(PinState.HIGH);
    }

    private static boolean nextClk(){
        if(!sda.getMode().equals(PinMode.DIGITAL_INPUT))
            sda.setMode(PinMode.DIGITAL_INPUT);
        scl.setState(PinState.LOW);
        scl.setState(PinState.HIGH);
        return sda.isHigh();
    }

    private static boolean ack(){
        scl.setState(PinState.LOW);
        sda.setMode(PinMode.DIGITAL_INPUT);
        if(sda.isHigh()){
            System.out.println("NON_ACK");
            return false;
        }
        return true;
    }

    private static void ack(boolean val){
        nextClk(!val);
    }

    //I2C address of the AD chip
    private static void sendAddress(boolean read){
        nextClk(true);  // 1
        nextClk(false); // 0
        nextClk(false); // 0
        nextClk(true);  // 1
        nextClk(false); // 0
        nextClk(false); // 0
        nextClk(false); // 0
        nextClk(read);       // R/W
    }

    //Setup as instructed in the ASD1110 documentation
    private static void setup(){
        System.out.println("Setting up chip");

        //Frame 1
        sda.setMode(PinMode.DIGITAL_OUTPUT);
        scl.setState(PinState.HIGH);
        sda.setState(PinState.LOW); //Start by master
        sendAddress(false);
        if(!ack()) return;
        scl.setState(PinState.HIGH);

        //Frame 2
        nextClk(false); //ST/DRDY
        nextClk(false); //0 - Reserved
        nextClk(false); //0 - Reserved
        nextClk(false); //0 - SC
        nextClk(false); //0 - DR1
        nextClk(true);  //1 - DR0
        nextClk(false); //0 - PGA1
        nextClk(false); //0 - PGA0

        if(!ack()) return;
        scl.setState(PinState.HIGH);

        sda.setMode(PinMode.DIGITAL_OUTPUT);
        sda.setState(PinState.HIGH); //Stop by master
        System.out.println("Done");
    }
}