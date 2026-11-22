import type { MembershipRole } from '@/types/membership'

const ROLE_LABELS: Record<MembershipRole, string> = {
  GAME_MASTER: 'Game Master',
  PLAYER: 'Player',
}

export function formatMembershipRole(role: MembershipRole): string {
  return ROLE_LABELS[role]
}