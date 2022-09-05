package me.felixnaumann.fwebserver.cli;

import me.felixnaumann.fwebserver.model.ConsoleCommand;
import me.felixnaumann.fwebserver.server.Server;
import me.felixnaumann.fwebserver.utils.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Scanner;

public class ConsoleThread implements Runnable {

    public static String currdir = "";

    public static void runThread() {
        new Thread(new ConsoleThread()).start();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!Server.configloaded) {
                while (!Server.configloaded) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {

                    }
                }
                try {
                    currdir = (new File(Server.config.getWwwroot())).getCanonicalPath();
                } catch (IOException ignored) {


                }
            }
            System.out.print("> ");
            Scanner in = new Scanner(System.in);

            String inputString = in.nextLine();
            ConsoleCommand command = ConsoleCommand.buildCommand(inputString);

            Method commandMethod = ReflectionUtils.getCliMethod(command.getCommand());

            try {
                if (commandMethod != null) {
                    if (!commandMethod.getAnnotation(CliCommandName.class).implemented())
                        System.err.println("command not implemented yet");
                    else commandMethod.invoke(null, command);
                } else {
                    System.err.println("command not found");
                }
            }
            catch (Exception e) {
                System.err.printf("An error occured while running command %s. Please check the stack trace.\n", command.getCommand());
                e.printStackTrace();
            }
        }
    }
}
