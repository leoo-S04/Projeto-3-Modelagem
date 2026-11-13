package br.pucpr.ib2b.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação web (biblioteca.jar com Tomcat embutido — diagrama de implantação).
 * O Spring só varre o pacote {@code web}: o domínio e os padrões continuam em Java puro.
 */
@SpringBootApplication
public class AplicacaoBiblioteca {
    public static void main(String[] args) {
        SpringApplication.run(AplicacaoBiblioteca.class, args);
    }
}
