package io.hydev.currency.exchange.controller

import io.hydev.currency.exchange.BaseIntegrationTest
import io.hydev.currency.exchange.domain.controller.AccountController
import io.hydev.currency.exchange.domain.controller.model.CreateAccountRequest
import io.hydev.currency.exchange.domain.controller.model.ExchangeCurrenciesRequest
import io.hydev.currency.exchange.domain.model.Account
import io.hydev.currency.exchange.domain.model.Account.SubAccount
import io.hydev.currency.exchange.domain.model.Currency
import io.hydev.currency.exchange.utils.IsLocalDateTimeEqual
import io.hydev.currency.exchange.utils.NumberUtils
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.apache.http.HttpStatus
import org.hamcrest.Matchers

class AccountControllerTest extends BaseIntegrationTest {

    def "[POST: /] should create account and return data"() {
        given:
        CreateAccountRequest request = CreateAccountRequest.builder()
                .ownerFirstName("Joe")
                .ownerLastName("Doe-Own")
                .initialBalanceInPln(1.23)
                .build()

        when:
        ValidatableResponse response = triggerCreateAccount(request)

        then:
        response.statusCode(HttpStatus.SC_CREATED)

        and:
        List<Account> accounts = testHelper.findAllCurrencyExchangeAccounts()
        accounts.size() == 1
        verifyCreatedAccount(accounts[0], request)

        and:
        verifyAccountResponse(response, accounts[0])
    }

    def "[POST: /] should not create account when already exists for first name and last name"() {
        given:
        Account existingAccount = testHelper.storeAccount()

        and:
        CreateAccountRequest request = CreateAccountRequest.builder()
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
        List<Account> accounts = testHelper.findAllCurrencyExchangeAccounts()
        accounts.size() == 1
        accounts[0].id == existingAccount.id
    }

    def "[GET: /] should return accounts page"() {
        given:
        Account account1 = testHelper.storeAccount(1.23, "Joe", "Doe")
        Account account2 = testHelper.storeAccount(1.23, "John", "Doey")

        when:
        ValidatableResponse response1 = triggerGetAccounts(0, 1)
        ValidatableResponse response2 = triggerGetAccounts(1, 1)
        ValidatableResponse response3 = triggerGetAccounts(2, 1)

        then:
        response1.statusCode(HttpStatus.SC_OK)
        verifyAccountsResponse(response1, [account1], 2)

        and:
        response2.statusCode(HttpStatus.SC_OK)
        verifyAccountsResponse(response2, [account2], 2)

        and:
        response3.statusCode(HttpStatus.SC_OK)
        verifyAccountsResponse(response3, [], 2)
    }

    def "[GET: /{accountId}] should return account data when exists"() {
        given:
        Account account = testHelper.storeAccount()

        when:
        ValidatableResponse response = triggerGetAccount(account.id)

        then:
        response.statusCode(HttpStatus.SC_OK)
        verifyAccountResponse(response, account)
    }

    def "[GET: /{accountId}] should return 404 when account not exists"() {
        when:
        ValidatableResponse response = triggerGetAccount(UUID.randomUUID().toString())

        then:
        response.statusCode(HttpStatus.SC_NOT_FOUND)
    }

    def "[POST: /{accountId}/exchange] should exchange when account exists and sub account amount is sufficient"() {
        given:
        BigDecimal exchangeRate = 4.05
        testHelper.storeExchangeRate(Currency.PLN, Currency.USD, exchangeRate)

        and:
        BigDecimal initialBalanceInPln = 4.05
        Account account = testHelper.storeAccount(initialBalanceInPln)

        and:
        ExchangeCurrenciesRequest request = ExchangeCurrenciesRequest.builder()
                .fromCurrency(Currency.PLN)
                .toCurrency(Currency.USD)
                .amount(amountToExchange)
                .build()

        when:
        ValidatableResponse response = triggerExchange(account.id, request)

        then:
        response.statusCode(HttpStatus.SC_OK)

        and:
        Account updatedAccount = testHelper.findCurrencyExchangeAccount(account.id)
        Map<Currency, SubAccount> subAccountsByCurrency = getSubAccountsByCurrency(updatedAccount)
        subAccountsByCurrency[Currency.PLN].amount == expectedPlnBalance
        subAccountsByCurrency[Currency.USD].amount == expectedUsdBalance

        and:
        BigDecimal totalBalance = NumberUtils.scale(
                Currency.PLN,
                subAccountsByCurrency[Currency.PLN].amount + subAccountsByCurrency[Currency.USD].amount * exchangeRate)
        totalBalance == initialBalanceInPln

        where:
        amountToExchange | expectedPlnBalance | expectedUsdBalance
        4.05             | 0.00               | 1.00
        2.00             | 2.05               | 0.4938272
        1.00             | 3.05               | 0.2469136
        0.05             | 4.00               | 0.0123457
    }

    def "[POST: /{accountId}/exchange] should return 404 when account not found"() {
        given:
        ExchangeCurrenciesRequest request = ExchangeCurrenciesRequest.builder()
                .fromCurrency(Currency.PLN)
                .toCurrency(Currency.USD)
                .amount(1.23)
                .build()

        expect:
        triggerExchange(UUID.randomUUID().toString(), request).statusCode(HttpStatus.SC_NOT_FOUND)
    }

    def "[POST: /{accountId}/exchange] should return 422 when exchange rate is not available"() {
        given:
        Account account = testHelper.storeAccount(4.05)

        and:
        ExchangeCurrenciesRequest request = ExchangeCurrenciesRequest.builder()
                .fromCurrency(Currency.PLN)
                .toCurrency(Currency.USD)
                .amount(4.05)
                .build()

        when:
        ValidatableResponse response = triggerExchange(account.id, request)

        then:
        response.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        response.body("errors", Matchers.hasSize(1))
        response.body("errors[0]", Matchers.equalTo("Exchange rate not available for PLN to USD"))
    }

    def "[POST: /{accountId}/exchange] should return 422 when exchanging to same currency"() {
        given:
        testHelper.storeExchangeRate(Currency.PLN, Currency.USD, 4.05)

        and:
        Account account = testHelper.storeAccount(4.05)

        and:
        ExchangeCurrenciesRequest request = ExchangeCurrenciesRequest.builder()
                .fromCurrency(Currency.PLN)
                .toCurrency(Currency.PLN)
                .amount(4.06)
                .build()

        when:
        ValidatableResponse response = triggerExchange(account.id, request)

        then:
        response.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        response.body("errors", Matchers.hasSize(1))
        response.body("errors[0]", Matchers.equalTo("Cannot exchange to the same currency"))
    }

    def "[POST: /{accountId}/exchange] should return 422 when sub account amount is not sufficient"() {
        given:
        testHelper.storeExchangeRate(Currency.PLN, Currency.USD, 4.05)

        and:
        Account account = testHelper.storeAccount(4.05)

        and:
        ExchangeCurrenciesRequest request = ExchangeCurrenciesRequest.builder()
                .fromCurrency(Currency.PLN)
                .toCurrency(Currency.USD)
                .amount(4.06)
                .build()

        when:
        ValidatableResponse response = triggerExchange(account.id, request)

        then:
        response.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        response.body("errors", Matchers.hasSize(1))
        response.body("errors[0]", Matchers.equalTo("Not enough funds to exchange"))
    }

    def "[POST: /{accountId}/exchange] should return 422 when amount is less than minimum required for exchange"() {
        given:
        testHelper.storeExchangeRate(Currency.PLN, Currency.USD, 4.05)

        and:
        Account account = testHelper.storeAccount(4.05)

        and:
        ExchangeCurrenciesRequest request = ExchangeCurrenciesRequest.builder()
                .fromCurrency(Currency.PLN)
                .toCurrency(Currency.USD)
                .amount(0.04)
                .build()

        when:
        ValidatableResponse response = triggerExchange(account.id, request)

        then:
        response.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        response.body("errors", Matchers.hasSize(1))
        response.body("errors[0]", Matchers.equalTo("Amount is less than required for minimum exchange"))
    }

    private static ValidatableResponse triggerGetAccounts(int pageNumber, int pageSize) {
        return RestAssured.when()
                .get("$AccountController.BASE_PATH?pageNumber=$pageNumber&pageSize=$pageSize")
                .then()
    }

    private static ValidatableResponse triggerGetAccount(String accountId) {
        return RestAssured.when()
                .get("$AccountController.BASE_PATH/$accountId")
                .then()
    }

    private ValidatableResponse triggerExchange(String accountId, ExchangeCurrenciesRequest request) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("$AccountController.BASE_PATH/$accountId/exchange")
                .then()
    }

    private ValidatableResponse triggerCreateAccount(CreateAccountRequest request) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("$AccountController.BASE_PATH")
                .then()
    }

    private static void verifyCreatedAccount(Account createdAccount, CreateAccountRequest request) {
        assert createdAccount.lockVersion != null
        assert createdAccount.ownerFirstName == request.ownerFirstName
        assert createdAccount.ownerLastName == request.ownerLastName
        assert createdAccount.createdDate != null
        assert createdAccount.lastModifiedDate != null

        Map<Currency, SubAccount> subAccountsByCurrency = getSubAccountsByCurrency(createdAccount)
        assert subAccountsByCurrency.size() == Currency.values().size()

        assert subAccountsByCurrency[Currency.PLN].lockVersion != null
        assert subAccountsByCurrency[Currency.PLN].amount == request.initialBalanceInPln
        assert subAccountsByCurrency[Currency.PLN].createdDate != null
        assert subAccountsByCurrency[Currency.PLN].lastModifiedDate != null

        assert subAccountsByCurrency[Currency.USD].lockVersion != null
        assert subAccountsByCurrency[Currency.USD].amount == 0.00
        assert subAccountsByCurrency[Currency.USD].createdDate != null
        assert subAccountsByCurrency[Currency.USD].lastModifiedDate != null
    }

    private static void verifyAccountsResponse(ValidatableResponse response,
                                               List<Account> expectedAccounts,
                                               int expectedTotalElements) {
        response.body("content", Matchers.hasSize(expectedAccounts.size()))
        response.body('total_elements', Matchers.equalTo(expectedTotalElements))
        expectedAccounts.eachWithIndex { Account expectedAccount, int index ->
            verifyAccountResponse(response, expectedAccount, "content[$index].")
        }
    }

    private static void verifyAccountResponse(ValidatableResponse response, Account expectedAccount, String prefix = "") {
        response.body("${prefix}id", Matchers.equalTo(expectedAccount.id))
        response.body("${prefix}id", Matchers.equalTo(expectedAccount.id))
        response.body("${prefix}owner_first_name", Matchers.equalTo(expectedAccount.ownerFirstName))
        response.body("${prefix}owner_last_name", Matchers.equalTo(expectedAccount.ownerLastName))
        response.body("${prefix}created_date", IsLocalDateTimeEqual.from(expectedAccount.createdDate))
        response.body("${prefix}last_modified_date", IsLocalDateTimeEqual.from(expectedAccount.lastModifiedDate))

        Map<Currency, SubAccount> subAccountsByCurrency = getSubAccountsByCurrency(expectedAccount)
        Currency.values()
                .sort()
                .eachWithIndex { Currency currency, int index ->
                    SubAccount expectedSubAccount = subAccountsByCurrency[currency]
                    BigDecimal expectedAmount = NumberUtils.scale(expectedSubAccount.currency, expectedSubAccount.amount)
                    response.body("${prefix}sub_accounts[$index].id", Matchers.equalTo(expectedSubAccount.id))
                    response.body("${prefix}sub_accounts[$index].currency", Matchers.equalTo(expectedSubAccount.currency.toString()))
                    response.body("${prefix}sub_accounts[$index].amount", Matchers.equalTo(expectedAmount.floatValue()))
                    response.body("${prefix}sub_accounts[$index].created_date", IsLocalDateTimeEqual.from(expectedSubAccount.createdDate))
                    response.body("${prefix}sub_accounts[$index].last_modified_date", IsLocalDateTimeEqual.from(expectedSubAccount.lastModifiedDate))
                }
    }

    private static Map<Currency, SubAccount> getSubAccountsByCurrency(Account account) {
        return account.subAccounts.collectEntries() { [it.currency, it] }
    }
}
