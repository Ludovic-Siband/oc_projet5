export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  user: {
    id: number;
    email: string;
    username: string;
  };
}

export type LoginError = {
  kind: 'validation' | 'unauthorized' | 'unknown';
  message: string;
  fields?: Record<string, string>;
};
