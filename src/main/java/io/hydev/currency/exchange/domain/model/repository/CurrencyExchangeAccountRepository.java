package io.hydev.currency.exchange.domain.model.repository;

import io.hydev.currency.exchange.domain.model.CurrencyExchangeAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyExchangeAccountRepository extends JpaRepository<CurrencyExchangeAccount, String> {

    boolean existsByOwnerFirstNameAndOwnerLastName(String ownerFirstName, String ownerLastName);
}
