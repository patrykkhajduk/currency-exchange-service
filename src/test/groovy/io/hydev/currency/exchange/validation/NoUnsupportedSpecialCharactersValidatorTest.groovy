package io.hydev.currency.exchange.validation

import jakarta.validation.ConstraintValidatorContext
import spock.lang.Specification
import spock.lang.Subject

class NoUnsupportedSpecialCharactersValidatorTest extends Specification {

    @Subject
    NoUnsupportedSpecialCharactersValidator validator = new NoUnsupportedSpecialCharactersValidator()

    def "isValid should pass null value"() {
        expect:
        validator.isValid(null, Mock(ConstraintValidatorContext))
    }

    def "isValid should return false when value has invalid characters"() {
        expect:
        !validator.isValid(value, Mock(ConstraintValidatorContext))

        where:
        value << [
                "Al@",
                "test@test.io",
                " .,@_- ",
                "<script>alert<script>",
                "select * from x",
                "x.a()",
                "x?a",
                "x!a",
                "¿Qué pasa?"
        ]
    }

    def "isValid should return true when value has no invalid characters"() {
        expect:
        validator.isValid(value, Mock(ConstraintValidatorContext))

        where:
        value << [
                "abcABC-adada",
                "ąćęłóśżźĄĆĘŁÓŚŻŹ",
                "Qué-pasa",
                "schön",
        ]
    }
}
