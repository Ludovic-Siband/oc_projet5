export interface UserProfile {
  id: number;
  email: string;
  username: string;
  subscriptions: UserSubscription[];
}

export interface UserSubscription {
  subjectId: number;
  name: string;
  description: string;
}

export interface UpdateUserRequest {
  email?: string | null;
  username?: string | null;
  password?: string | null;
}

export interface UpdateUserResponse {
  id: number;
  email: string;
  username: string;
}

export type UserUpdateError = {
  kind: 'validation' | 'conflict' | 'unknown';
  message: string;
  fields?: Record<string, string>;
};
