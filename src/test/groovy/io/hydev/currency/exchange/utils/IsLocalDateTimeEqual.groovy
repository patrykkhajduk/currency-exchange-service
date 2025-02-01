package io.hydev.currency.exchange.utils


import org.hamcrest.BaseMatcher
import org.hamcrest.Description

import java.time.LocalDateTime

class IsLocalDateTimeEqual extends BaseMatcher<LocalDateTime> {

    static IsLocalDateTimeEqual from(LocalDateTime expectedValue) {
        return new IsLocalDateTimeEqual(expectedValue)
    }

    private final LocalDateTime expectedValue

    IsLocalDateTimeEqual(LocalDateTime expectedValue) {
        this.expectedValue = expectedValue
    }

    @Override
    boolean matches(Object o) {
        if (o == null || expectedValue == null) {
            return o == expectedValue
        } else if (o instanceof LocalDateTime) {
            return Objects.equals(expectedValue, o)
        } else {
            //remove tailing zeros when comparing to string local date time that is from HTTP response body
            String expectedValueWithoutTailingZeros = expectedValue.toString().replaceAll('0*$', "")
            return Objects.equals(expectedValueWithoutTailingZeros, o.toString())
        }
    }

    @Override
    void describeTo(Description description) {
        description.appendValue(expectedValue)
    }
}
