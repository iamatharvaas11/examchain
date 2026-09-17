import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import App from '../App';
import * as healthService from '@/services/api/health';

describe('App', () => {
  it('renders EXAMCHAIN title in header and home view', async () => {
    vi.spyOn(healthService, 'fetchHealth').mockResolvedValue({
      application: 'examchain',
      version: '0.1.0',
      status: 'UP',
      database: 'CONNECTED',
      timestamp: new Date().toISOString(),
    });

    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    );

    const elements = screen.getAllByText(/EXAMCHAIN/i);
    expect(elements.length).toBeGreaterThan(0);

    await waitFor(() => {
      expect(screen.getByText(/System Online/i)).toBeInTheDocument();
    });
  });
});
