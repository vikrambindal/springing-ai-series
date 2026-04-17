package demo.vikram.springai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("app")
public class ApplicationProperties {

    private Map<String, String> credentials = new HashMap<>();
}
