import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClientServiceProvider implements Runnable {

    boolean endOfThread = false;
    String directory;
    Object clientServiceProviderMonitor;

    public ClientServiceProvider(String directory, Object clientServiceProviderMonitor) {
        this.clientServiceProviderMonitor = clientServiceProviderMonitor;
        this.directory = directory;
    }

    @Override
    public void run() {

        String tmpStr = "";
        String cmdLines[] = new String[100];
        String everything = "";
        byte[] everythingByte = null;
        try {

            String command = Utility.fileReader(Constants.currentPath + "/test/server/in/command.txt");

            cmdLines[0] = command;

            String[] arrCommand = cmdLines[0].split(" ");

            if (arrCommand[0].trim().equalsIgnoreCase("GET")) {

                if (arrCommand[1].trim().equalsIgnoreCase("/")) {

                    ArrayList<File> files = (ArrayList<File>) FolderExplorer.listf(directory);
                    StringBuilder sbFileNames = new StringBuilder();

                    for (File f : files) {
                        sbFileNames.append(f.getName());
                        sbFileNames.append(System.lineSeparator());
                    }
                    everything = sbFileNames.toString();
                    everythingByte = everything.getBytes();

                    FileOutputStream os = new FileOutputStream(Constants.currentPath + "/test/server/out/result.txt");
                    os.write(everythingByte);
                    os.close();

                } else {
                    boolean find = false;

                    tmpStr = "";
                    String tmpFileName = arrCommand[1].substring(arrCommand[1].lastIndexOf("/") + 1,
                            arrCommand[1].length());
                    tmpFileName = tmpFileName.trim();
                    tmpFileName = tmpFileName.replace("\r\n", "");
                    ArrayList<File> files = (ArrayList<File>) FolderExplorer.listf(directory);

                    for (File f : files) {

                        if (find == false) {

                            tmpStr = f.getName();
                            if (tmpStr.equalsIgnoreCase(tmpFileName)) {
                                find = true;
                                Path path = Paths.get(directory + "/" + f.getName());

                                // Load as binary:
                                everythingByte = Files.readAllBytes(path);


                            }
                        }
                    }
                }
                FileOutputStream os = new FileOutputStream(Constants.currentPath + "/test/server/out/result.txt");
                os.write(everythingByte);
                os.close();

            } else if (arrCommand[0].trim().equalsIgnoreCase("POST")) {

                String tmpFileName = arrCommand[1].substring(arrCommand[1].lastIndexOf("/") + 1,
                        arrCommand[1].length());

                if (!tmpFileName.contains(".")) {
                    tmpFileName = tmpFileName + ".txt";
                }

                PrintWriter writer = new PrintWriter(directory + "/" + tmpFileName, "UTF-8");
                int i = extractData(cmdLines[0].split(" "));

                String strData;
                String[] tmpStr123 = arrCommand[2].trim().split(System.lineSeparator());

                for (int k = 0; k < tmpStr123.length; k++) {
                    writer.println(tmpStr123[k]);
                }
                writer.close();


                everything = "Sucessful";

            } else {
                everything = "Invalid Message Format";

            }


            endOfThread = true;
            synchronized (clientServiceProviderMonitor) {
                clientServiceProviderMonitor.notify();
            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }

    public boolean isEndOfThread() {
        return endOfThread;
    }

    public void setEndOfThread(boolean endOfThread) {
        this.endOfThread = endOfThread;
    }

    private int extractData(String[] cmdLines) {
        int index = 0;
        for (index = 0; index < cmdLines.length; index++) {
            if (cmdLines[index].length() == 0) {
                return index;
            }
        }
        return index;
    }

}
