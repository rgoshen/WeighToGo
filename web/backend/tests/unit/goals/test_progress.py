"""Unit tests for the calculate_progress pure function (FR-G-2)."""

from __future__ import annotations

from decimal import Decimal

import pytest


def _prog(start: str, current: str, target: str) -> float:
    from weighttogo.goals.domain.progress import calculate_progress

    result = calculate_progress(
        start=Decimal(start),
        current=Decimal(current),
        target=Decimal(target),
    )
    return result.percent


# ── at-start (0%) ─────────────────────────────────────────────────────────────


def test_calculate_progress_at_start_is_zero() -> None:
    assert _prog("200", "200", "150") == pytest.approx(0.0)


# ── at-target (100%) ──────────────────────────────────────────────────────────


def test_calculate_progress_at_target_is_one_hundred() -> None:
    assert _prog("200", "150", "150") == pytest.approx(100.0)


# ── halfway (50%) ─────────────────────────────────────────────────────────────


def test_calculate_progress_halfway_is_fifty() -> None:
    assert _prog("200", "175", "150") == pytest.approx(50.0)


# ── past-target clamps to 100% ────────────────────────────────────────────────


def test_calculate_progress_past_target_clamps_to_one_hundred() -> None:
    assert _prog("200", "140", "150") == pytest.approx(100.0)


# ── wrong direction clamps to 0% ─────────────────────────────────────────────


def test_calculate_progress_wrong_direction_clamps_to_zero() -> None:
    # User gaining weight on a lose goal → negative raw → clamp to 0
    assert _prog("200", "210", "150") == pytest.approx(0.0)


# ── gain goal works symmetrically ─────────────────────────────────────────────


def test_calculate_progress_gain_goal_halfway() -> None:
    # start=150 target=200: gained 25 of 50 = 50%
    assert _prog("150", "175", "200") == pytest.approx(50.0)


def test_calculate_progress_gain_goal_at_target() -> None:
    assert _prog("150", "200", "200") == pytest.approx(100.0)


# ── zero denominator guard ────────────────────────────────────────────────────


def test_calculate_progress_zero_denominator_returns_zero() -> None:
    # start == target (prevented by validation, but domain must be safe)
    assert _prog("200", "200", "200") == pytest.approx(0.0)
