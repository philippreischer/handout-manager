/**
 * Mirrors UserResponseDTO from the backend.
 * createdAt arrives as an ISO string - Jackson sends text, and Axios only
 * parses JSON, it does not build Date objects.
 */
export interface UserResponse {
  id: number
  email: string
  userName: string
  createdAt: string
}