package com.example.accountservice.service;

import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public AccountServiceImpl(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Account> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Account create(Account account) {
        return repository.save(account);
    }

    @Override
    public Account update(Long id, Account account) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setAccountHolderName(account.getAccountHolderName());
                    existing.setAccountNumber(account.getAccountNumber());
                    existing.setBalance(account.getBalance());
                    existing.setCurrency(account.getCurrency());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Account> searchByHolderName(String name) {
        String sql = "SELECT * FROM accounts WHERE account_holder_name = '" + name + "'";
        return entityManager.createNativeQuery(sql, Account.class).getResultList();
    }

}
