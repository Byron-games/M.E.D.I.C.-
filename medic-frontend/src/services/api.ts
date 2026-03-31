import axios from 'axios'
import { useAuthStore } from '../store/authStore'

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth ────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
}

// ── Patients ─────────────────────────────────────────────────────────────────
export const patientApi = {
  register: (data: any) => api.post('/patients', data),
  getByMpi: (mpiId: string) => api.get(`/patients/mpi/${mpiId}`),
  getByNationalId: (id: string) => api.get(`/patients/national/${id}`),
  search: (params: any) => api.get('/patients/search', { params }),
  update: (id: string, data: any) => api.put(`/patients/${id}`, data),
}

// ── EMR ───────────────────────────────────────────────────────────────────────
export const emrApi = {
  getRecords: (mpiId: string, page = 0) =>
    api.get(`/records/patient/${mpiId}`, { params: { page, size: 10 } }),
  createRecord: (data: any) => api.post('/records', data),
  getRecord: (id: string) => api.get(`/records/${id}`),
}

// ── Appointments ──────────────────────────────────────────────────────────────
export const appointmentApi = {
  create: (data: any) => api.post('/appointments', data),
  getPatient: (mpiId: string) => api.get(`/appointments/patient/${mpiId}`),
  getClinician: (id: string, from: string, to: string) =>
    api.get(`/appointments/clinician/${id}/schedule`, { params: { from, to } }),
  updateStatus: (id: string, data: any) => api.patch(`/appointments/${id}/status`, data),
}

// ── Telemedicine ──────────────────────────────────────────────────────────────
export const telemedicineApi = {
  createSession: (data: any) => api.post('/telemedicine/sessions', data),
  getSession: (id: string) => api.get(`/telemedicine/sessions/${id}`),
  startSession: (id: string) => api.post(`/telemedicine/sessions/${id}/start`),
  endSession: (id: string) => api.post(`/telemedicine/sessions/${id}/end`),
}

// ── Pharmacy ──────────────────────────────────────────────────────────────────
export const pharmacyApi = {
  issue: (data: any) => api.post('/pharmacy/prescriptions', data),
  getByRxCode: (code: string) => api.get(`/pharmacy/prescriptions/rx/${code}`),
  getPatient: (mpiId: string) => api.get(`/pharmacy/prescriptions/patient/${mpiId}`),
  dispense: (code: string) => api.post(`/pharmacy/prescriptions/rx/${code}/dispense`),
}

// ── Analytics ─────────────────────────────────────────────────────────────────
export const analyticsApi = {
  getAlerts: () => api.get('/analytics/alerts/outbreaks'),
  getByRegion: (region: string, from: string, to: string) =>
    api.get(`/analytics/disease/region/${region}`, { params: { from, to } }),
}
