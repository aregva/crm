package com.gym.crm.domain;

import java.time.LocalDate;

public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}