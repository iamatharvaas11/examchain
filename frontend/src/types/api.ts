export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface HealthStatus {
  application: string;
  version: string;
  status: string;
  database: string;
  timestamp: string;
}
