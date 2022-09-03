package me.felixnaumann.fwebserver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.felixnaumann.fwebserver.BuiltIn;
import me.felixnaumann.fwebserver.ClientHeader;
import me.felixnaumann.fwebserver.server.Server;
import me.felixnaumann.fwebserver.ServerConfig;
import org.python.util.PythonInterpreter;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

public class FileUtils {
    public static ServerConfig getServerConfig() {
        Gson gsoninstance = null;
        ServerConfig config;
        StringBuilder json = new StringBuilder();
        gsoninstance = new GsonBuilder().setPrettyPrinting().create();
        File configfile = new File("server.json");

        if (configfile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(configfile));
                String line;
                while ((line = br.readLine()) != null) {
                    json.append(line);
                }
                br.close();
                config = gsoninstance.fromJson(json.toString(), ServerConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                config = new ServerConfig();
                config.createNewConfig();
            }
        } else {
            config = new ServerConfig();
            config.createNewConfig();
            String jsonout = gsoninstance.toJson(config);

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configfile));
                bw.write(jsonout);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return config;
    }

    public static void createWwwRoot(String wwwroot, ServerConfig config) {
        File webroot = new File(wwwroot);
        if (!webroot.exists()) {
            LogUtils.Consolelogf("The wwwroot path \"%s\" doesn't exist. creating it for you...\n", webroot.getAbsolutePath());
            webroot.mkdir();
            File aboutfile = new File(wwwroot + "/about2.html");
            File indexfile = new File(wwwroot + "/index.html");
            File dynfile = new File(wwwroot + "/about2dyn.pyfs");
            String filecontents = new String(Base64.getDecoder().decode(BuiltIn.about2));
            String indexcontents = new String(Base64.getDecoder().decode(BuiltIn.index));
            String dynfilecontents = new String(Base64.getDecoder().decode(BuiltIn.dyn));

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(aboutfile));
                bw.write(filecontents);
                bw.close();

                bw = new BufferedWriter(new FileWriter(indexfile));
                bw.write(indexcontents);
                bw.close();

                bw = new BufferedWriter(new FileWriter(dynfile));
                bw.write(dynfilecontents);
                bw.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            //Check if config is null
            String status = config.isConfigCorrupt();
            if (!status.equals("no")) {
                System.err.println(status);
                System.exit(1);
            }
        }
    }

    /**
     * Converts the number of bytes into the correct unit
     * @param size the number of bytes
     * @return the correctly formatted file size
     */
    public static String convertToNearestUnit(long size) {
        StringBuilder endstring = new StringBuilder();
        double convertedsize;
        endstring.append("SIZE");
        if (size < 99L) {
            convertedsize = (double) size;
            endstring.append("B");
        } else if (size > 99L && size < 100000L) {
            convertedsize = (double) size / 1000D;
            endstring.append("K");
        } else if(size > 99999L && size < 100000000L) {
            convertedsize = (double) size / 1000000D;
            endstring.append("M");
        } else if (size > 99999999L && size < 100000000000L) {
            convertedsize = (double) size / 100000000D;
            endstring.append("G");
        } else if (size > 99999999999L && size < 100000000000000L) {
            convertedsize = (double) size / 1000000000000D;
            endstring.append("T");
        } else {
            convertedsize = (double) size / 10000000000000000D;
            endstring.append("P");
        }

        if (convertedsize == (long) convertedsize) return endstring.toString().replace("SIZE", String.format("%d", (int) convertedsize));
        return endstring.toString().replace("SIZE", String.format("%.2f", convertedsize));
    }

    /**
     * Read a file in text mode (CURRENTLY NOT USABLE FOR BINARY FILES)
     * @param filename the file to read
     * @return the file contents
     */
    public static String readFilePlain(String filename) {
        //TODO: Read binary on non plain-text file
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replaceFirst("/", "").replace("..", "");
        }

        File toberead = Server.wantedfile;
        StringBuilder tempstring = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(toberead));
            String line;

            while((line = br.readLine()) != null) {
                tempstring.append(line);
                tempstring.append("\n");
            }
            return tempstring.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "<center><h1>FILE IO ERROR</center></h1>";
        }

    }

    /**
     * Tries to read the wanted scriptfile. Prints out an error to the browser when the read fails
     * @param file the scriptfile
     * @return contents of file
     */
    public static String readScriptFile(File file) {
        StringBuilder tempstring = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while((line = br.readLine()) != null) {
                tempstring.append(line);
                tempstring.append("\n");
            }
            return tempstring.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "a.write('<font color=\"red\" size=\"120\">Error reading file</font>";
        }
    }

    /**
     * list al the files in a given directory
     * @param path
     * @param header
     * @return
     */
    public static String listFiles(String path, ClientHeader header) {
        File[] filelist = (new File(Server.config.getWwwroot() + "/" + path).listFiles());
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n\t<head>\n\t\t<meta http-equiv=\"Content-Type\" content=\"text-html; charset=utf-8\"/>\n\t\t<title>Index of ").append(path).append("</title>\n\t</head>\n\t<body>\n");
        html.append("\t\t<h1>Index of ").append(path).append("</h1>\n");
        html.append("\t\t<table>\n");
        html.append("\t\t\t<tr>\n\t\t\t\t<td style=\"width: 300px;\">Name</td><td style=\"width: 300px;\">Last modified</td><td style=\"width: 300px;\">Size</td>\n\t\t\t</tr>");
        html.append("\n\t\t\t<tr>\n\t\t\t\t<td style=\"width: 300px;\"><a href=\"").append(getParentDirectory(header.getRequesteddocument())).append("\">..</a></td><td style=\"width: 300px;\"></td><td style=\"width: 300px;\"></td>\n\t\t\t</tr>");

        for (File dir : filelist) {
            if (dir.isFile()) continue;
            Date lastmodified = new Date(dir.lastModified());
            html.append("\n\t\t\t<tr>\n\t\t\t\t<td>").append("<a href=\"").append(path).append("/").append(dir.getName()).append("\">").append(dir.getName()).append("</a></td><td>").append(lastmodified.toString()).append("</td><td>").append("-</td>\n\t\t\t</tr>");
        }

        for (File f : filelist) {
            if (f.isDirectory()) continue;
            Date lastmodified = new Date(f.lastModified());
            html.append("\n\t\t\t<tr>\n\t\t\t\t<td>").append("<a href=\"").append(path).append("/").append(f.getName()).append("\">").append(f.getName()).append("</a></td><td>").append(lastmodified.toString()).append("</td><td>").append(convertToNearestUnit(f.length())).append("</td>\n\t\t\t</tr>");
        }
        html.append("\n\t\t</table>");
        html.append("\n\t\t<hr>\n\t\t$(servername)"+ (!Server.config.isVersionSuppressed() ? "/" + Server.VERSION : "")+"\n\t</body>\n</html>");

        return HtmlUtils.processHTML(html.toString(), header);
    }

    /**
     * 0 = not exist, 1 = exists, 2 = no permissions, 3 = directory
     *
     * checks if a wanted file exists
     * @param filename the file to check
     * @return status code of the file
     */
    public static int fileExists(String filename) throws IOException {
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replace("..", "").replaceFirst("/", "");
        }

        File temp = new File( Server.config.getWwwroot() + "/" + filename);
        File temp2 = new File(Server.currdir + "/" + filename);

        if (!Server.blacklist.contains(filename)) {
            if ((temp.exists() && temp.isDirectory()) || (temp2.exists() && temp2.isDirectory())) {
                if (temp.exists()) Server.currdir = temp.getAbsolutePath();
                else Server.currdir = temp2.getAbsolutePath();
                return 3;
            } else if (temp.exists() || temp2.exists()){
                if (temp.exists()) Server.wantedfile = new File(temp.getAbsolutePath());
                else Server.wantedfile = new File(temp2.getAbsolutePath());

                Server.wantedfilemime = Files.probeContentType(Server.wantedfile.toPath());
                if (Server.wantedfilemime == null && filename.endsWith(".pyfs")) {
                    Server.wantedfilemime = "text/html";
                }
                Server.wantedfileLastModified = new Date(Server.wantedfile.lastModified());
                return 1;
            }
            return 0;
        }
        return 2;
    }

    /**
     * Gets the parent directory of a file
     * @param currdocument the file that we want to know the parent directory of
     * @return the parent directory of a file or ..
     */
    public static  String getParentDirectory(String currdocument) {
        int slashpos = 0;
        for (int i = currdocument.length() - 1; i > 0; i--) {
            if (currdocument.charAt(i) == '/') {
                slashpos = i;
                break;
            }
        }
        if (slashpos != 0) return currdocument.substring(0, slashpos);
        return "..";
    }

    /**
     * reads, converts html to python and then interprets the python.
     * @param scriptfile scriptfile to be run
     * @param relpath the relative path to the scriptfile (from the wwwroot directory)
     * @param rqid the request id to know which result corresponds to which client / request
     * @return the html of the interpreted scriptfile
     */
    public static String interpretScriptFile(File scriptfile, String relpath, String rqid) {
        Server.scriptresults.put(rqid, new StringBuilder());
        Server.scriptheader.put(rqid, Server.currentHeader);
        try {
            if (fileExists(relpath) == 1) {
                String contents = readScriptFile(Server.wantedfile);

                boolean htmlfound = false;
                int startidx = 0, endidx = 0;

                //Extract html areas from script file
                while(contents.contains("\"\"\"html")) {
                    for (int i = 0; i < contents.length(); i++) {
                        if (i < contents.length() - 7) {
                            if (contents.substring(i, i + 7).equals("\"\"\"html")) {
                                htmlfound = true;
                                startidx = i + 7;
                                endidx = contents.length() - 1;
                            } else if (contents.substring(i, i + 7).equals("html\"\"\"")) {
                                if (htmlfound) {
                                    endidx = i - 1;
                                    contents = MiscUtils.strCutAndConvert(contents, startidx, endidx, MiscUtils.getCurrentLine(contents, i));
                                    htmlfound = false;
                                    break;
                                } else {
                                    //Count newlines to get the line number
                                    String linecounttemp = contents.substring(0, i + 1);
                                    int cnt = MiscUtils.countChar(linecounttemp, '\n') + 1;

                                    contents = "a.write('<font color=\"red\" size=\"120\">Error: missing opening html tag (\"\"\"html) for closing tag (html\"\"\") in line " + cnt + "</font>";
                                }
                            }
                        }
                    }
                }

                PythonInterpreter pi = new PythonInterpreter();
                pi.exec("import me.felixnaumann.fwebserver.api.PythonApi as PythonApi");
                pi.exec("a = PythonApi(\"" + rqid + "\")");
                HashMap<String, String> getparams = MiscUtils.getGETParams(Server.currentHeader.getGETparams());
                String dict = MiscUtils.constructPythonDictFromHashMap("GET", getparams);
                pi.exec(dict);
                pi.exec(contents);

                return Server.scriptresults.get(rqid).toString();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}