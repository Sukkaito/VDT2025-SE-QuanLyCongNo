export interface ApiResponse<T> {
  status: number;
  message: string;
  data?: T;
}

interface PaginatedData<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  sortBy: string;
}

export interface PagignatedResponse<T> {
  status: number;
  message: string;
  data?: PaginatedData<T>;
}