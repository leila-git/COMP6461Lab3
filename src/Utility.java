import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Utility {
    public static void writeToFile(String path, String textTmp) {

        try {
            File file = new File(path);
            FileWriter fw = new FileWriter(file);
            fw.write(textTmp);
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String fileReader(String path) {
        String out = "";
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);
            StringBuilder sb = new StringBuilder();

            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                sb.append(System.lineSeparator());
            }

            sc.close();
            out = sb.toString();

            return (out);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (out);

    }
}
