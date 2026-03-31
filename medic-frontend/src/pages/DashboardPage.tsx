import { Activity, Users, Calendar, Video, Pill, AlertTriangle, TrendingUp, Server } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

const stats = [
  { label: 'Total Patients (MPI)', value: '—', icon: Users,          color: 'text-cyan',    bg: 'bg-cyan-dim',    border: 'border-cyan/20' },
  { label: 'Today\'s Appointments', value: '—', icon: Calendar,      color: 'text-emerald', bg: 'bg-emerald-dim', border: 'border-emerald/20' },
  { label: 'Active Telemedicine',   value: '—', icon: Video,          color: 'text-violet',  bg: 'bg-violet-dim',  border: 'border-violet/20' },
  { label: 'Pending Prescriptions', value: '—', icon: Pill,           color: 'text-amber',   bg: 'bg-amber-dim',   border: 'border-amber/20' },
]

const services = [
  { name: 'API Gateway',         port: 8080, status: 'healthy' },
  { name: 'Patient Identity',    port: 8081, status: 'healthy' },
  { name: 'EMR Service',         port: 8082, status: 'healthy' },
  { name: 'Appointment Service', port: 8083, status: 'healthy' },
  { name: 'Telemedicine',        port: 8084, status: 'healthy' },
  { name: 'Pharmacy',            port: 8085, status: 'healthy' },
  { name: 'Analytics',           port: 8086, status: 'healthy' },
]

export function DashboardPage() {
  const user = useAuthStore(s => s.user)

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2 mb-1">
          <span className="badge badge-green">
            <span className="w-1.5 h-1.5 bg-emerald rounded-full animate-pulse-slow" />
            System Online
          </span>
        </div>
        <h1 className="font-display font-bold text-3xl text-bright">
          Good morning, {user?.fullName?.split(' ')[0] ?? 'Doctor'}.
        </h1>
        <p className="text-subtle text-sm mt-1 font-body">
          {user?.facilityName} · {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map(({ label, value, icon: Icon, color, bg, border }) => (
          <div key={label} className="stat-card">
            <div className={`w-10 h-10 rounded-lg ${bg} border ${border} flex items-center justify-center mb-3`}>
              <Icon size={18} className={color} />
            </div>
            <div className={`text-2xl font-display font-bold ${color}`}>{value}</div>
            <div className="text-subtle text-xs font-body">{label}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Service Health */}
        <div className="card lg:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Server size={16} className="text-cyan" />
              <h2 className="font-display font-semibold text-bright text-sm">Service Health</h2>
            </div>
            <span className="badge badge-green">7/7 Online</span>
          </div>
          <div className="space-y-2">
            {services.map(({ name, port, status }) => (
              <div key={name} className="flex items-center justify-between py-2 border-b border-border last:border-0">
                <div className="flex items-center gap-3">
                  <div className="w-2 h-2 rounded-full bg-emerald shadow-emerald-glow animate-pulse-slow" />
                  <span className="text-body text-sm font-body">{name}</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-subtle text-xs font-mono">:{port}</span>
                  <span className="badge badge-green">{status}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Quick Actions */}
        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            <Activity size={16} className="text-cyan" />
            <h2 className="font-display font-semibold text-bright text-sm">Quick Actions</h2>
          </div>
          <div className="space-y-2">
            {[
              { label: 'Register New Patient',   href: '/patients',     color: 'text-cyan' },
              { label: 'Book Appointment',        href: '/appointments', color: 'text-emerald' },
              { label: 'Start Telemedicine',      href: '/telemedicine', color: 'text-violet' },
              { label: 'Issue Prescription',      href: '/pharmacy',     color: 'text-amber' },
              { label: 'View Outbreak Alerts',    href: '/analytics',    color: 'text-rose' },
            ].map(({ label, href, color }) => (
              <a
                key={label}
                href={href}
                className={`flex items-center gap-3 py-2.5 px-3 rounded-lg bg-surface hover:bg-muted transition-all duration-150 group`}
              >
                <TrendingUp size={14} className={`${color} opacity-60 group-hover:opacity-100`} />
                <span className={`text-sm font-body text-subtle group-hover:${color} transition-colors`}>{label}</span>
              </a>
            ))}
          </div>
        </div>
      </div>

      {/* Outbreak Alert Banner */}
      <div className="flex items-start gap-4 p-4 rounded-xl bg-amber-dim border border-amber/20">
        <AlertTriangle size={18} className="text-amber flex-shrink-0 mt-0.5" />
        <div>
          <div className="font-display font-semibold text-amber text-sm">Analytics Service Active</div>
          <div className="text-subtle text-xs font-body mt-0.5">
            Disease outbreak detection runs daily at 02:00 UTC. Visit the Analytics module to view current public health trends.
          </div>
        </div>
      </div>
    </div>
  )
}
