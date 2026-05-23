"""Unit tests for the weight-entry list cursor codec (ADR-0015).

The cursor is a base64url-no-padding encoding of "YYYY-MM-DD:N", where
the date is the observation_date of the last returned entry and N is its
entry_id. Decoding malformed input must raise a single, typed error so
the router can translate it to RFC 7807 422.
"""

from __future__ import annotations

from datetime import date

import pytest

from weighttogo.weight_tracking.interface.cursor import (
    InvalidCursorError,
    decode_cursor,
    encode_cursor,
)


def test_encode_then_decode_round_trips_exact_values() -> None:
    encoded = encode_cursor(date(2026, 5, 20), 17)
    decoded_date, decoded_id = decode_cursor(encoded)
    assert decoded_date == date(2026, 5, 20)
    assert decoded_id == 17


def test_encode_returns_opaque_string_without_colon_or_date_literal() -> None:
    """Cursor must be opaque on the wire — the plaintext shape should not
    leak through, so clients are discouraged from constructing cursors."""
    encoded = encode_cursor(date(2026, 5, 20), 17)
    assert ":" not in encoded
    assert "2026" not in encoded


def test_encode_omits_base64_padding() -> None:
    """base64url-no-padding for cleaner URLs and idempotent round-trips."""
    encoded = encode_cursor(date(2026, 5, 20), 17)
    assert "=" not in encoded


def test_decode_rejects_empty_string() -> None:
    with pytest.raises(InvalidCursorError):
        decode_cursor("")


def test_decode_rejects_non_base64_input() -> None:
    with pytest.raises(InvalidCursorError):
        decode_cursor("!!!not-base64!!!")


def test_decode_rejects_payload_without_colon() -> None:
    """Decoded payload missing the date:id separator is malformed."""
    import base64

    bad = base64.urlsafe_b64encode(b"2026-05-20").rstrip(b"=").decode("ascii")
    with pytest.raises(InvalidCursorError):
        decode_cursor(bad)


def test_decode_rejects_payload_with_invalid_date() -> None:
    import base64

    bad = base64.urlsafe_b64encode(b"2026-13-99:17").rstrip(b"=").decode("ascii")
    with pytest.raises(InvalidCursorError):
        decode_cursor(bad)


def test_decode_rejects_payload_with_non_integer_id() -> None:
    import base64

    bad = base64.urlsafe_b64encode(b"2026-05-20:abc").rstrip(b"=").decode("ascii")
    with pytest.raises(InvalidCursorError):
        decode_cursor(bad)


def test_decode_rejects_payload_with_negative_id() -> None:
    """entry_id values are positive surrogate keys; a negative id is malformed."""
    import base64

    bad = base64.urlsafe_b64encode(b"2026-05-20:-1").rstrip(b"=").decode("ascii")
    with pytest.raises(InvalidCursorError):
        decode_cursor(bad)
