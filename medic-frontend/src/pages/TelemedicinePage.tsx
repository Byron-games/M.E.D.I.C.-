import { useState } from 'react'
import { Video, Play, Square, ExternalLink, Wifi, WifiOff } from 'lucide-react'
import { telemedicineApi } from '../services/api'
import toast from 'react-hot-toast'

export function TelemedicinePage() {
  const [form, setForm] = useState({
    appointmentId: '', mpiId: '', clinicianId: 'doc-1',
    scheduledAt: new Date().toISOString().slice(0, 16), lowBandwidthMode: false
  })
  const [session, setSession] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [sessionId, setSessionId] = useState('')
  const [lookupSession, setLookupSession] = useState<any>(null)

  const createSession = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await telemedicineApi.createSession({
        ...form,
        appointmentId: form.appointmentId || crypto.randomUUID(),
        scheduledAt: new Date(form.scheduledAt).toISOString()
      })
      setSession(res.data)
      toast.success('Telemedicine session created!')
    } catch (err: any) {
      toast.error(err.response?.data?.detail ?? 'Failed to create session')
    } finally {
      setLoading(false)
    }
  }

  const endSession = async () => {
    if (!session) return
    try {
      await telemedicineApi.endSession(session.id)
      toast.success('Session ended')
      setSession(null)
    } catch {
      toast.error('Failed to end session')
    }
  }

  const lookupById = async () => {
    if (!sessionId.trim()) return
    try {
      const res = await telemedicineApi.getSession(sessionId)
      setLookupSession(res.data)
    } catch {
      toast.error('Session not found')
    }
  }

  const statusColors: Record<string, string> = {
    CREATED: 'badge-cyan', ACTIVE: 'badge-green',
    COMPLETED: 'badge-violet', EXPIRED: 'badge-amber', CANCELLED: 'badge-red'
  }

  return (
    <div className="space-y-6 animate-slide-up">
      <div>
        <h1 className="font-display font-bold text-2xl text-bright">Telemedicine</h1>
        <p className="text-subtle text-sm mt-1">Secure video consultations — supports low-bandwidth rural mode</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Create Session */}
        <form onSubmit={createSession} className="card space-y-4">
          <div className="flex items-center gap-2 pb-3 border-b border-border">
            <Video size={16} className="text-violet" />
            <h2 className="font-display font-semibold text-bright text-sm">New Session</h2>
          </div>
          <div className="space-y-3">
            {[
              { label: 'Patient MPI ID *', key: 'mpiId', placeholder: 'MPI-...' },
              { label: 'Clinician ID *', key: 'clinicianId', placeholder: 'doc-1' },
            ].map(({ label, key, placeholder }) => (
              <div key={key}>
                <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">{label}</label>
                <input value={(form as any)[key]} onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
                  placeholder={placeholder} className="input" />
              </div>
            ))}
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Scheduled At</label>
              <input type="datetime-local" value={form.scheduledAt}
                onChange={e => setForm(f => ({ ...f, scheduledAt: e.target.value }))} className="input" />
            </div>
            <label className="flex items-center gap-3 cursor-pointer p-3 rounded-lg bg-surface border border-border hover:border-amber/30 transition-all">
              <input type="checkbox" checked={form.lowBandwidthMode}
                onChange={e => setForm(f => ({ ...f, lowBandwidthMode: e.target.checked }))}
                className="rounded border-border" />
              <div>
                <div className="flex items-center gap-2 text-sm text-body">
                  {form.lowBandwidthMode ? <WifiOff size={14} className="text-amber" /> : <Wifi size={14} className="text-emerald" />}
                  Low Bandwidth Mode (Rural)
                </div>
                <div className="text-subtle text-xs mt-0.5">Reduces video quality for slow connections</div>
              </div>
            </label>
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full justify-center flex items-center gap-2">
            {loading ? <span className="w-4 h-4 border-2 border-void/30 border-t-void rounded-full animate-spin" /> : <Play size={14} />}
            Create Session
          </button>
        </form>

        {/* Active Session / Lookup */}
        <div className="space-y-4">
          {session && (
            <div className="card border-violet/30 shadow-[0_0_20px_rgba(168,85,247,0.1)] space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-emerald rounded-full animate-pulse" />
                  <span className="font-display font-semibold text-bright text-sm">Session Active</span>
                </div>
                <span className={`badge ${statusColors[session.status] || 'badge-cyan'}`}>{session.status}</span>
              </div>
              <div className="space-y-3">
                <div className="p-3 rounded-lg bg-surface border border-border">
                  <div className="text-subtle text-xs font-mono uppercase tracking-wider mb-1">Patient Join URL</div>
                  <a href={session.patientJoinUrl} target="_blank" rel="noreferrer"
                    className="flex items-center gap-2 text-cyan text-xs font-mono hover:underline break-all">
                    <ExternalLink size={12} className="flex-shrink-0" />{session.patientJoinUrl}
                  </a>
                </div>
                <div className="p-3 rounded-lg bg-surface border border-border">
                  <div className="text-subtle text-xs font-mono uppercase tracking-wider mb-1">Clinician Join URL</div>
                  <a href={session.clinicianJoinUrl} target="_blank" rel="noreferrer"
                    className="flex items-center gap-2 text-violet text-xs font-mono hover:underline break-all">
                    <ExternalLink size={12} className="flex-shrink-0" />{session.clinicianJoinUrl}
                  </a>
                </div>
                {session.lowBandwidthMode && (
                  <div className="flex items-center gap-2 text-amber text-xs">
                    <WifiOff size={12} />Low bandwidth mode active
                  </div>
                )}
              </div>
              <div className="flex gap-2">
                <a href={session.clinicianJoinUrl} target="_blank" rel="noreferrer" className="btn-primary flex-1 text-center text-sm">
                  Join as Clinician
                </a>
                <button onClick={endSession} className="btn-danger flex items-center gap-2">
                  <Square size={14} />End
                </button>
              </div>
            </div>
          )}

          {/* Lookup existing session */}
          <div className="card space-y-3">
            <div className="flex items-center gap-2 pb-3 border-b border-border">
              <Video size={16} className="text-cyan" />
              <h2 className="font-display font-semibold text-bright text-sm">Lookup Session</h2>
            </div>
            <div className="flex gap-3">
              <input value={sessionId} onChange={e => setSessionId(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && lookupById()}
                placeholder="Session UUID" className="input flex-1 font-mono text-xs" />
              <button onClick={lookupById} className="btn-ghost px-4">Find</button>
            </div>
            {lookupSession && (
              <div className="p-3 rounded-lg bg-surface border border-border space-y-2">
                <div className="flex justify-between">
                  <span className="text-subtle text-xs font-mono">Status</span>
                  <span className={`badge ${statusColors[lookupSession.status]}`}>{lookupSession.status}</span>
                </div>
                {lookupSession.durationSeconds && (
                  <div className="flex justify-between">
                    <span className="text-subtle text-xs font-mono">Duration</span>
                    <span className="text-body text-xs font-mono">{Math.round(lookupSession.durationSeconds / 60)} min</span>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
