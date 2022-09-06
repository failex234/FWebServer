package me.felixnaumann.fwebserver.cli;

import me.felixnaumann.fwebserver.annotations.CliCommandName;
import me.felixnaumann.fwebserver.model.ConsoleCommand;
import me.felixnaumann.fwebserver.server.Server;

import java.io.File;
import java.io.IOException;

/**
 * Contains all commands that can be run from the cli
 * interface.
 */
public class CliCommands {

    @CliCommandName("version")
    public static void versionCommand(ConsoleCommand command) {
        System.out.printf("%s version %s\n", Server.NAME, Server.VERSION);
    }

    @CliCommandName(value = "help", implemented = false)
    public static void helpCommand(ConsoleCommand command) {

    }

    @CliCommandName("ls")
    public static void lsCommand(ConsoleCommand command) {
        if (command.hasArgs()) {

        }
        File[] filelist = (new File(ConsoleThread.currdir).listFiles());

        if (ConsoleThread.currdir.endsWith(Server.config.getWwwroot())) {
            System.out.println("Directory listing for /");
        } else {
            try {
                System.out.printf("Directory listing for %s:\n", ConsoleThread.currdir.replace(new File(Server.config.getWwwroot()).getCanonicalPath(), "").replace("\\", "/"));
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

    @CliCommandName("cd")
    public static void cdCommand(ConsoleCommand command) {
        File newdir = new File(ConsoleThread.currdir + "/" + command.getNthArg(0));
        if (!newdir.exists()) {
            System.out.println("no such file or directory");
        } else if (newdir.isFile()) {
            System.out.println("you can't cd to a file");
        } else {
            try {
                if (newdir.getCanonicalPath().startsWith(new File(Server.config.getWwwroot()).getAbsolutePath())) {
                    ConsoleThread.currdir = newdir.getCanonicalPath();
                } else {
                    System.out.println("no such file or directory");
                }
            } catch (IOException e) {
                System.out.println("no such file or directory");
            }
        }
    }

    @CliCommandName("stop")
    public static void stopCommand(ConsoleCommand command) {
        Server.getInstance().stopServer();
    }

    @CliCommandName("status")
    public static void statusCommand(ConsoleCommand command) {
        System.out.printf("%s running on port :%d\n", Server.NAME, Server.port);
        System.out.printf("www-root: %s\nconfig path: %s\nlog path: %s\n",
                new File(Server.config.getWwwroot()).getAbsolutePath(),
                Server.config.getConfigFile().getAbsolutePath(),
                new File(Server.config.getLogfolder()).getAbsolutePath());
    }
}
