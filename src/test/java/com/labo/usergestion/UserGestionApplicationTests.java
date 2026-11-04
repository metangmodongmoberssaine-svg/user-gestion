package com.labo.usergestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.labo.usergestion.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserGestionApplicationTests {

    @Autowired
    private UserController controller;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }
}
