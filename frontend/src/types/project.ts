import type { MembershipRole } from '@/types/membership'

export interface ProjectResponse {
  id: number
  name: string
  membershipRole: MembershipRole
}