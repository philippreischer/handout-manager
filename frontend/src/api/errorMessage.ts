import { isAxiosError } from 'axios'

/**
 * Extracts a readable message from an API error.
 * The backend answers with RFC 9457 ProblemDetail, so the "detail" field
 * carries the message meant for the user.
 */
export function extractErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError(error)) {
    // No response at all means the request never reached the server.
    if (!error.response) {
      return 'Server not reachable.'
    }
    return error.response.data?.detail ?? fallback
  }
  return fallback
}
