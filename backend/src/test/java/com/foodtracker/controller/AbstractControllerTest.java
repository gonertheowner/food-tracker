package com.foodtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtracker.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for controller smoke tests.
 * Imports production SecurityConfig (currently permits all) and provides
 * MockMvc + ObjectMapper for subclasses.
 * Subclasses must declare @WebMvcTest(SomeController.class).
 */
@Import(SecurityConfig.class)
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
