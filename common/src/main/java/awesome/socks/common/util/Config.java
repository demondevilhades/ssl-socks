package awesome.socks.common.util;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public final class Config {
    
    private static final Config CONFIG = new Config();

    private final Properties properties = new Properties();
    
    private Config() {
        log.info("Config.init start");
        try (FileInputStream fis = new FileInputStream(ResourcesUtils.getResource("app.properties").getFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);) {
            properties.load(isr);
            log.info("Config.init end");
        } catch (Exception e) {
            log.error("Config.init error", e);
        }
    }

    public static String get(String key) {
        String property = CONFIG.properties.getProperty(key);
        if (property == null) {
            for (Map.Entry<Object, Object> entry : CONFIG.properties.entrySet()) {
                if (key.toUpperCase().equals(((String) entry.getKey()).toUpperCase())) {
                    if (property == null) {
                        property = (String) entry.getValue();
                    } else {
                        return null;
                    }
                }
            }
        }
        return property;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
