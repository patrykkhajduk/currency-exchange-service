package io.hydev.currency.exchange.domain.model.repository;

import io.hydev.currency.exchange.domain.exception.AccountNotFoundException;
import io.hydev.currency.exchange.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

    default Account getById(String id) {
        return findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    boolean existsByOwnerFirstNameAndOwnerLastName(String ownerFirstName, String ownerLastName);
}
