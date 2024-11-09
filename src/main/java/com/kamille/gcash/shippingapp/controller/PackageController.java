package com.kamille.gcash.shippingapp.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.kamille.gcash.shippingapp.model.PackageModel;
import io.swagger.client.api.VoucherApi;
import io.swagger.client.model.VoucherItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
public class PackageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageController.class.getName());

    private KieContainer kieContainer;

    private VoucherApi voucherApi;

    private String apiKey;

    @Autowired
    public PackageController(KieContainer kieContainer, VoucherApi voucherApi, @Value("${voucher.key:''}") String apiKey) {
        this.kieContainer = kieContainer;
        this.voucherApi = voucherApi;
        this.apiKey = apiKey;
    }

    @Operation(summary = "Get package cost", description = "Returns a package cost as per the details provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "405", description = "Bad Request - Wrong Details in the Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping("/api/calculate")
    @ResponseBody
    public ResponseEntity<String> calculate(@Valid @RequestBody PackageModel packageModel) {
        LOGGER.info("weight: {}, height: {}, width: {}, length: {}, voucherCode: {}", packageModel.getWeight(),
                packageModel.getHeight(), packageModel.getWidth(), packageModel.getLength(),
                packageModel.getVoucherCode());
        // fire off Drools rules
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(packageModel);
        kieSession.fireAllRules(1);
        kieSession.dispose();

        String shippingCostString = calculateShippingCost(packageModel);

        return new ResponseEntity<>(String.format("Shipping Cost: %s", shippingCostString),
                HttpStatus.OK);
    }

    private String calculateShippingCost(PackageModel packageModel) {
        float shippingCost = packageModel.getCost();
        String shippingCostString = null;
        if (shippingCost == -1.0f) {
            shippingCostString = "N/A";
        } else if (packageModel.getVoucherCode() != null && !packageModel.getVoucherCode().isBlank()) {
            Float discount = getVoucherDiscount(packageModel);
            shippingCost -= discount;
            if (shippingCost <= 0f) {
                shippingCostString = "FREE";
            }
        }

        if (shippingCostString == null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            shippingCostString = formatter.format(shippingCost);
        }
        return shippingCostString;
    }

    private Float getVoucherDiscount(PackageModel packageModel) {
        VoucherItem voucher = voucherApi.voucher(packageModel.getVoucherCode(), apiKey);
        LOGGER.debug("voucher: {}", voucher);
        if (voucher != null && voucher.getExpiry().isAfter(LocalDate.now())) {
            return voucher.getDiscount();
        }
        return 0.0f;
    }

    @ExceptionHandler
    public ResponseEntity<String> handleRequestExceptions(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        LOGGER.error("Exception : {}", exception.getClass());
        String errorMessage = null;

        if (exception instanceof HttpMessageNotReadableException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) ((HttpMessageNotReadableException) exception)
                    .getMostSpecificCause();
            final String sourceType = invalidFormatException.getValue().getClass().getSimpleName();
            final String targetType = invalidFormatException.getTargetType().getSimpleName();
            errorMessage = String.format("Incorrect type of value: is of type: %s, expected: %s", sourceType,
                    targetType);
        } else if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            List<FieldError> fieldErrors = methodArgumentNotValidException.getFieldErrors();
            StringBuilder sb = new StringBuilder();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("\n");
            }
            errorMessage = sb.toString().trim();
        } else {
            // return server error if not one of the two exceptions above
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

}
