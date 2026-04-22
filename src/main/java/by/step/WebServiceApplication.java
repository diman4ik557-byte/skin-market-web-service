package by.step;

import by.step.config.JwtAuthenticationFilter;
import by.step.config.JwtTokenProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class WebServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebServiceApplication.class, args);

        System.out.println("=== BEAN CHECK ===");
        System.out.println("JwtTokenProvider: " + (context.getBean(JwtTokenProvider.class) != null));
        System.out.println("JwtAuthenticationFilter: " + (context.getBean(JwtAuthenticationFilter.class) != null));
        System.out.println("==================");
    }
}