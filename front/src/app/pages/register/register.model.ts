export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegisterResponse {
  id: number;
  email: string;
  username: string;
}

export type RegisterError = {
  kind: 'validation' | 'conflict' | 'unknown';
  message: string;
  fields?: Record<string, string>;
};
