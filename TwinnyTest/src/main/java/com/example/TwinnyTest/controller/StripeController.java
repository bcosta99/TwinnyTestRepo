package com.example.TwinnyTest.controller;

import com.example.TwinnyTest.model.CreateCustomerReq;
import com.example.TwinnyTest.model.CustomerDTO;
import com.example.TwinnyTest.model.InvoiceDTO;
import com.example.TwinnyTest.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class StripeController {
    private final StripeService stripeService;

    @PostMapping("/")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody @NotNull CreateCustomerReq req) {
        log.debug("Creating customer with name: {} and email: {}", req.getName(), req.getEmail());
        var response = stripeService.createCustomer(req.getName(), req.getEmail());
        log.debug("Created Customer with id: {}", response.getCustomerId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> retrieveCustomer(@PathVariable("customerId") String customerId) {
        log.debug("Retrieving customer with id: {}", customerId);
        var response = stripeService.retrieveCustomer(customerId);
        log.debug("Customer with id: {} found", customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/invoices")
    public ResponseEntity<List<InvoiceDTO>> retrieveCustomerInvoices(@PathVariable("customerId") String customerId) throws StripeException {
        log.debug("Retrieving invoices for customer id");
        var response = stripeService.retrieveInvoices(customerId)
                .stream()
                .map(inv -> {
                    var invoiceDto = new InvoiceDTO();
                    invoiceDto.setInvoicePdf(inv.getInvoicePdf());
                    invoiceDto.setPaid(inv.getPaid());
                    invoiceDto.setNumber(inv.getNumber());
                    invoiceDto.setAmount(inv.getAmountDue());
                    invoiceDto.setBillingDate(inv.getDueDate());
                    return invoiceDto;
                })
                .collect(Collectors.toList());
        log.debug("Invoices found for customer: {}", customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}/subscription")
    public ResponseEntity<Void> cancelSubscription(@PathVariable("customerId") String customerId) throws StripeException {
        log.debug("Cancelling subscription for customer with id: {}", customerId);
        stripeService.cancelSubscription(customerId);
        log.debug("Cancelled subscription for customer with id: {}", customerId);
        return ResponseEntity.noContent().build();
    }
}