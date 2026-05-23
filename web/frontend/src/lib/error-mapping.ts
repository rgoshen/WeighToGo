/**
 * Maps HTTP status codes returned by the Weigh to Go! API to user-friendly
 * error messages.
 *
 * Keeping this mapping in a dedicated module makes it independently testable
 * and prevents error-message strings from being scattered across the codebase.
 */

/** Human-readable messages indexed by HTTP status code. */
const ERROR_MESSAGES: Readonly<Record<number, string>> = {
  400: 'The request contained invalid data. Please check your input and try again.',
  401: 'Your session has expired. Please log in again.',
  403: 'You do not have permission to perform this action.',
  404: 'The requested resource could not be found.',
  409: 'A record with that information already exists.',
  422: 'The submitted data did not pass validation. Please review the form and try again.',
  429: 'Too many requests. Please wait a moment before trying again.',
  500: 'A server error occurred. Please try again later.',
  503: 'The service is temporarily unavailable. Please try again later.',
};

const FALLBACK_MESSAGE = 'An unexpected error occurred. Please try again later.';

/**
 * Returns a user-friendly string for the given HTTP status code.
 *
 * @param status - HTTP response status code.
 */
export function mapApiError(status: number): string {
  return ERROR_MESSAGES[status] ?? FALLBACK_MESSAGE;
}

export type FieldErrors = Record<string, string>;

interface ValidationProblem {
  field: string;
  code: string;
  message: string;
}

/**
 * Converts an array of RFC 7807-style field-level validation errors into a
 * flat record keyed by field name.  When a field appears more than once, only
 * the first message is kept so the caller always gets a single, stable string
 * per field.
 *
 * @param errors - Array of validation problem objects from the API response.
 */
export function mapValidationErrors(errors: ValidationProblem[]): FieldErrors {
  const out: FieldErrors = {};
  for (const e of errors) {
    if (!(e.field in out)) out[e.field] = e.message;
  }
  return out;
}
