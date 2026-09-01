package br.csi.rep_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "API RepHub",
                version = "1.0",
                description = "API para organização e gerenciamento de repúblicas e moradias compartilhadas.",
                contact = @Contact(name = "Ana julia Bock Medina", email = "ana.bock@acad.ufsm.br")
        )
)

@SpringBootApplication
public class RepHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepHubApplication.class, args);
    }

}
