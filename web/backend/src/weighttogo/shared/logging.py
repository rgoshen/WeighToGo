"""Structured logging setup for the Weigh to Go! backend.

Uses structlog with stdlib integration. PII masking is provided as a
standalone utility so callers can sanitise log values before binding them.

Usage::

    from weighttogo.shared.logging import get_logger, mask_pii

    logger = get_logger(__name__)
    logger.info("user action", user_id="u-123", email=mask_pii(raw_email))
"""

import re

import structlog

_EMAIL_PATTERN: re.Pattern[str] = re.compile(r"[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}")


def mask_pii(value: str) -> str:
    """Replace email addresses in *value* with the placeholder ``[email]``.

    Args:
        value: An arbitrary string that may contain PII.

    Returns:
        The input string with all email addresses replaced by ``[email]``.
    """
    return _EMAIL_PATTERN.sub("[email]", value)


def get_logger(name: str = __name__) -> structlog.stdlib.BoundLogger:
    """Return a bound structlog logger for *name*.

    The logger is configured by the application's structlog setup. Callers
    should pass ``__name__`` as the *name* argument so log records carry the
    originating module path.

    Args:
        name: Logger name, typically ``__name__`` of the calling module.

    Returns:
        A structlog ``BoundLogger`` ready to emit structured log events.
    """
    return structlog.stdlib.get_logger(name)
