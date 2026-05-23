"""Pydantic request and response schemas for the auth API endpoints.

Schemas validate inputs at the API boundary before any domain logic runs
(SRS §NFR-S-4).  Password complexity, email format, and display name length
are enforced here using Pydantic validators.

All schemas use ``from __future__ import annotations`` for forward references
and are exported via the module's ``__all__`` for explicit public API.
"""

from __future__ import annotations

import re
from datetime import datetime

from pydantic import BaseModel, EmailStr, Field, field_validator

# Password must be ≥12 chars with at least one uppercase, lowercase, digit, and symbol.
_PW_RE = re.compile(r"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{12,}$")


class RegisterRequest(BaseModel):
    """Request body for POST /api/v1/auth/register (SRS §9.3.1, FR-A-1).

    Attributes:
        email: RFC 5322 email address; normalised to lowercase at the boundary.
        password: Plain-text password meeting complexity requirements.
        display_name: Human-readable name (2–50 chars, trimmed).
    """

    email: EmailStr
    password: str = Field(min_length=12, max_length=128)
    display_name: str = Field(min_length=2, max_length=50)

    @field_validator("email", mode="after")
    @classmethod
    def normalise_email(cls, v: str) -> str:
        """Lowercase and strip the email so storage always matches queries."""
        return v.strip().lower()

    @field_validator("password")
    @classmethod
    def password_meets_complexity(cls, v: str) -> str:
        """Enforce uppercase, lowercase, digit, and symbol requirements."""
        if not _PW_RE.match(v):
            raise ValueError(
                "Password must be at least 12 characters and contain an uppercase "
                "letter, a lowercase letter, a digit, and a special character."
            )
        return v

    @field_validator("display_name")
    @classmethod
    def display_name_not_blank_after_strip(cls, v: str) -> str:
        """Reject display names that are only whitespace after stripping."""
        stripped = v.strip()
        if len(stripped) < 2:
            raise ValueError("Display name must be at least 2 non-whitespace characters.")
        return v


class LoginRequest(BaseModel):
    """Request body for POST /api/v1/auth/login (SRS §9.3.2, FR-A-2).

    Attributes:
        email: The user's email address; normalised to lowercase at the boundary.
        password: The plain-text password.
    """

    email: EmailStr
    password: str = Field(min_length=1, max_length=128)

    @field_validator("email", mode="after")
    @classmethod
    def normalise_email(cls, v: str) -> str:
        """Lowercase and strip the email so it matches the stored normalised form."""
        return v.strip().lower()


class UserResponse(BaseModel):
    """Response body for register, login, and /me endpoints (SRS §9.3.3, FR-A-5).

    Contains only the fields the user themselves provided plus metadata.
    No PII is added beyond what the user submitted (SRS §NFR-Priv-2).

    Attributes:
        user_id: The opaque numeric user identifier.
        email: The user's email address.
        display_name: The user's display name.
        created_at: UTC timestamp of account creation.
    """

    user_id: int
    email: str
    display_name: str
    created_at: datetime

    model_config = {"from_attributes": True}
