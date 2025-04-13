package com.hmshk.controller;

import com.hmshk.model.Customer;
import com.hmshk.model.Facility;
import com.hmshk.service.CustomerService;
import com.hmshk.service.FacilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    private final CustomerService customerService;
    private final FacilityService facilityService;
    
    public ApiController(CustomerService customerService, FacilityService facilityService) {
        this.customerService = customerService;
        this.facilityService = facilityService;
    }
    
    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacility(@PathVariable Long id) {
        return facilityService.getFacilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}