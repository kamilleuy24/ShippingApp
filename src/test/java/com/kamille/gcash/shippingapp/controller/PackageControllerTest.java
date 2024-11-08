package com.kamille.gcash.shippingapp.controller;

import com.kamille.gcash.shippingapp.model.PackageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PackageController.class)
public class PackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KieContainer kieContainer;

    @MockBean
    private KieSession kieSession;

    @MockBean
    private PackageModel packageModel;

    @BeforeEach
    void setup() {
        when(kieContainer.newKieSession()).thenReturn(kieSession);
        when(kieSession.insert(packageModel)).thenReturn(null);
        when(kieSession.fireAllRules()).thenReturn(1);
        doNothing().when(kieSession).dispose();
    }

    @Test
    void incorrectWeightFormatShouldReturnErrorMessage() throws Exception {
        String json = "{\"weight\":\"TEXT\",\"height\":\"2\",\"width\":\"2\",\"length\":\"2\",\"voucherCode\":\"ee\"}";

        this.mockMvc.perform(post("/api/calculate").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .string("Incorrect type of value: is of type: String, expected: float"));

    }

    @Test
    void incorrectHeightFormatShouldReturnErrorMessage() throws Exception {
        String json = "{\"weight\":\"10.1\",\"height\":\"TEXT\",\"width\":\"2\",\"length\":\"2\",\"voucherCode\":\"ee\"}";

        this.mockMvc.perform(post("/api/calculate").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .string("Incorrect type of value: is of type: String, expected: float"));

    }

    @Test
    void incorrectMinValueShouldReturnErrorMessage() throws Exception {
        String json = "{\"weight\":\"10.1\",\"height\":\"3\",\"width\":\"2\",\"length\":\"0\",\"voucherCode\":\"ee\"}";

        this.mockMvc.perform(post("/api/calculate").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .string("length: Please input a valid value for length"));

    }

}
