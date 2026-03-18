package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpressionTest {
    // region parse roundtrip

    @Test
    fun parseSimpleAddition() = assertRoundtrip("1+2")

    @Test
    fun parseOperatorPrecedence() = assertRoundtrip("1+2*3")

    @Test
    fun parseUnaryMinus() = assertRoundtrip("-1")

    @Test
    fun parseUnaryMinusOnGroup() = assertRoundtrip("-(1+2)")

    @Test
    fun parseDivideGroup() = assertRoundtrip("1/(2+3)")

    @Test
    fun parseNestedBinary() = assertRoundtrip("(1+2)*(3+4)")

    @Test
    fun parseLeftAssociativeSubtraction() = assertRoundtrip("1-2-3")

    @Test
    fun parseLeftAssociativeDivision() = assertRoundtrip("8/4/2")

    // endregion

    // region parse structure

    @Test
    fun precedenceParsedAsExpected() {
        val expression = Expression.parseOrNull("2+3*4")
        val expected =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(2)),
                argument2 =
                    Expression.BinaryOperation(
                        argument1 = Expression.Value(BigDecimal.fromInt(3)),
                        argument2 = Expression.Value(BigDecimal.fromInt(4)),
                        type = Expression.BinaryOperation.Type.Times,
                    ),
                type = Expression.BinaryOperation.Type.Plus,
            )
        assertEquals(expected = expected, actual = expression)
    }

    @Test
    fun leftAssociativitySubtraction() {
        val expression = Expression.parseOrNull("1-2-3")
        val expected =
            Expression.BinaryOperation(
                argument1 =
                    Expression.BinaryOperation(
                        argument1 = Expression.Value(BigDecimal.fromInt(1)),
                        argument2 = Expression.Value(BigDecimal.fromInt(2)),
                        type = Expression.BinaryOperation.Type.Minus,
                    ),
                argument2 = Expression.Value(BigDecimal.fromInt(3)),
                type = Expression.BinaryOperation.Type.Minus,
            )
        assertEquals(expected = expected, actual = expression)
    }

    // endregion

    // region parse invalid

    @Test
    fun parseEmptyStringIsNull() = assertParseNull("")

    @Test
    fun parseAlphaIsNull() = assertParseNull("abc")

    @Test
    fun parseTrailingOperatorIsNull() = assertParseNull("1/")

    @Test
    fun parseDivisionByZeroIsNull() = assertParseNull("1/0")

    @Test
    fun parseDivisionByZeroExpressionIsNull() = assertParseNull("6/(3-3)")

    @Test
    fun parseUnclosedParenIsNull() = assertParseNull("(1+2")

    // endregion

    // region serialize minimal parens

    @Test
    fun serializeNoUnnecessaryParensForPrecedence() {
        val expression =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(1)),
                argument2 =
                    Expression.BinaryOperation(
                        argument1 = Expression.Value(BigDecimal.fromInt(2)),
                        argument2 = Expression.Value(BigDecimal.fromInt(3)),
                        type = Expression.BinaryOperation.Type.Times,
                    ),
                type = Expression.BinaryOperation.Type.Plus,
            )
        assertEquals(expected = "1+2*3", actual = expression.serialize())
    }

    @Test
    fun serializeParensForLowPriorityRightChild() {
        val expression =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(1)),
                argument2 =
                    Expression.BinaryOperation(
                        argument1 = Expression.Value(BigDecimal.fromInt(2)),
                        argument2 = Expression.Value(BigDecimal.fromInt(3)),
                        type = Expression.BinaryOperation.Type.Plus,
                    ),
                type = Expression.BinaryOperation.Type.Times,
            )
        assertEquals(expected = "1*(2+3)", actual = expression.serialize())
    }

    @Test
    fun serializeParensForSamePriorityRightChildSubtraction() {
        val expression =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(1)),
                argument2 =
                    Expression.BinaryOperation(
                        argument1 = Expression.Value(BigDecimal.fromInt(2)),
                        argument2 = Expression.Value(BigDecimal.fromInt(3)),
                        type = Expression.BinaryOperation.Type.Minus,
                    ),
                type = Expression.BinaryOperation.Type.Minus,
            )
        assertEquals(expected = "1-(2-3)", actual = expression.serialize())
    }

    @Test
    fun serializeUnaryMinusValue() {
        val expression =
            Expression.UnaryOperation(
                argument = Expression.Value(BigDecimal.fromInt(1)),
                type = Expression.UnaryOperation.Type.Minus,
            )
        assertEquals(expected = "-1", actual = expression.serialize())
    }

    @Test
    fun serializeUnaryMinusBinaryWrapsInParens() {
        val expression =
            Expression.UnaryOperation(
                argument =
                    Expression.BinaryOperation(
                        argument1 = Expression.Value(BigDecimal.fromInt(1)),
                        argument2 = Expression.Value(BigDecimal.fromInt(2)),
                        type = Expression.BinaryOperation.Type.Plus,
                    ),
                type = Expression.UnaryOperation.Type.Minus,
            )
        assertEquals(expected = "-(1+2)", actual = expression.serialize())
    }

    // endregion

    // region evaluate

    @Test
    fun evaluateAdditionWithPrecedence() {
        val expression = Expression.parseOrNull("1+2*3")!!
        assertEquals(
            expected = BigDecimal.fromInt(7),
            actual = expression.evaluate(DecimalMode.DEFAULT),
        )
    }

    @Test
    fun evaluateUnaryMinus() {
        val expression = Expression.parseOrNull("-5")!!
        assertEquals(
            expected = BigDecimal.fromInt(-5),
            actual = expression.evaluate(DecimalMode.DEFAULT),
        )
    }

    @Test
    fun evaluateOrNullDivisionByZeroIsNull() {
        val expression =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(1)),
                argument2 = Expression.Value(BigDecimal.ZERO),
                type = Expression.BinaryOperation.Type.Divide,
            )
        assertNull(expression.evaluateOrNull(decimalMode = null))
    }

    // endregion

    // region serialize unary inside binary (potential bugs)

    @Test
    fun serializeUnaryMinusAsRightChildOfTimes() {
        // Times(2, UnaryOperation(3)) — должен сериализоваться как 2*(-3), не 2*-3
        val expression =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(2)),
                argument2 =
                    Expression.UnaryOperation(
                        argument = Expression.Value(BigDecimal.fromInt(3)),
                        type = Expression.UnaryOperation.Type.Minus,
                    ),
                type = Expression.BinaryOperation.Type.Times,
            )
        assertRoundtripExpression(expression)
    }

    @Test
    fun serializeUnaryMinusAsRightChildOfDivide() {
        // Divide(1, UnaryOperation(2)) — должен сериализоваться как 1/(-2), не 1/-2
        val expression =
            Expression.BinaryOperation(
                argument1 = Expression.Value(BigDecimal.fromInt(1)),
                argument2 =
                    Expression.UnaryOperation(
                        argument = Expression.Value(BigDecimal.fromInt(2)),
                        type = Expression.UnaryOperation.Type.Minus,
                    ),
                type = Expression.BinaryOperation.Type.Divide,
            )
        assertRoundtripExpression(expression)
    }

    @Test
    fun serializeUnaryMinusAsLeftChildOfTimes() {
        // Times(UnaryOperation(2), 3) — должен сериализоваться как -2*3 (без скобок)
        val expression =
            Expression.BinaryOperation(
                argument1 =
                    Expression.UnaryOperation(
                        argument = Expression.Value(BigDecimal.fromInt(2)),
                        type = Expression.UnaryOperation.Type.Minus,
                    ),
                argument2 = Expression.Value(BigDecimal.fromInt(3)),
                type = Expression.BinaryOperation.Type.Times,
            )
        assertRoundtripExpression(expression)
    }

    @Test
    fun parseRoundtripUnaryInMultiplication() = assertRoundtrip("2*(-3)")

    @Test
    fun parseRoundtripUnaryInDivision() = assertRoundtrip("1/(-2)")

    // endregion

    // region helpers

    private fun assertRoundtrip(input: String) {
        val expression = Expression.parseOrNull(input)
        val serialized = expression?.serialize()
        assertEquals(expected = input, actual = serialized)
    }

    private fun assertParseNull(input: String) = assertNull(Expression.parseOrNull(input))

    private fun assertRoundtripExpression(expression: Expression) {
        val serialized = expression.serialize()
        val reparsed = Expression.parseOrNull(serialized)
        assertEquals(expected = expression, actual = reparsed)
    }

    // endregion
}
