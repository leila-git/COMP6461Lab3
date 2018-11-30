import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.net.URL;
import java.util.Arrays;

public class CommandLineParser {

    URL cmdURL = null;
    String host = null;
    String query = null;
    boolean hasV = false;
    String[] paramH = null;
    String[] paramD = null;
    String[] paramF = null;
    String mainCmd = null;
    String[] cmdLineArray;
    String path = null;
    String execCommand = null;
    String[] paramO = null;
    int urlFirstIndex;
    int urlLastIndex;
    int pathLastIndex;
    String cmdLine;
    String helpCommand;
    public CommandLineParser(String cmdLine) {
        cmdLineArray = cmdLine.split(" ");
        this.cmdLine = cmdLine;
    }
    public CommandLineParser(String[] cmdLineArr) {

        cmdLineArray = cmdLineArr;
    }

    public String[] getParamO() {
        return paramO;
    }

    public void setParamO(String[] paramO) {
        this.paramO = paramO;
    }

    public String getHelpCommand() {
        return helpCommand;
    }

    public void setHelpCommand(String helpCommand) {
        this.helpCommand = helpCommand;
    }

    public int validateCommandLine(String[] cmdLineArray) {
        String tmpStrPath;
        try {
            this.cmdLine = createCmdLineString(cmdLineArray);
            OptionParser parserCmdLine = new OptionParser("vh:d:f:o:");
            OptionSet optionsCmdLine = parserCmdLine.parse(cmdLineArray);
            if (optionsCmdLine.has("d") & optionsCmdLine.has("f")) {
                return -8;
            }

            int indexMainCmd = 0;
            if (!optionsCmdLine.has("v")) {
                hasV = false;
            } else {
                hasV = true;
            }


            if (!(cmdLineArray[indexMainCmd].equalsIgnoreCase("GET") |
                    cmdLineArray[indexMainCmd].equalsIgnoreCase("POST") |
                    cmdLineArray[indexMainCmd].equalsIgnoreCase("HELP"))) {
                return ErrorDescriptions.MAIN_COMMAND_ERROR_CODE;
                //Command is not supported
            }

            mainCmd = cmdLineArray[indexMainCmd].toUpperCase();

            if (cmdLineArray[indexMainCmd].equalsIgnoreCase("GET") |
                    cmdLineArray[indexMainCmd].equalsIgnoreCase("POST")) {

                if (optionsCmdLine.has("o")) {
                    if (validate_o(optionsCmdLine)) {
                        cmdURL = new URL(cmdLineArray[cmdLineArray.length - 3]);
                        tmpStrPath = cmdURL.getPath();
                        urlFirstIndex = cmdLine.indexOf(cmdLineArray[cmdLineArray.length - 3]);
                        urlLastIndex = cmdLine.indexOf(tmpStrPath);


                        paramO = Arrays.copyOf(optionsCmdLine.valuesOf("o").toArray(), optionsCmdLine.valuesOf("o").toArray().length, String[].class);
                    } else {
                        return -11;
                    }

                } else {
                    cmdURL = new URL(cmdLineArray[cmdLineArray.length - 1]);
                    tmpStrPath = cmdURL.getPath();
                    urlFirstIndex = cmdLine.indexOf(cmdLineArray[cmdLineArray.length - 1]);

                    urlLastIndex = cmdLine.indexOf(tmpStrPath);
                }//*************


                if (cmdURL.getHost().isEmpty()) {
                    return ErrorDescriptions.URL_EMPTY_ERROR_CODE;
                }
                host = cmdURL.getHost();
                query = cmdURL.getQuery();
                setPath(cmdURL.getPath());
            }
            if (cmdLineArray[indexMainCmd].equalsIgnoreCase("HELP")) {
                if (!(cmdLineArray[indexMainCmd + 1].equalsIgnoreCase("GET") |
                        cmdLineArray[indexMainCmd + 1].equalsIgnoreCase("POST"))) {
                    return ErrorDescriptions.HELP_PARAMETERS_ERROR_CODE;
                }


                if (cmdLineArray.length > 3) {
                    return ErrorDescriptions.HELP_COMMAND_LEN_ERROR_CODE;
                }
                helpCommand = cmdLineArray[indexMainCmd + 1];
            }

            if (optionsCmdLine.has("h")) {
                if (validate_h(optionsCmdLine)) {
                    paramH = Arrays.copyOf(optionsCmdLine.valuesOf("h").toArray(), optionsCmdLine.valuesOf("h").toArray().length, String[].class);
                } else {
                    return -15;
                }
            }

            if (optionsCmdLine.has("d")) {
                if (validate_d(optionsCmdLine)) {
                    paramD = Arrays.copyOf(optionsCmdLine.valuesOf("d").toArray(), optionsCmdLine.valuesOf("d").toArray().length, String[].class);
                } else {
                    return -16;
                }
            }

            if (optionsCmdLine.has("f")) {
                if (validate_f(optionsCmdLine)) {
                    paramF = Arrays.copyOf(optionsCmdLine.valuesOf("f").toArray(), optionsCmdLine.valuesOf("f").toArray().length, String[].class);
                } else {
                    return -16;
                }

            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -100;
        }

        return 0;

    }

    public int getUrlLastIndex() {
        return urlLastIndex;
    }

    public void setUrlLastIndex(int urlLastIndex) {
        this.urlLastIndex = urlLastIndex;
    }

    public int getUrlFirstIndex() {
        return urlFirstIndex;
    }

    public void setUrlFirstIndex(int urlFirstIndex) {
        this.urlFirstIndex = urlFirstIndex;
    }

    public String getExecCommand() {
        return execCommand;
    }

    public void setExecCommand(String execCommand) {
        this.execCommand = execCommand;
    }

    public String getPath() {
        if (path.length() == 0)
            return "/";

        return path;
    }

    public void setPath(String path) {
        if (path.length() == 0) {
            path = "/";
        }

        if (!path.substring(path.length() - 1, path.length()).equalsIgnoreCase("/")) {
            path = path + "/";
        }
        String[] arrTmp = path.split("/");

        if (arrTmp.length > 0 && arrTmp[arrTmp.length - 1].contains(".")) {
            this.path = path.substring(0, path.length() - 1);
        } else {
            this.path = path;
        }

    }

    public URL getCmdURL() {
        return cmdURL;
    }

    public void setCmdURL(URL cmdURL) {
        this.cmdURL = cmdURL;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isHasV() {
        return hasV;
    }

    public void setHasV(boolean hasV) {
        this.hasV = hasV;
    }

    public String[] getParamH() {
        return paramH;
    }

    public void setParamH(String[] paramH) {
        this.paramH = paramH;
    }

    public String[] getParamD() {
        return paramD;
    }

    public void setParamD(String[] paramD) {
        this.paramD = paramD;
    }

    public String[] getParamF() {
        return paramF;
    }

    public void setParamF(String[] paramF) {
        this.paramF = paramF;
    }

    public String getMainCmd() {
        return mainCmd;
    }

    public void setMainCmd(String mainCmd) {
        this.mainCmd = mainCmd;
    }

    private boolean validate_o(OptionSet optionsCmdLine) {

        if (optionsCmdLine.has("o")) {
            if (optionsCmdLine.valuesOf("o") != null) {
                return true;
            }

            if (optionsCmdLine.valuesOf("o") == null) {
                return false;
            }
        }

        return false;
    }

    private boolean validate_h(OptionSet optionsCmdLine) {

        if (optionsCmdLine.has("h")) {
            if (optionsCmdLine.valuesOf("h") != null) {
                return true;
            }

            if (optionsCmdLine.valuesOf("h") == null) {
                return false;
            }
        }

        return false;
    }

    private boolean validate_d(OptionSet optionsCmdLine) {

        if (optionsCmdLine.has("d")) {
            if (optionsCmdLine.valuesOf("d") != null) {
                return true;
            }

            if (optionsCmdLine.valuesOf("d") == null) {
                return false;
            }
        }

        return false;
    }

    private boolean validate_f(OptionSet optionsCmdLine) {

        if (optionsCmdLine.has("f")) {
            if (optionsCmdLine.valuesOf("f") != null) {
                return true;
            }
            if (optionsCmdLine.valuesOf("f") == null) {
                return false;
            }
        }

        return false;
    }

    private String createCmdLineString(String[] cmdLineArr) {

        String cmdLineString = "";

        for (String tmpStr : cmdLineArr) {
            cmdLineString = cmdLineString + " " + tmpStr;
        }

        return cmdLineString;


    }

    private int validateOrderOfSwitches(String[] cmdLineArr) {
        int indexV = 1000;
        int indexH = 1000;
        int indexD = 1000;
        int indexF = 1000;
        int indexO = 1000;

        for (int i = 0; i < cmdLineArr.length; i++) {
            if (cmdLineArr[i].equalsIgnoreCase("-v")) {
                indexV = i;
            }

            if (cmdLineArr[i].equalsIgnoreCase("-h")) {
                indexH = i;
            }

            if (cmdLineArr[i].equalsIgnoreCase("-d")) {
                indexD = i;
            }

            if (cmdLineArr[i].equalsIgnoreCase("-f")) {
                indexF = i;
            }

            if (cmdLineArr[i].equalsIgnoreCase("-o")) {
                indexO = i;
            }

            if (indexV > indexH | indexV > indexD | indexV > indexF | indexV > indexO) {
                return -2000;
            }

            if (indexH > indexD | indexH > indexF | indexH > indexO) {
                return -3000;
            }

            if (indexD > indexF | indexD > indexO) {
                return -4000;
            }

            if (indexF > indexO) {
                return -5000;
            }

        }
        return 0;
    }
}
