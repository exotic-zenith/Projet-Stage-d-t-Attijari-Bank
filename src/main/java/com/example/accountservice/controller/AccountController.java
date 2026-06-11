package com.example.accountservice.controller;

import com.example.accountservice.model.Account;
import com.example.accountservice.service.AccountNotFoundException;
import com.example.accountservice.service.AccountService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<Account> listAccounts() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        Account saved = service.create(account);
        return ResponseEntity.created(URI.create("/api/accounts/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable Long id, @Valid @RequestBody Account account) {
        return service.update(id, account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        if (service.findById(id).isEmpty()) {
            throw new AccountNotFoundException(id);
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Account> search(@RequestParam String name) {
        return service.searchByHolderName(name);
    }

}
