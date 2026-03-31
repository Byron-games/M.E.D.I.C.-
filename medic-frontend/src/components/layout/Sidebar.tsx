import { NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, Users, FileText, Calendar,
  Video, Pill, BarChart3, LogOut, Activity, Shield
} from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import { clsx } from 'clsx'

const navItems = [
  { to: '/',             icon: LayoutDashboard, label: 'Overview',      end: true },
  { to: '/patients',     icon: Users,           label: 'Patients (MPI)' },
  { to: '/emr',          icon: FileText,        label: 'Medical Records' },
  { to: '/appointments', icon: Calendar,        label: 'Appointments' },
  { to: '/telemedicine', icon: Video,           label: 'Telemedicine' },
  { to: '/pharmacy',     icon: Pill,            label: 'Pharmacy' },
  { to: '/analytics',    icon: BarChart3,       label: 'Analytics' },
]

export function Sidebar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside className="w-64 flex-shrink-0 bg-surface border-r border-border flex flex-col h-screen sticky top-0">
      {/* Logo */}
      <div className="p-6 border-b border-border">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-cyan-dim border border-cyan/30 flex items-center justify-center shadow-cyan-glow">
            <Activity size={18} className="text-cyan" />
          </div>
          <div>
            <div className="font-display font-bold text-bright text-sm tracking-wider">M.E.D.I.C.</div>
            <div className="text-subtle text-xs font-mono">v1.0.0-SNAPSHOT</div>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 p-3 space-y-0.5 overflow-y-auto">
        {navItems.map(({ to, icon: Icon, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              clsx(isActive ? 'nav-item-active' : 'nav-item')
            }
          >
            <Icon size={16} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* User */}
      <div className="p-3 border-t border-border">
        <div className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-panel mb-1">
          <div className="w-8 h-8 rounded-full bg-cyan-dim border border-cyan/20 flex items-center justify-center flex-shrink-0">
            <Shield size={14} className="text-cyan" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-bright text-xs font-display font-semibold truncate">{user?.fullName ?? 'User'}</div>
            <div className="text-subtle text-xs font-mono truncate">{user?.role}</div>
          </div>
        </div>
        <button onClick={handleLogout} className="nav-item w-full text-rose hover:text-rose hover:bg-rose-dim">
          <LogOut size={16} />
          <span>Sign out</span>
        </button>
      </div>
    </aside>
  )
}
