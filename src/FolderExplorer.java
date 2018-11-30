import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderExplorer {
    String path = "";

    public FolderExplorer(String path) {
        this.path = path;
    }

    public static List<File> listf(String directoryName) {
        File directory = new File(directoryName);
        String fileNames = "";
        List<File> resultList = new ArrayList<File>();

        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        return resultList;
    }
}

