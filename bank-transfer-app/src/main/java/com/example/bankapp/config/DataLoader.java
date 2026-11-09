package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            customerRepository.save(new Customer(null, "Kamal Perera", "100001", "kamal@example.com", new BigDecimal("50000.00")));
            customerRepository.save(new Customer(null, "Nimali Silva", "100002", "nimali@example.com", new BigDecimal("25000.00")));
            customerRepository.save(new Customer(null, "Saman Fernando", "100003", "saman@example.com", new BigDecimal("10000.00")));
        }
    }
}