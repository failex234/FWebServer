package me.felixnaumann.fwebserver.model;

import java.util.ArrayList;

public class ConsoleCommand {
    private String command;
    private String[] args;

    public static ConsoleCommand buildCommand(String cmdline) {
        boolean inquotes = false;
        boolean readingCommand = true;
        char quotetype = '"';

        StringBuilder command = new StringBuilder();
        StringBuilder currarg = new StringBuilder();
        ArrayList<String> args = new ArrayList<>();

        for (int i = 0; i < cmdline.length(); i++) {
            char currchar = cmdline.charAt(i);

            if (currchar == ' ' && !inquotes) {
                if (readingCommand) {
                    readingCommand = false;
                } else {
                    args.add(currarg.toString());
                    currarg.setLength(0);
                }
            } else if (currchar == '"' || currchar == '\'') {
                if (!inquotes) {
                    inquotes = true;
                    quotetype = currchar;
                } else if (quotetype == currchar) {
                    inquotes = false;
                }
            } else if (!readingCommand && i == cmdline.length() - 1){
                currarg.append(currchar);
                args.add(currarg.toString());
            } else {
                if (readingCommand) {
                    command.append(currchar);
                } else {
                    currarg.append(currchar);
                }
            }
        }

        return new ConsoleCommand(command.toString(), args.toArray(new String[args.size()]));

    }

    public ConsoleCommand(String cmdname, String... args) {
        this.command = cmdname;
        this.args = args;
    }

    public String getCommand() {
        return this.command;
    }

    public String getNthArg(int idx) {
        if (idx >= args.length) {
            return "";
        }
        return args[idx];
    }

    public boolean hasArgs() {
        return args.length > 0;
    }
}
