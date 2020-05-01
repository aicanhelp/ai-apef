package io.apef.base.config.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@ActiveProfiles({"default", "production", "ex-production"})
@TestPropertySource(properties = {
        "spring.config.name = test-app-config",
        "spring.config.location = classpath:spring/",
        "spring.profiles.active = production"
})
@ContextConfiguration(classes = XmlPropertySourceLoaderTest.ContextConfig.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@Slf4j
public class XmlPropertySourceLoaderTest extends AbstractTestNGSpringContextTests {
    @Autowired
    protected MainConfiguration mainConfiguration;

    @Test
    public void test1() {

        log.info("before print:  -------------");
        log.info(mainConfiguration.toString());
        assertEquals(mainConfiguration.getName2(), "b1");
        assertEquals(mainConfiguration.getSub().size(), 4);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableConfigurationProperties(MainConfiguration.class)
    @ComponentScan({"io.apef.base.config.spring"})
    public static class ContextConfig {

    }
}