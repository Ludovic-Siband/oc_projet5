export type FeedSort = 'asc' | 'desc';

export interface FeedPost {
  id: number;
  subjectId: number;
  author: string;
  title: string;
  content: string;
  createdAt: string;
}
