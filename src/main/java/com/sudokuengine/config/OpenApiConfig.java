package com.sudokuengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sudokuEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sudoku Engine API")
                        .description("A full-stack Sudoku generation, solving and algorithm visualization platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sudoku Engine")
                                .url("https://github.com/MuhammetDemir0/sudoku-engine"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
