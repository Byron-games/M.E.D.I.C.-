import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Activity, Eye, EyeOff, Shield } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuthStore()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!username || !password) {
      toast.error('Please enter credentials')
      return
    }

    setLoading(true)
    try {
      // Demo login — replace with real JWT endpoint when Auth Service is built
      await new Promise(r => setTimeout(r, 800))

      const demoUsers: Record<string, any> = {
        'admin':    { id: '1', username: 'admin',    fullName: 'System Admin',      role: 'ADMIN',         facilityId: 'FAC-000', facilityName: 'National HQ' },
        'doctor':   { id: '2', username: 'doctor',   fullName: 'Dr. Amara Diallo',  role: 'CLINICIAN',     facilityId: 'FAC-001', facilityName: 'Yaoundé Central Hospital' },
        'pharmacy': { id: '3', username: 'pharmacy', fullName: 'PharmTech Kofi',    role: 'PHARMACY',      facilityId: 'FAC-002', facilityName: 'Central Pharmacy' },
        'analyst':  { id: '4', username: 'analyst',  fullName: 'Data Analyst',      role: 'ANALYST',       facilityId: 'FAC-000', facilityName: 'National HQ' },
      }

      const user = demoUsers[username.toLowerCase()]
      if (!user || password !== 'medic123') {
        toast.error('Invalid credentials. Try admin/medic123')
        return
      }

      login(user, 'demo-jwt-token-' + user.id)
      toast.success(`Welcome back, ${user.fullName.split(' ')[0]}!`)
      navigate('/')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-void grid-bg flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-cyan/5 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-64 h-64 bg-violet/5 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-sm animate-slide-up relative z-10">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-cyan-dim border border-cyan/30 shadow-cyan-glow mb-4">
            <Activity size={28} className="text-cyan" />
          </div>
          <h1 className="font-display font-bold text-2xl text-bright tracking-wide">M.E.D.I.C.</h1>
          <p className="text-subtle text-sm mt-1 font-body">Healthcare Interoperability Hub</p>
        </div>

        {/* Card */}
        <div className="card shadow-panel">
          <div className="flex items-center gap-2 mb-6 pb-4 border-b border-border">
            <Shield size={14} className="text-cyan" />
            <span className="text-subtle text-xs font-mono uppercase tracking-wider">Secure Access</span>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">
                Username
              </label>
              <input
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="admin, doctor, pharmacy..."
                className="input"
                autoComplete="username"
              />
            </div>

            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="medic123"
                  className="input pr-10"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPw(!showPw)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-subtle hover:text-body"
                >
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full justify-center flex items-center gap-2 mt-2"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-void/30 border-t-void rounded-full animate-spin" />
              ) : null}
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>

          <div className="mt-4 p-3 rounded-lg bg-surface border border-border">
            <p className="text-subtle text-xs font-mono">
              Demo: <span className="text-cyan">admin</span> / <span className="text-cyan">medic123</span>
            </p>
          </div>
        </div>

        <p className="text-center text-subtle text-xs font-mono mt-6">
          M.E.D.I.C. v1.0 • Secure Healthcare Infrastructure
        </p>
      </div>
    </div>
  )
}
