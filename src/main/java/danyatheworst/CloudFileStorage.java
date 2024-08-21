package danyatheworst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CloudFileStorage {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CloudFileStorage.class, args);
    }
}
