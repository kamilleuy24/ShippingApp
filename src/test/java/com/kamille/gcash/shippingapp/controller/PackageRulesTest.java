package com.kamille.gcash.shippingapp.controller;

import com.kamille.gcash.shippingapp.model.PackageModel;
import io.swagger.client.api.VoucherApi;
import io.swagger.client.model.VoucherItem;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class PackageRulesTest {

    private PackageController controller;

    @Autowired
    private KieContainer kieContainer;

    @MockBean
    private VoucherApi voucherApi;

    private String apiKey = "apikey";

    @BeforeEach
    void setup() {
        controller = new PackageController(kieContainer, voucherApi, apiKey);
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void testRejectRule() throws Exception {
        @Valid
        PackageModel packageModel = new PackageModel(51.0f, 4.0f, 20.0f, 10.0f, null);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: N/A", response.getBody());
    }

    @Test
    public void testHeavyParcelRule() throws Exception {
        @Valid
        PackageModel packageModel = new PackageModel(11.0f, 4.0f, 20.0f, 10.0f, null);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱220.00", response.getBody());
    }

    @Test
    public void testSmallParcelRule() throws Exception {
        @Valid
        PackageModel packageModel = new PackageModel(9.5f, 10.0f, 10.1f, 9.3f, null);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱28.18", response.getBody());
    }

    @Test
    public void testMediumParcelRule() throws Exception {
        @Valid
        PackageModel packageModel = new PackageModel(8.5f, 15.0f, 10.1f, 12.2f, null);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱73.93", response.getBody());
    }

    @Test
    public void testLargeParcelRule() throws Exception {
        @Valid
        PackageModel packageModel = new PackageModel(8.5f, 23.0f, 19.1f, 17.5f, null);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱384.39", response.getBody());
    }

    @Test
    public void testValidVoucherDiscount() {
        @Valid
        PackageModel packageModel = new PackageModel(8.5f, 23.0f, 19.1f, 17.5f, "MYNT");
        VoucherItem voucherItem = new VoucherItem();
        voucherItem.setDiscount(12f);
        voucherItem.setExpiry(LocalDate.of(Year.MAX_VALUE, Month.DECEMBER, 31));
        when(voucherApi.voucher("MYNT", "apikey")).thenReturn(voucherItem);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱372.39", response.getBody());
    }

    @Test
    public void testInvalidVoucherDiscount() {
        @Valid
        PackageModel packageModel = new PackageModel(8.5f, 23.0f, 19.1f, 17.5f, "FAKEVOUCHER");
        when(voucherApi.voucher("MYNT", "apikey")).thenReturn(null);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱384.39", response.getBody());
    }

    @Test
    public void testExpiredVoucherDiscount() {
        @Valid
        PackageModel packageModel = new PackageModel(8.5f, 23.0f, 19.1f, 17.5f, "MYNT");
        VoucherItem voucherItem = new VoucherItem();
        voucherItem.setDiscount(12f);
        voucherItem.setExpiry(LocalDate.of(2022, Month.DECEMBER, 31));
        when(voucherApi.voucher("MYNT", "apikey")).thenReturn(voucherItem);
        ResponseEntity<String> response = controller.calculate(packageModel);
        assertEquals("Shipping Cost: ₱384.39", response.getBody());
    }

}
