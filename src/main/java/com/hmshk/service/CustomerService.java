package com.hmshk.service;

import com.hmshk.model.Customer;
import com.hmshk.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    

    
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
    
    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Customer> searchCustomersByPhone(String phoneNumber) {
        return customerRepository.findByPhoneNumberContaining(phoneNumber);
    }
    
    public List<Customer> getCustomersByVipStatus(String vipStatus) {
        return customerRepository.findByVipStatus(vipStatus);
    }
    
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActive(true);
    }
    
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    
    public Customer updateCustomer(Customer customer) {
        if (!customerRepository.existsById(customer.getId())) {
            throw new RuntimeException("Customer not found with ID: " + customer.getId());
        }
        return customerRepository.save(customer);
    }
    
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
    
    public void deactivateCustomer(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer existingCustomer = customer.get();
            existingCustomer.setActive(false);
            customerRepository.save(existingCustomer);
        } else {
            throw new RuntimeException("Customer not found with ID: " + id);
        }
    }
    
    public void activateCustomer(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer existingCustomer = customer.get();
            existingCustomer.setActive(true);
            customerRepository.save(existingCustomer);
        } else {
            throw new RuntimeException("Customer not found with ID: " + id);
        }
    }
}