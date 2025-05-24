package com.Iviinvest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HealthControllerTest {

    @Test
    void deveRetornarStatusApiOnline() {
        // Arrange
        HealthController controller = new HealthController();

        // Act
        ResponseEntity<Map<String, String>> response = controller.healthCheck();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("API online", response.getBody().get("status"));
    }
}
