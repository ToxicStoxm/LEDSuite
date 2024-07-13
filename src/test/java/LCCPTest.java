import com.toxicstoxm.lccp.LCCP;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class LCCPTest {

    @Test
    public void testVersionPropertiesReturnsVersion() {
        // Load the version.properties file
        try (InputStream inputStream = LCCP.class.getResourceAsStream("/version.properties")) {
            assertNotNull(inputStream, "version.properties file not found");

            // Load the properties
            Properties properties = new Properties();
            properties.load(inputStream);

            // Retrieve the version property
            String version = properties.getProperty("app.version");

            // Check that the version property is not null or empty
            assertNotNull(version, "app.version property not found");
            assertFalse(version.isEmpty(), "app.version property is empty");
        } catch (IOException e) {
            fail("Exception occurred while loading version.properties: " + e.getMessage());
        }
    }
    @Test
    public void testConfigYamlExists() {
        // Attempt to load the config.yaml from resources
        try (InputStream inputStream = getClass().getResourceAsStream("/config.yaml")) {
            assertNotNull(inputStream, "config.yaml file not found in resources");
        } catch (IOException e) {
            fail("IOException occurred while trying to access config.yaml: " + e.getMessage());
        }
    }

    @Test
    public void testServerConfigYamlExists() {
        // Attempt to load the server-config.yaml from resources
        try (InputStream inputStream = getClass().getResourceAsStream("/server_config.yaml")) {
            assertNotNull(inputStream, "server-config.yaml file not found in resources");
        } catch (IOException e) {
            fail("IOException occurred while trying to access server-config.yaml: " + e.getMessage());
        }
    }
}
