export interface SubjectOption {
  id: number;
  name: string;
}

export interface CreatePostRequest {
  subjectId: number;
  title: string;
  content: string;
}

export interface CreatePostResponse {
  id: number;
}

export type CreatePostError = {
  kind: 'validation' | 'not-found' | 'unknown';
  message: string;
  fields?: Record<string, string>;
};
