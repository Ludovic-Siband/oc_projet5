export interface PostDetail {
  id: number;
  subject: {
    id: number;
    name: string;
  };
  title: string;
  content: string;
  author: string;
  createdAt: string;
  comments: PostComment[];
}

export interface PostComment {
  id: number;
  content: string;
  author: string;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}
