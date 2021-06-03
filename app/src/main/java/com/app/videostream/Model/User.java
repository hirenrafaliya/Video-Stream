package com.app.videostream.Model;

public class User {

    String id;
    String name;
    String email;
    String number;
    String packageAmount;
    String packageStarted;
    String packageExpire;
    String isFreeUsed;
    String paymentId;


    public User(String id, String name, String email, String number, String packageAmount, String packageStarted, String packageExpire, String isFreeUsed, String paymentId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.number = number;
        this.packageAmount = packageAmount;
        this.packageStarted = packageStarted;
        this.packageExpire = packageExpire;
        this.isFreeUsed = isFreeUsed;
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public User() {
    }

    public String getIsFreeUsed() {
        return isFreeUsed;
    }

    public void setIsFreeUsed(String isFreeUsed) {
        this.isFreeUsed = isFreeUsed;
    }

    public String getPackageExpire() {
        return packageExpire;
    }

    public void setPackageExpire(String packageExpire) {
        this.packageExpire = packageExpire;
    }

    public String getPackageAmount() {
        return packageAmount;
    }

    public void setPackageAmount(String packageAmount) {
        this.packageAmount = packageAmount;
    }

    public String getPackageStarted() {
        return packageStarted;
    }

    public void setPackageStarted(String packageStarted) {
        this.packageStarted = packageStarted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
