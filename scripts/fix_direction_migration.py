#!/usr/bin/env python3
"""Migrate PinFin budget file: bind categories to amount direction.

For every entry record:
  * amount < 0  -> category gets "-" prefix, amount is inverted to be positive
  * amount >= 0 -> category gets "+" prefix, amount stays as-is
  * "0" / "-0"  -> "+" category, amount normalized to "0"

category_config ids are updated accordingly; categories used in both
directions are split into two config lines.

Usage:
    python3 fix_direction_migration.py <budget-file>

Writes the corrected copy next to the input as "<budget-file>.fixed".
"""

import re
import sys
from fractions import Fraction

RECORD_RE = re.compile(r'\{"category":"([^"]*)","amount":"([^"]*)"')
CATEGORY_CONFIG_ID_RE = re.compile(r'"type":"category_config","id":"([^"]*)"')


class ExpressionError(ValueError):
    pass


class Parser:
    def __init__(self, source):
        self.source = source
        self.pos = 0

    def parse(self):
        expr = self._parse_add_sub()
        self._skip_ws()
        if self.pos != len(self.source):
            raise ExpressionError("unexpected trailing characters")
        return expr

    def _skip_ws(self):
        while self.pos < len(self.source) and self.source[self.pos].isspace():
            self.pos += 1

    def _peek(self):
        return self.source[self.pos] if self.pos < len(self.source) else None

    def _parse_add_sub(self):
        left = self._parse_mul_div()
        while True:
            self._skip_ws()
            ch = self._peek()
            if ch in ("+", "-"):
                self.pos += 1
                right = self._parse_mul_div()
                left = left + right if ch == "+" else left - right
            else:
                break
        return left

    def _parse_mul_div(self):
        left = self._parse_unary()
        while True:
            self._skip_ws()
            ch = self._peek()
            if ch in ("*", "/"):
                self.pos += 1
                right = self._parse_unary()
                if ch == "*":
                    left = left * right
                else:
                    if right == 0:
                        raise ExpressionError("division by zero")
                    left = left / right
            else:
                break
        return left

    def _parse_unary(self):
        self._skip_ws()
        ch = self._peek()
        if ch == "-":
            self.pos += 1
            return -self._parse_unary()
        if ch == "+":
            self.pos += 1
            return self._parse_unary()
        return self._parse_primary()

    def _parse_primary(self):
        self._skip_ws()
        ch = self._peek()
        if ch == "(":
            self.pos += 1
            expr = self._parse_add_sub()
            self._skip_ws()
            if self._peek() != ")":
                raise ExpressionError("expected ')'")
            self.pos += 1
            return expr
        if ch is not None and (ch.isdigit() or ch == "."):
            return self._parse_number()
        raise ExpressionError("unexpected character: %r" % ch)

    def _parse_number(self):
        start = self.pos
        while self.pos < len(self.source) and (
            self.source[self.pos].isdigit() or self.source[self.pos] == "."
        ):
            self.pos += 1
        raw = self.source[start:self.pos]
        try:
            return Fraction(raw)
        except (ValueError, ZeroDivisionError):
            raise ExpressionError("invalid number: %r" % raw)


def evaluate(amount):
    return Parser(amount.strip()).parse()


def negate(amount):
    s = amount.strip()
    if s.startswith("-"):
        rest = s[1:]
        if rest.startswith("(") and rest.endswith(")"):
            rest = rest[1:-1]
        return rest
    return "-(" + s + ")"


def process_record(match):
    category = match.group(1)
    amount = match.group(2)
    try:
        value = evaluate(amount)
    except ExpressionError as exc:
        print("ERROR: cannot evaluate amount %r: %s" % (amount, exc), file=sys.stderr)
        raise

    if value < 0:
        direction = "-"
        new_amount = negate(amount)
    else:
        direction = "+"
        new_amount = "0" if amount.strip().startswith("-") else amount

    directions.setdefault(category, set()).add(direction)
    return '{"category":"%s%s","amount":"%s"' % (direction, category, new_amount)


def process_line(line):
    if '"type":"transaction"' in line and '"type":{"type":"entry"' in line:
        return [RECORD_RE.sub(process_record, line)]

    if '"type":"category_config"' in line:
        m = CATEGORY_CONFIG_ID_RE.search(line)
        if not m:
            return [line]
        category = m.group(1)
        seen = directions.get(category)
        if seen is None:
            print("WARNING: category %r has no transactions, left unchanged" % category,
                  file=sys.stderr)
            return [line]
        result = []
        for direction in sorted(seen):
            result.append(
                line.replace(
                    '"id":"%s"' % category,
                    '"id":"%s%s"' % (direction, category),
                    1,
                )
            )
        return result

    return [line]


def main():
    if len(sys.argv) != 2:
        print("Usage: python3 fix_direction_migration.py <budget-file>", file=sys.stderr)
        sys.exit(1)

    input_path = sys.argv[1]
    output_path = input_path + ".fixed"

    with open(input_path, "r", encoding="utf-8") as f:
        lines = f.read().splitlines()

    for line in lines:
        if '"type":"transaction"' in line and '"type":{"type":"entry"' in line:
            for m in RECORD_RE.finditer(line):
                process_record(m)

    out_lines = []
    for line in lines:
        out_lines.extend(process_line(line))

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("\n".join(out_lines))
        f.write("\n")

    print("Wrote %s (%d lines -> %d lines)" % (output_path, len(lines), len(out_lines)))


directions = {}


if __name__ == "__main__":
    main()
