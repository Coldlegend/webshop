package com.webshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineShopApplication.class);
    }
    /*
    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            productRepository.save(new Product("Logitech MX Master 3S", new BigDecimal("39900"), 15));
            productRepository.save(new Product("Apple MacBook Pro 14", new BigDecimal("799900"), 5));
            productRepository.save(new Product("Dell UltraSharp Monitor", new BigDecimal("149900"), 0));

            System.out.println(">>> Kezdő termékek betöltve a H2-be! <<<");
        };
    }

    */
}