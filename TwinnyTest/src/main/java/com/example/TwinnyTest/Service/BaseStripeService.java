package com.example.TwinnyTest.Service;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

public abstract class BaseStripeService{
    @Value("${stripe.api.key}")
    private String STRIPE_API_KEY;

    @PostConstruct
    public void init() {
        Stripe.apiKey = STRIPE_API_KEY;
    }
}