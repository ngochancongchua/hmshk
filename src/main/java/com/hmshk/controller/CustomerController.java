package com.hmshk.controller;

import com.hmshk.model.Customer;
import com.hmshk.service.CustomerService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CustomerController {
    
    
    private final CustomerService customerService;
    
    @GetMapping("/customers")
    public String viewCustomers(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customer";
    }
    
    @GetMapping("/customers/{id}")
    public String viewCustomerDetails(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Customer> customer = customerService.getCustomerById(id);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            return "customer-details";
        } else {
            return "redirect:/customers";
        }
    }
    
    @GetMapping("/customers/new")
    public String showNewCustomerForm(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("customer", new Customer());
        model.addAttribute("idTypes", new String[]{"Passport", "National ID", "Driver's License", "Other"});
        model.addAttribute("vipStatuses", new String[]{"Regular", "Silver", "Gold", "Platinum"});
        return "customer-form";
    }
    
    @GetMapping("/customers/edit/{id}")
    public String showEditCustomerForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Customer> customer = customerService.getCustomerById(id);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            model.addAttribute("idTypes", new String[]{"Passport", "National ID", "Driver's License", "Other"});
            model.addAttribute("vipStatuses", new String[]{"Regular", "Silver", "Gold", "Platinum"});
            return "customer-form";
        } else {
            return "redirect:/customers";
        }
    }
    
    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = customer.getId() == null;
            Customer savedCustomer = customerService.saveCustomer(customer);
            redirectAttributes.addFlashAttribute("success", 
                (isNew ? "Customer added successfully" : "Customer updated successfully"));
            return "redirect:/customers/" + savedCustomer.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers";
        }
    }
    
    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }
    
    @GetMapping("/customers/status/{id}")
    public String toggleCustomerStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Customer> customer = customerService.getCustomerById(id);
            if (customer.isPresent()) {
                if (customer.get().isActive()) {
                    customerService.deactivateCustomer(id);
                    redirectAttributes.addFlashAttribute("success", "Customer deactivated successfully");
                } else {
                    customerService.activateCustomer(id);
                    redirectAttributes.addFlashAttribute("success", "Customer activated successfully");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }
    
    @GetMapping("/customers/search")
    public String searchCustomers(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) String vipStatus,
                                  Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Customer> customers;
        
        if (name != null && !name.isEmpty()) {
            customers = customerService.searchCustomersByName(name);
            model.addAttribute("searchType", "name");
            model.addAttribute("searchTerm", name);
        } else if (phone != null && !phone.isEmpty()) {
            customers = customerService.searchCustomersByPhone(phone);
            model.addAttribute("searchType", "phone");
            model.addAttribute("searchTerm", phone);
        } else if (vipStatus != null && !vipStatus.isEmpty()) {
            customers = customerService.getCustomersByVipStatus(vipStatus);
            model.addAttribute("searchType", "vipStatus");
            model.addAttribute("searchTerm", vipStatus);
        } else {
            customers = customerService.getAllCustomers();
        }
        
        model.addAttribute("customers", customers);
        model.addAttribute("vipStatuses", new String[]{"Regular", "Silver", "Gold", "Platinum"});
        return "customer";
    }
}