import type { HealthStatus, ApiResponse } from '@/types/api';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export async function fetchHealth(): Promise<HealthStatus> {
  const response = await fetch(`${API_BASE_URL}/health`);
  
  if (!response.ok) {
    throw new Error('Health check failed');
  }

  const result = await response.json() as ApiResponse<HealthStatus> | HealthStatus;
  
  // Handle both wrapped and unwrapped responses in case backend structure varies
  if ('data' in result && 'success' in result) {
    if (!result.success) {
      throw new Error(result.message || 'Health check reported failure');
    }
    return result.data;
  }
  
  return result as HealthStatus;
}
