import type { MembershipRole } from '@/types/membership'

export interface ProjectResponse {
  id: number
  name: string
  gameSystem: string | null
  description: string | null
  membershipRole: MembershipRole
}