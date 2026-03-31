import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import { AppLayout } from './components/layout/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { PatientsPage } from './pages/PatientsPage'
import { EMRPage } from './pages/EMRPage'
import { AppointmentsPage } from './pages/AppointmentsPage'
import { TelemedicinePage } from './pages/TelemedicinePage'
import { PharmacyPage } from './pages/PharmacyPage'
import { AnalyticsPage } from './pages/AnalyticsPage'

function RequireAuth({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={
        <RequireAuth>
          <AppLayout />
        </RequireAuth>
      }>
        <Route index element={<DashboardPage />} />
        <Route path="patients" element={<PatientsPage />} />
        <Route path="emr" element={<EMRPage />} />
        <Route path="appointments" element={<AppointmentsPage />} />
        <Route path="telemedicine" element={<TelemedicinePage />} />
        <Route path="pharmacy" element={<PharmacyPage />} />
        <Route path="analytics" element={<AnalyticsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
