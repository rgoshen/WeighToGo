"""Shared weight-unit conversion utilities.

Pure functions — no framework or persistence dependencies.  Re-used by:
- ``goals.application.get_active_goal_with_progress`` (converts the latest
  weight entry into the goal's unit before computing progress).
- ``weight_tracking`` Step 5 display formatting (FR-W-6), which will import
  this module rather than reimplement the converter.
"""

from __future__ import annotations

from decimal import Decimal

# 1 pound expressed in kilograms (exact NIST value)
_LBS_TO_KG = Decimal("0.45359237")
_KG_TO_LBS = Decimal("1") / _LBS_TO_KG


def convert_weight(value: Decimal, from_unit: str, to_unit: str) -> Decimal:
    """Convert *value* from *from_unit* to *to_unit*.

    Complexity: O(1) time, O(1) space.

    Args:
        value: The weight value to convert.
        from_unit: The source unit — either ``'lbs'`` or ``'kg'``.
        to_unit: The target unit — either ``'lbs'`` or ``'kg'``.

    Returns:
        The converted weight as a ``Decimal``.

    Raises:
        ValueError: When *from_unit* or *to_unit* is not ``'lbs'`` or ``'kg'``.
    """
    valid = {"lbs", "kg"}
    if from_unit not in valid:
        raise ValueError(f"Unknown unit: {from_unit!r}. Must be one of {valid}.")
    if to_unit not in valid:
        raise ValueError(f"Unknown unit: {to_unit!r}. Must be one of {valid}.")

    if from_unit == to_unit:
        return value
    if from_unit == "lbs":
        return value * _LBS_TO_KG
    return value * _KG_TO_LBS
