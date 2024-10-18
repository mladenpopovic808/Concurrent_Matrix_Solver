package config;

import java.io.IOException;
import java.util.Properties;


//Klasa koja ce da sadrzi sve informacije iz config.txt
public class Config {

    private static Config instance = null;

    private final Properties properties; //Sluzi za cuvanje kljuc-vrednost (u pozadini je mapa)

    private int sys_explorer_sleep_time;
    private long maximum_file_chink_size;
    private int maximum_rows_size;
    private String start_dir;
    private String fileExtension=".rix";



    public Config() {

        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));

        } catch (IOException e) {
            System.err.println("Error while loading app.properties " + e.getMessage());
        }
    }

    public static Config getInstance() {
        if (instance == null) instance = new Config();
        return instance;
    }

    //ucitamo iz app.properties
    public void loadProperties() {
        sys_explorer_sleep_time = Integer.parseInt(getFromProperty("sys_explorer_sleep_time"));
        maximum_file_chink_size = Long.parseLong(getFromProperty("maximum_file_chink_size"));
        maximum_rows_size = Integer.parseInt(getFromProperty("maximum_rows_size"));
        start_dir = getFromProperty("start_dir");
    }


    //Iz klase property uzimamo vrednost
    private String getFromProperty(String keyName) {

        return properties.getProperty(keyName, "No Such Value");
    }


    @Override
    public String toString() {
        return "Config{sys_explorer_sleep_time=" + sys_explorer_sleep_time +
                ", maximum_file_chink_size=" + maximum_file_chink_size +
                ", maximum_rows_size=" + maximum_rows_size +
                ", start_dir='" + start_dir + '\'' +
                '}';
    }

    public long getSys_explorer_sleep_time() {
        return sys_explorer_sleep_time;
    }

    public long getMaximum_file_chink_size() {
        return maximum_file_chink_size;
    }

    public int getMaximum_rows_size() {
        return maximum_rows_size;
    }

    public String getStart_dir() {
        return start_dir;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
