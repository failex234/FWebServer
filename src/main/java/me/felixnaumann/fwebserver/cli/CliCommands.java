package me.felixnaumann.fwebserver.cli;

import me.felixnaumann.fwebserver.annotations.CliCommandName;
import me.felixnaumann.fwebserver.model.ConsoleCommand;
import me.felixnaumann.fwebserver.server.Server;
import me.felixnaumann.fwebserver.utils.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Contains all commands that can be run from the cli
 * interface.
 */
public class CliCommands {

    @CliCommandName(name = "version", description = "Show version information")
    public static void versionCommand(ConsoleCommand command) {
        System.out.printf("%s version %s\n", Server.getInstance().NAME, Server.getInstance().VERSION);
    }

    @CliCommandName(name = "help", description = "List available commands")
    public static void helpCommand(ConsoleCommand command) {
        Method[] commands = ReflectionUtils.getCliCommandMethods();
        System.out.println("This is the FWebServer CLI Interface");
        System.out.println("-- the following commands are available --");

        for (Method cmd : commands) {
            CliCommandName metadata = cmd.getAnnotation(CliCommandName.class);
            System.out.printf("- %s%s\n", metadata.name(), !metadata.description().isEmpty() ? " | " + cmd.getAnnotation(CliCommandName.class).description() : "");
        }
        System.out.println("----");

    }

    @CliCommandName(name = "pwd", description = "Print current working directory")
    public static void pwdCommand(ConsoleCommand command) throws IOException {
        if (ConsoleThread.currdir.endsWith(Server.config.getWwwroot())) {
            System.out.println("/");
        } else {
            System.out.println(ConsoleThread.currdir.replace(new File(Server.config.getWwwroot()).getCanonicalPath(), "").replace("\\", "/"));
        }
    }

    @CliCommandName(name = "ls", description = "List files in the current working directory")
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

    @CliCommandName(name = "cd", description = "Change the working directory")
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

    @CliCommandName(name = "stop", description = "Gracefully stop the server")
    public static void stopCommand(ConsoleCommand command) {
        Server.getInstance().stopServer();
    }

    @CliCommandName(name = "status", description = "Show current server status")
    public static void statusCommand(ConsoleCommand command) {
        System.out.printf("%s running on port :%d\n", Server.getInstance().NAME, Server.getInstance().PORT);
        System.out.printf("www-root: %s\nconfig path: %s\nlog path: %s\n",
                new File(Server.config.getWwwroot()).getAbsolutePath(),
                Server.config.getConfigFile().getAbsolutePath(),
                new File(Server.config.getLogfolder()).getAbsolutePath());
    }
}
