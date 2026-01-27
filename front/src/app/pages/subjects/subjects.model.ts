export interface SubjectItem {
  id: number;
  name: string;
  description: string;
  subscribed: boolean;
}

export type SubscribeError = {
  kind: 'conflict' | 'not-found' | 'unknown';
  message: string;
};
