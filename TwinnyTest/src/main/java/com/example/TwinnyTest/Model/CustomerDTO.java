package com.example.TwinnyTest.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private String customerId;
    private String email;
    private String name;
    private SubscriptionDTO subscription;
}