import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';

describe('admin route registry', () => {
  it('keeps required management modules mounted', () => {
    const source = readFileSync(new URL('./app.tsx', import.meta.url), 'utf8');

    [
      '/config/exam-items',
      '/config/packages',
      '/config/branches',
      '/config/departments',
      '/config/rules',
      '/iam/roles',
      '/iam/accounts',
      '/exam/check-in',
      '/exam/guide-sheet',
      '/exam/queues',
      '/exam/progress',
      '/report/templates',
      '/report/warnings',
      '/management/risk-users',
      '/management/intervention',
      '/statistics/business',
      '/statistics/health',
      '/statistics/export',
      '/audit/logs'
    ].forEach(path => {
      expect(source).toContain(path);
    });
  });
});
