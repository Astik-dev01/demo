import { User } from './auth.types';

export interface Team {
  id: string;
  name: string;
  description: string | null;
  owner: User;
  avatarUrl: string | null;
  isPublic: boolean;
  memberCount: number;
  createdAt: string;
}

export interface TeamMember {
  id: string;
  user: User;
  role: string;
  joinedAt: string;
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
  avatarUrl?: string;
  isPublic?: boolean;
}

export interface UpdateTeamRequest {
  name?: string;
  description?: string;
  avatarUrl?: string;
  isPublic?: boolean;
}

export interface Invitation {
  id: string;
  email: string;
  type: 'PROJECT' | 'TEAM';
  targetId: string;
  targetName: string;
  roleId: string | null;
  invitedBy: User;
  expiresAt: string;
  status: 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';
  createdAt: string;
}

export interface CreateInvitationRequest {
  email: string;
  type: 'PROJECT' | 'TEAM';
  targetId: string;
  roleId?: string;
}
