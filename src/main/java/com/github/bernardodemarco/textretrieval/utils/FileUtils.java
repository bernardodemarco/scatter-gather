package com.github.bernardodemarco.textretrieval.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FileUtils {
    private static final String resourcesFolderPath = "src/main/resources";
    private static final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    private FileUtils() {}

    public static <T> T readJSONFile(String fileRelativePath, Type typeOfT) {
        try (JsonReader fileReader = new JsonReader(new FileReader(resourcesFolderPath + fileRelativePath))) {
            return gson.fromJson(fileReader, typeOfT);
        } catch (IOException e) {
            String errorMessage = String.format("File [%s] not found.", resourcesFolderPath + fileRelativePath);
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    public static Properties readPropertiesFile(String fileRelativePath) {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(resourcesFolderPath + fileRelativePath)) {
            properties.load(in);
            return properties;
        } catch (IOException e) {
            logger.error("File [{}] not found.", resourcesFolderPath + fileRelativePath, e);
            throw new RuntimeException(e);
        }
    }

    public static List<String> readTextFile(String fileRelativePath) {
        try {
            return Files.readAllLines(Paths.get(resourcesFolderPath + fileRelativePath));
        } catch (IOException e) {
            logger.error("An error occurred while reading the file [{}].", resourcesFolderPath + fileRelativePath, e);
            throw new RuntimeException(e);
        }
    }
}
