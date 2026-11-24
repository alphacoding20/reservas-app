// Prueba de ESTRÉS: sube la carga por escalones hasta encontrar el punto de quiebre.
// Uso:  k6 run -e BASE_URL=https://reservas-xxxx.onrender.com k6/estres.js
// Para el artículo: anota en qué escalón suben los errores o el p95 supera 500 ms.
import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL;
const FECHA = __ENV.FECHA || '2026-12-01';

export const options = {
  stages: [
    { duration: '1m', target: 100 },
    { duration: '1m', target: 200 },
    { duration: '1m', target: 400 },
    { duration: '1m', target: 600 },
    { duration: '1m', target: 0 },
  ],
  // Sin 'abortOnFail': queremos ver cómo se degrada, no que se corte al primer fallo.
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const r = http.get(`${BASE}/api/reservas/disponibilidad?canchaId=1&fecha=${FECHA}`);
  check(r, { 'status 200': (x) => x.status === 200 });
}
