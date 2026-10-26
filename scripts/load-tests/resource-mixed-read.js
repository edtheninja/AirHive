import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = (__ENV.AIRHIVE_BASE_URL || 'http://localhost:8080')
  .replace(/\/$/, '');
const TOKEN = __ENV.AIRHIVE_TOKEN;

const endpoints = [
  '/api/airports',
  '/api/routes',
  '/api/aircraft-types',
  '/api/aircraft',
  '/api/flights',
  '/api/crew',
  '/api/maintenance',
  '/api/bookings',
  '/api/passengers',
  '/api/analytics/overview',
];

export const options = {
  scenarios: {
    mixed_read_load: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 50),
      duration: __ENV.DURATION || '90s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    checks: ['rate>0.99'],
  },
};

export default function () {
  if (!TOKEN) {
    throw new Error('Set AIRHIVE_TOKEN before running this test.');
  }

  const endpoint = endpoints[(__VU + __ITER - 1) % endpoints.length];

  const response = http.get(`${BASE_URL}${endpoint}`, {
    headers: {
      Authorization: `Bearer ${TOKEN}`,
      Accept: 'application/json',
    },
    tags: { endpoint },
    timeout: '10s',
  });

  check(response, {
    [`${endpoint} returns HTTP 200`]: (r) => r.status === 200,
  });
}
