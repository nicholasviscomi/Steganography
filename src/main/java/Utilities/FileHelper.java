package Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class FileHelper {

    public FileHelper() {

    }

    public String readTxtFromFile(String path) throws Exception {

        File file = new File(path);
        byte[] contents = Files.readAllBytes(file.toPath());
        return new String(contents, StandardCharsets.US_ASCII);
    }
}
