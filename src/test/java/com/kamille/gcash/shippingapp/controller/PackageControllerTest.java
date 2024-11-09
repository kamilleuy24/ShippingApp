package com.kamille.gcash.shippingapp.controller;

import com.kamille.gcash.shippingapp.model.PackageModel;
import io.swagger.client.api.VoucherApi;
import io.swagger.client.model.VoucherItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static org.mockito.ArgumentMatchers.any;
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

    @Autowired
    private KieContainer kieContainer;

    @MockBean
    private VoucherApi voucherApi;

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

    @Test
    void correctValuesAndValidVoucherShouldReturnDiscountedPrice() throws Exception {
        String json = "{\"weight\":\"11\",\"height\":\"5\",\"width\":\"5\",\"length\":\"5\",\"voucherCode\":\"ee\"}";
        VoucherItem voucherItem = new VoucherItem();
        voucherItem.setDiscount(5f);
        voucherItem.setExpiry(LocalDate.of(Year.MAX_VALUE, Month.DECEMBER, 31));
        when(voucherApi.voucher(any(), any())).thenReturn(voucherItem);

        this.mockMvc.perform(post("/api/calculate").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content()
                        .string("Shipping Cost: \u20B1215.00"));
    }

}
