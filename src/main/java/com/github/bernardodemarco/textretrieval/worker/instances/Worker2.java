package com.github.bernardodemarco.textretrieval.worker.instances;

import com.github.bernardodemarco.textretrieval.utils.FileUtils;
import com.github.bernardodemarco.textretrieval.worker.Worker;

import java.util.Properties;

public class Worker2 {
    public static void main(String[] args) {
        Properties properties = FileUtils.readPropertiesFile("/workers/worker2.properties");
        int port = Integer.parseInt(properties.getProperty("worker.server.port"));

        new Worker(port).handleRequests();
    }
}
