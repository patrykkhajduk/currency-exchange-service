package io.hydev.currency.exchange.controller

import io.hydev.currency.exchange.BaseIntegrationTest
import io.hydev.currency.exchange.controller.model.CreateCurrencyExchangeAccountRequest
import io.hydev.currency.exchange.domain.model.Currency
import io.hydev.currency.exchange.domain.model.CurrencyExchangeAccount
import io.hydev.currency.exchange.domain.model.CurrencyExchangeAccount.CurrencyExchangeAccountBalance
import io.hydev.currency.exchange.domain.model.repository.CurrencyExchangeAccountRepository
import io.hydev.currency.exchange.utils.IsLocalDateTimeEqual
import io.hydev.currency.exchange.utils.NumberUtils
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

class CurrencyExchangeAccountControllerTest extends BaseIntegrationTest {

    @Autowired
    private CurrencyExchangeAccountRepository currencyExchangeAccountRepository

    def setup() {
        currencyExchangeAccountRepository.deleteAll()
    }

    def "[POST: /] should create account and return data"() {
        given:
        CreateCurrencyExchangeAccountRequest request = CreateCurrencyExchangeAccountRequest.builder()
                .ownerFirstName("Joe")
                .ownerLastName("Doe")
                .initialBalanceInPln(1.23)
                .build()

        when:
        ValidatableResponse response = triggerCreateAccount(request)

        then:
        response.statusCode(HttpStatus.SC_CREATED)

        and:
        List<CurrencyExchangeAccount> accounts = currencyExchangeAccountRepository.findAll()
        accounts.size() == 1
        verifyCreatedAccount(accounts[0], request)

        and:
        verifyAccountResponse(response, accounts[0])
    }

    def "[POST: /] should not create account when already exists for first name and last name"() {
        given:
        CurrencyExchangeAccount existingAccount = storeAccount()

        and:
        CreateCurrencyExchangeAccountRequest request = CreateCurrencyExchangeAccountRequest.builder()
                .ownerFirstName(existingAccount.ownerFirstName)
                .ownerLastName(existingAccount.ownerLastName)
                .initialBalanceInPln(1.23)
                .build()

        when:
        ValidatableResponse response = triggerCreateAccount(request)

        then:
        response.statusCode(HttpStatus.SC_BAD_REQUEST)
        response.body("errors", Matchers.hasSize(1))
        response.body("errors[0]", Matchers.equalTo("Account with this owner already exists"))

        and:
        List<CurrencyExchangeAccount> accounts = currencyExchangeAccountRepository.findAll()
        accounts.size() == 1
        accounts[0].id == existingAccount.id
    }

    def "[GET: /{accountId}] should return account data when exists"() {
        given:
        CurrencyExchangeAccount account = storeAccount()

        when:
        ValidatableResponse response = triggerGetAccount(account.id)

        then:
        response.statusCode(HttpStatus.SC_OK)
        verifyAccountResponse(response, account)
    }

    def "[GET: /{accountId}] should return 404 when account not exists"() {
        when:
        ValidatableResponse response = triggerGetAccount("non-existing-id")

        then:
        response.statusCode(HttpStatus.SC_NOT_FOUND)
    }

    private CurrencyExchangeAccount storeAccount(BigDecimal initialBalanceInPln = 1.23, String ownerFirstName = "Joe", String ownerLastName = "Doe") {
        return currencyExchangeAccountRepository.save(
                new CurrencyExchangeAccount(ownerFirstName, ownerLastName, Currency.PLN, initialBalanceInPln))
    }

    private static ValidatableResponse triggerGetAccount(String accountId) {
        return RestAssured.when()
                .get("$CurrencyExchangeAccountController.BASE_PATH/$accountId")
                .then()
    }

    private ValidatableResponse triggerCreateAccount(CreateCurrencyExchangeAccountRequest request) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("$CurrencyExchangeAccountController.BASE_PATH")
                .then()
    }

    private static void verifyCreatedAccount(CurrencyExchangeAccount createdAccount, CreateCurrencyExchangeAccountRequest request) {
        assert createdAccount.ownerFirstName == request.ownerFirstName
        assert createdAccount.ownerLastName == request.ownerLastName
        assert createdAccount.createdDate != null
        assert createdAccount.lastModifiedDate != null

        Map<Currency, CurrencyExchangeAccountBalance> balances = getBalancesByCurrency(createdAccount)
        assert balances.size() == Currency.values().size()

        assert balances[Currency.PLN].amount == request.initialBalanceInPln
        assert balances[Currency.PLN].createdDate != null
        assert balances[Currency.PLN].lastModifiedDate != null

        assert balances[Currency.USD].amount == NumberUtils.getScaledDecimal()
        assert balances[Currency.USD].createdDate != null
        assert balances[Currency.USD].lastModifiedDate != null
    }

    private static void verifyAccountResponse(ValidatableResponse response, CurrencyExchangeAccount expectedAccount) {
        response.body("id", Matchers.equalTo(expectedAccount.id))
        response.body("owner_first_name", Matchers.equalTo(expectedAccount.ownerFirstName))
        response.body("owner_last_name", Matchers.equalTo(expectedAccount.ownerLastName))
        response.body("created_date", IsLocalDateTimeEqual.from(expectedAccount.createdDate))
        response.body("last_modified_date", IsLocalDateTimeEqual.from(expectedAccount.lastModifiedDate))

        Map<Currency, CurrencyExchangeAccountBalance> balances = getBalancesByCurrency(expectedAccount)
        Currency.values()
                .sort()
                .eachWithIndex { Currency currency, int index ->
                    CurrencyExchangeAccountBalance expectedBalance = balances[currency]
                    response.body("balances[$index].id", Matchers.equalTo(expectedBalance.id))
                    response.body("balances[$index].currency", Matchers.equalTo(expectedBalance.currency.toString()))
                    response.body("balances[$index].amount", Matchers.equalTo(expectedBalance.amount.floatValue()))
                    response.body("balances[$index].created_date", IsLocalDateTimeEqual.from(expectedBalance.createdDate))
                    response.body("balances[$index].last_modified_date", IsLocalDateTimeEqual.from(expectedBalance.lastModifiedDate))
                }
    }

    private static Map<Currency, CurrencyExchangeAccountBalance> getBalancesByCurrency(CurrencyExchangeAccount account) {
        return account.balances.collectEntries() { [it.currency, it] }
    }
}
