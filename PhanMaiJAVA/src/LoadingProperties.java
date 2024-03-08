import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class LoadingProperties {
    private static final Properties properties = new java.util.Properties();

    public static void loadProperties(String fileName) {
        try (FileInputStream fis = new FileInputStream("src/" + fileName)) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Properties file not found", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading properties file", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}

