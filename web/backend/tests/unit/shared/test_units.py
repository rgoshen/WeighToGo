"""Unit tests for the shared weight-unit converter."""

from __future__ import annotations

from decimal import Decimal

import pytest

from weighttogo.shared.units import convert_weight

# ── lbs → kg ──────────────────────────────────────────────────────────────────


def test_convert_weight_lbs_to_kg() -> None:
    result = convert_weight(Decimal("1"), "lbs", "kg")
    assert result == pytest.approx(Decimal("0.45359237"), rel=Decimal("1e-6"))


def test_convert_weight_100_lbs_to_kg() -> None:
    result = convert_weight(Decimal("100"), "lbs", "kg")
    assert result == pytest.approx(Decimal("45.359237"), rel=Decimal("1e-6"))


# ── kg → lbs ──────────────────────────────────────────────────────────────────


def test_convert_weight_kg_to_lbs() -> None:
    result = convert_weight(Decimal("1"), "kg", "lbs")
    assert float(result) == pytest.approx(2.20462, rel=1e-4)


# ── identity (same unit) ──────────────────────────────────────────────────────


def test_convert_weight_lbs_identity() -> None:
    assert convert_weight(Decimal("175.5"), "lbs", "lbs") == Decimal("175.5")


def test_convert_weight_kg_identity() -> None:
    assert convert_weight(Decimal("80"), "kg", "kg") == Decimal("80")


# ── round-trip ────────────────────────────────────────────────────────────────


def test_convert_weight_round_trip() -> None:
    original = Decimal("200")
    converted = convert_weight(original, "lbs", "kg")
    back = convert_weight(converted, "kg", "lbs")
    assert float(back) == pytest.approx(float(original), rel=1e-6)


# ── invalid unit ──────────────────────────────────────────────────────────────


def test_convert_weight_invalid_from_unit_raises() -> None:
    with pytest.raises(ValueError, match="Unknown unit"):
        convert_weight(Decimal("100"), "stones", "kg")


def test_convert_weight_invalid_to_unit_raises() -> None:
    with pytest.raises(ValueError, match="Unknown unit"):
        convert_weight(Decimal("100"), "lbs", "pounds")
