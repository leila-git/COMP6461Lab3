public class ErrorDescriptions {

    public static final String HTTPC = "httpc";
    public static final int HTTPC_ERROR_CODE = -1;
    public static final String HTTPC_ERROR_DSC = "There is no HTTPC command";


    public static final String MAIN_COMMAND = "maincommand";
    public static final int MAIN_COMMAND_ERROR_CODE = -2;
    public static final String MAIN_COMMAND_ERROR_DSC = "Main Command Must be Get or Post or Help";

    public static final String HELP_PARAMETERS = "Help_Parameters";
    public static final int HELP_PARAMETERS_ERROR_CODE = -3;
    public static final String HELP_PARAMETERS_ERROR_DSC = "Help Parameters Must be Get or Post";

    public static final int HELP_COMMAND_LEN_ERROR_CODE = -4;
    public static final String HELP_COMMAND_LEN_ERROR_DSC = "The length of help Command must be 3";

    public static final int URL_EMPTY_ERROR_CODE = -5;
    public static final String URL_EMPTY_ERROR_DSC = "The Url is empty";
}
