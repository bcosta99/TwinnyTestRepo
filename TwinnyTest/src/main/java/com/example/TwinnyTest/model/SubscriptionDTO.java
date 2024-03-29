package com.example.TwinnyTest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDTO {
    private String subscriptionId;
    private Long startAt;
    private Long periodEnd;
    private String status;
}