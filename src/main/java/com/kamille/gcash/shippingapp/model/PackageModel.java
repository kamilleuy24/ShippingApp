package com.kamille.gcash.shippingapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PackageModel {

    @DecimalMin(value = "0.001", message = "Please input a valid value for weight")
    @NotNull
    private float weight;

    @DecimalMin(value = "0.001", message = "Please input a valid value for height")
    @NotNull
    private float height;

    @DecimalMin(value = "0.001", message = "Please input a valid value for width")
    @NotNull
    private float width;

    @DecimalMin(value = "0.001", message = "Please input a valid value for length")
    @NotNull
    private float length;

    private float cost;

    private String voucherCode;

    @JsonIgnore
    private float volume;

    public PackageModel() {

    }

    public PackageModel(@Min(value = 0, message = "Please input a valid value for weight") @NotNull float weight,
                        float height, float width, float length, String voucherCode) {
        super();
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
        this.voucherCode = voucherCode;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public float getVolume() {
        this.volume = height * width * length;
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
