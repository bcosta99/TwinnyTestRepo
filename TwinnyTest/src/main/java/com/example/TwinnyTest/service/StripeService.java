package com.example.TwinnyTest.service;

import com.example.TwinnyTest.model.CustomerDTO;
import com.example.TwinnyTest.model.SubscriptionDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StripeService extends BaseStripeService {

    @Value("${stripe.product.price.id}")
    private String idPrice;

    public CustomerDTO createCustomer(String name, String email) {
        var params = CustomerCreateParams
                .builder()
                .setName(name)
                .setEmail(email)
                .build();

        Customer customer;
        try {
            customer = Customer.create(params);
        } catch (StripeException e) {
            log.error("Error creating customer", e);
            throw new RuntimeException(e);
        }

        var subCreateParams = SubscriptionCreateParams
                .builder()
                .setCustomer(customer.getId())
                .addItem(
                        SubscriptionCreateParams.Item
                                .builder()
                                .setPrice(idPrice)
                                .build()
                )
                .setTrialEnd(calculateTrialEnd())
                .build();

        Subscription subscription;
        try {
            subscription = Subscription.create(subCreateParams);
        } catch (StripeException e) {
            log.error("Error creating subscription", e);
            throw new RuntimeException(e);
        }

        var subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getId());
        subscriptionDTO.setPeriodEnd(subscription.getCurrentPeriodEnd());
        subscriptionDTO.setStartAt(subscription.getCurrentPeriodStart());
        subscriptionDTO.setStatus(subscription.getStatus());

        var customerDTO = new CustomerDTO();
        customerDTO.setCustomerId(customer.getId());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setName(customer.getName());
        customerDTO.setSubscription(subscriptionDTO);

        return customerDTO;
    }

    public CustomerDTO retrieveCustomer(String customerId) {
        var params = InvoiceListParams
                .builder()
                .setCustomer(customerId)
                .addAllExpand(List.of("data.subscription","data.customer"))
                .build();
        try {
            var invoices = Invoice.list(params);
            var invoice = invoices.getData().get(0);

            var subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setSubscriptionId(invoice.getSubscriptionObject().getId());
            subscriptionDTO.setPeriodEnd(invoice.getSubscriptionObject().getCurrentPeriodEnd());
            subscriptionDTO.setStartAt(invoice.getSubscriptionObject().getCurrentPeriodStart());
            subscriptionDTO.setStatus(invoice.getSubscriptionObject().getStatus());

            var customerDTO = new CustomerDTO();
            customerDTO.setCustomerId(invoice.getCustomerObject().getId());
            customerDTO.setEmail(invoice.getCustomerObject().getEmail());
            customerDTO.setName(invoice.getCustomerObject().getName());
            customerDTO.setSubscription(subscriptionDTO);

            return customerDTO;
        } catch (StripeException e) {
            log.error("Error retrieving customer", e);
            throw new RuntimeException(e);
        }
    }

    public List<Invoice> retrieveInvoices(String customerId, List<String> expandFields) throws StripeException {
        var builder = InvoiceListParams.builder();
        if (expandFields != null) {
            builder = builder.addAllExpand(expandFields);
        }
        var params = builder.setCustomer(customerId).build();

        try {
            return Invoice.list(params)
                    .getData();
        } catch(StripeException ex) {
            throw ex;
        }
    }

    public List<Invoice> retrieveInvoices(String customerId) throws StripeException {
        return retrieveInvoices(customerId, null);
    }

    public void cancelSubscription(String customerId) throws StripeException {
        List<Invoice> invoices = retrieveInvoices(customerId);
        try {
            var subscription = Subscription.retrieve(invoices.get(0).getSubscription());
            subscription.cancel();
        } catch (StripeException ex) {
            log.error("Could not cancel subscription");
        }
    }

    private static long calculateTrialEnd() {
        return Instant.now().plusSeconds(15 * 24 * 60 * 60).getEpochSecond();
    }
}
