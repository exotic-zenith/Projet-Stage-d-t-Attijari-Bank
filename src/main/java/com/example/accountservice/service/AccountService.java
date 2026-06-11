package com.example.accountservice.service;

import com.example.accountservice.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> findAll();

    Optional<Account> findById(Long id);

    Account create(Account account);

    Account update(Long id, Account account);

    void deleteById(Long id);

    List<Account> searchByHolderName(String name);
}
