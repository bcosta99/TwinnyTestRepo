package com.example.TwinnyTest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {
    private String number;
    private Long billingDate;
    private Boolean paid;
    private Long amount;
    private String invoicePdf;
}