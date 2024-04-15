package application;

import lombok.Getter;

import java.util.Properties;

@Getter
public class PropertiesConfigurator {
    private static PropertiesConfigurator instance;
    private final Properties properties;

    private String startDir;
    private String saveDir;
    private String fileExtension;
    private int sysExplorerSleepTime;
    private int maximumFileChunkSize;
    private int maximumRowsSize;

    private PropertiesConfigurator() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {
            System.err.println("APPLICATION/PropertyStorage: Error while loading application.properties: \"" + e.getMessage() + "\"");
        }
    }

    public static PropertiesConfigurator getInstance() {
        if (instance == null) {
            instance = new PropertiesConfigurator();
        }
        return instance;
    }

    public void loadProperties() {
        startDir = readProperty("start_dir");
        System.out.println("APPLICATION/PropertyStorage: Start directory is set to \"" + startDir + "\".");
        saveDir = readProperty("save_dir");
        System.out.println("APPLICATION/PropertyStorage: Save directory is set to \"" + saveDir + "\".");
        fileExtension = readProperty("file_extension");
        System.out.println("APPLICATION/PropertyStorage: File extension is set to \"" + fileExtension + "\".");
        sysExplorerSleepTime = Integer.parseInt(readProperty("sys_explorer_sleep_time"));
        System.out.println("APPLICATION/PropertyStorage: System Explorer sleep time is set to " + sysExplorerSleepTime + " milliseconds.");
        maximumFileChunkSize = Integer.parseInt(readProperty("maximum_file_chunk_size"));
        System.out.println("APPLICATION/PropertyStorage: Maximum file chunk size is set to " + maximumFileChunkSize + " bytes.");
        maximumRowsSize = Integer.parseInt(readProperty("maximum_rows_size"));
        System.out.println("APPLICATION/PropertyStorage: Maximum row size is set to " + maximumRowsSize + " rows.");
    }

    private String readProperty(String key) {
        System.out.println("APPLICATION/PropertyStorage: Loading property \"" + key + "\"...");
        return properties.getProperty(key, "Missing data");
    }
}