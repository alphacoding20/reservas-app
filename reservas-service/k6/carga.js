// Prueba de CARGA de reservas-service DESPLEGADO (no localhost, no Postman).
// Uso:  k6 run -e BASE_URL=https://reservas-xxxx.onrender.com k6/carga.js
// Antes de medir, calienta el servicio (los planes gratuitos se duermen): ver README.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE = __ENV.BASE_URL;
const FECHA = __ENV.FECHA || '2026-12-01';           // una fecha futura (máx. 30 días desde hoy)
const tDisponibilidad = new Trend('t_disponibilidad', true);
const tHistorial = new Trend('t_historial', true);
const tAgenda = new Trend('t_agenda', true);

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
    t_disponibilidad: ['p(95)<500'],
    t_historial: ['p(95)<500'],
    t_agenda: ['p(95)<500'],
  },
};

export default function () {
  let r = http.get(`${BASE}/api/reservas/disponibilidad?canchaId=1&fecha=${FECHA}`);
  tDisponibilidad.add(r.timings.duration);
  check(r, { 'disponibilidad 200': (x) => x.status === 200 });

  r = http.get(`${BASE}/api/reservas/usuario/2`);
  tHistorial.add(r.timings.duration);
  check(r, { 'historial 200': (x) => x.status === 200 });

  r = http.get(`${BASE}/api/reservas/cancha/1/agenda?fecha=${FECHA}`);
  tAgenda.add(r.timings.duration);
  check(r, { 'agenda 200': (x) => x.status === 200 });

  sleep(1);
}
