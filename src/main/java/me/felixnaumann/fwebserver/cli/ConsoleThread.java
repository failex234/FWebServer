package me.felixnaumann.fwebserver.cli;

import me.felixnaumann.fwebserver.model.ConsoleCommand;
import me.felixnaumann.fwebserver.server.Server;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ConsoleThread implements Runnable {

    private String currdir = "";

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

            switch (command.getCommand()) {
                case "version" -> System.out.printf("%s version %s\n", Server.SERVERNAME, Server.SERVERVERSION);
                case "help" -> System.out.println("Not implemented yet");
                case "ls" -> {
                    if (command.hasArgs()) {

                    }
                    File[] filelist = (new File(currdir).listFiles());

                    if (currdir.endsWith(Server.config.getWwwroot())) {
                        System.out.println("Directory listing for /");
                    } else {
                        try {
                            System.out.printf("Directory listing for %s:\n", currdir.replace(new File(Server.config.getWwwroot()).getCanonicalPath(), "").replace("\\", "/"));
                        } catch (IOException e) {

                        }
                    }

                    System.out.println("----------------------------");

                    for (File file : filelist) {
                        if (file.isFile()) continue;
                        System.out.printf("[%s]\n", file.getName());
                    }

                    for (File file : filelist) {
                        if (file.isDirectory()) continue;
                        System.out.println(file.getName());
                    }

                    System.out.println("----------------------------");
                }
                case "cd" -> {
                    File newdir = new File(currdir + "/" + command.getNthArg(0));
                    if (!newdir.exists()) {
                        System.out.println("no such file or directory");
                    } else if (newdir.isFile()) {
                        System.out.println("you can't cd to a file");
                    } else {
                        try {
                            if (newdir.getCanonicalPath().startsWith(new File(Server.config.getWwwroot()).getAbsolutePath())) {
                                currdir = newdir.getCanonicalPath();
                            } else {
                                System.out.println("no such file or directory");
                            }
                        } catch (IOException e) {
                            System.out.println("no such file or directory");
                        }
                    }
                }
                case "stop" -> Server.getInstance().stopServer();
                default -> System.out.println("Command not found");
            }
        }
    }
}
