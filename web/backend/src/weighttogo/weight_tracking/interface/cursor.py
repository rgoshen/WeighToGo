"""Opaque compound-cursor codec for weight-entry list pagination (ADR-0015).

Encodes ``(observation_date, entry_id)`` as a base64url-no-padding string so
the wire format mirrors the SQL sort key. The use case and repository talk
in domain terms (``tuple[date, int]``); only the router crosses this
boundary, so the codec lives in the interface layer.
"""

from __future__ import annotations

import base64
import binascii
from datetime import date


class InvalidCursorError(ValueError):
    """Raised when a cursor string cannot be decoded.

    Routers translate this to RFC 7807 422.
    """


def encode_cursor(observation_date: date, entry_id: int) -> str:
    """Encode the compound sort key as a base64url-no-padding string.

    Args:
        observation_date: The observation date of the last returned entry.
        entry_id: The entry_id of the last returned entry.

    Returns:
        A URL-safe opaque token.
    """
    payload = f"{observation_date.isoformat()}:{entry_id}".encode("ascii")
    return base64.urlsafe_b64encode(payload).rstrip(b"=").decode("ascii")


def decode_cursor(token: str) -> tuple[date, int]:
    """Decode a cursor string into its ``(observation_date, entry_id)`` pair.

    Args:
        token: The opaque cursor string previously returned as ``next_cursor``.

    Returns:
        A 2-tuple of the cursor's observation_date and entry_id.

    Raises:
        InvalidCursorError: When *token* is empty, not base64, or carries a
            malformed payload (missing colon, bad date, non-positive id).
    """
    if not token:
        raise InvalidCursorError("Cursor must not be empty.")

    padding = "=" * (-len(token) % 4)
    try:
        raw = base64.urlsafe_b64decode(token + padding)
    except (binascii.Error, ValueError) as exc:
        raise InvalidCursorError("Cursor is not valid base64url.") from exc

    try:
        payload = raw.decode("ascii")
    except UnicodeDecodeError as exc:
        raise InvalidCursorError("Cursor payload is not ASCII.") from exc

    if ":" not in payload:
        raise InvalidCursorError("Cursor payload is missing the date:id separator.")

    date_str, _, id_str = payload.partition(":")

    try:
        observation_date = date.fromisoformat(date_str)
    except ValueError as exc:
        raise InvalidCursorError("Cursor payload has an invalid date.") from exc

    try:
        entry_id = int(id_str)
    except ValueError as exc:
        raise InvalidCursorError("Cursor payload has a non-integer id.") from exc

    if entry_id <= 0:
        raise InvalidCursorError("Cursor payload has a non-positive id.")

    return observation_date, entry_id
