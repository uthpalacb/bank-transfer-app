package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("customers", bankService.searchCustomers(keyword));
        model.addAttribute("transactions", bankService.getAllTransactions());
        model.addAttribute("transferRequest", new TransferRequest());
        model.addAttribute("newCustomer", new Customer());
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("successCount", bankService.getCountByStatus(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount", bankService.getCountByStatus(TransactionStatus.FAILED));
        model.addAttribute("keyword", keyword);
        return "bank";
    }

    @PostMapping("/customers")
    public String addCustomer(@Valid @ModelAttribute("newCustomer") Customer customer,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newCustomer", result);
            redirectAttributes.addFlashAttribute("newCustomer", customer);
            return "redirect:/";
        }
        try {
            bankService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute("transferRequest") TransferRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation error in transfer form.");
            return "redirect:/";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Transfer completed successfully!");
        } catch (Exception e) {
            // Record failed transaction in a separate transaction (REQUIRES_NEW)
            try {
                auditService.recordFailure(
                        request.getFromAccount(),
                        request.getToAccount(),
                        request.getAmount(),
                        e.getMessage()
                );
            } catch (Exception auditEx) {
                // Ignore audit failure to prioritize main error response
            }
            redirectAttributes.addFlashAttribute("errorMessage", "Transfer failed: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(@PathVariable String accountNumber, Model model) {
        model.addAttribute("customer", bankService.getCustomerByAccountNumber(accountNumber));
        model.addAttribute("history", bankService.getAccountHistory(accountNumber));
        return "history";
    }
}