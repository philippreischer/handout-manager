/**
 * Mirrors TokenResponseDTO from the backend.
 * The refresh token is deliberately absent: it lives in an HttpOnly cookie
 * and is invisible to JavaScript.
 */
export interface TokenResponse {
  accessToken: string
}
