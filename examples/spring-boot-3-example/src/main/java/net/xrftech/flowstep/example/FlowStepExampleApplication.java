package net.xrftech.flowstep.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot 3 Application demonstrating FlowStep library usage.
 * 
 * This application showcases:
 * - Multi-step query operations using QueryTemplate
 * - Multi-step command operations using CommandTemplate
 * - Integration with both JPA and MyBatis
 * - Transactional command operations
 * - Comprehensive error handling and validation
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("net.xrftech.flowstep.example.mapper")
public class FlowStepExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowStepExampleApplication.class, args);
    }
}