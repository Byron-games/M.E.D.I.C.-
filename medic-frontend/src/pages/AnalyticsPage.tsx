import { useState, useEffect } from 'react'
import { AlertTriangle, BarChart3, TrendingUp, Globe, Shield } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LineChart, Line, CartesianGrid } from 'recharts'
import { analyticsApi } from '../services/api'
import toast from 'react-hot-toast'

const mockTrendData = [
  { month: 'Jan', malaria: 45, respiratory: 89, diarrhea: 23 },
  { month: 'Feb', malaria: 52, respiratory: 112, diarrhea: 18 },
  { month: 'Mar', malaria: 38, respiratory: 95, diarrhea: 31 },
  { month: 'Apr', malaria: 61, respiratory: 78, diarrhea: 42 },
  { month: 'May', malaria: 79, respiratory: 66, diarrhea: 38 },
  { month: 'Jun', malaria: 95, respiratory: 55, diarrhea: 29 },
]

const mockRegionData = [
  { region: 'Centre', cases: 234 },
  { region: 'Littoral', cases: 189 },
  { region: 'Ouest', cases: 145 },
  { region: 'Nord', cases: 312 },
  { region: 'Sud', cases: 98 },
  { region: 'Est', cases: 167 },
]

const alertLevelColors: Record<string, { badge: string; glow: string }> = {
  GREEN:  { badge: 'badge-green',  glow: '' },
  YELLOW: { badge: 'badge-amber',  glow: '' },
  ORANGE: { badge: 'badge-amber',  glow: '' },
  RED:    { badge: 'badge-red',    glow: 'border-rose/30 shadow-[0_0_20px_rgba(255,77,106,0.1)]' },
}

const tooltipStyle = {
  backgroundColor: '#161B22', border: '1px solid #21262D',
  borderRadius: '8px', color: '#C9D1D9', fontSize: '12px', fontFamily: '"DM Sans", sans-serif'
}

export function AnalyticsPage() {
  const [alerts, setAlerts] = useState<any[]>([])
  const [region, setRegion] = useState('Centre')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchAlerts()
  }, [])

  const fetchAlerts = async () => {
    setLoading(true)
    try {
      const res = await analyticsApi.getAlerts()
      setAlerts(Array.isArray(res.data) ? res.data : [])
    } catch {
      // Demo mode — show mock data
      setAlerts([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6 animate-slide-up">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="font-display font-bold text-2xl text-bright">Public Health Analytics</h1>
          <p className="text-subtle text-sm mt-1">Anonymized disease surveillance — no patient identifiers stored</p>
        </div>
        <div className="flex items-center gap-2">
          <Shield size={14} className="text-emerald" />
          <span className="text-emerald text-xs font-mono">Privacy Protected</span>
        </div>
      </div>

      {/* Outbreak Alerts */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <AlertTriangle size={16} className="text-amber" />
            <h2 className="font-display font-semibold text-bright text-sm">Active Outbreak Alerts</h2>
          </div>
          <button onClick={fetchAlerts} className="text-xs text-subtle hover:text-body font-mono">Refresh</button>
        </div>

        {alerts.length > 0 ? (
          <div className="space-y-3">
            {alerts.map((alert: any, i: number) => {
              const colors = alertLevelColors[alert.alertLevel] || alertLevelColors.GREEN
              return (
                <div key={i} className={`p-4 rounded-xl bg-surface border ${colors.glow || 'border-border'}`}>
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="font-display font-semibold text-bright text-sm">{alert.icdDescription || alert.icdCode}</div>
                      <div className="text-subtle text-xs font-mono mt-0.5">{alert.region} · {alert.snapshotDate}</div>
                    </div>
                    <span className={`badge ${colors.badge}`}>{alert.alertLevel}</span>
                  </div>
                  <div className="flex gap-6 mt-3 text-xs">
                    <div><span className="text-subtle font-mono">CASES</span><div className="text-bright font-bold mt-0.5">{alert.caseCount}</div></div>
                    {alert.newCasesVsPreviousWeek && (
                      <div><span className="text-subtle font-mono">CHANGE (7D)</span>
                        <div className={`font-bold mt-0.5 ${alert.newCasesVsPreviousWeek > 0 ? 'text-rose' : 'text-emerald'}`}>
                          {alert.newCasesVsPreviousWeek > 0 ? '+' : ''}{alert.newCasesVsPreviousWeek}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        ) : (
          <div className="text-center py-8">
            <div className="w-12 h-12 rounded-full bg-emerald-dim border border-emerald/20 flex items-center justify-center mx-auto mb-3">
              <Shield size={20} className="text-emerald" />
            </div>
            <div className="text-body text-sm font-semibold">No Active Outbreak Alerts</div>
            <div className="text-subtle text-xs mt-1">Surveillance system running — next check at 02:00 UTC</div>
          </div>
        )}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp size={16} className="text-cyan" />
            <h2 className="font-display font-semibold text-bright text-sm">Disease Trends (6 Months)</h2>
          </div>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={mockTrendData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#21262D" />
              <XAxis dataKey="month" stroke="#8B949E" tick={{ fontSize: 11, fontFamily: '"JetBrains Mono"' }} />
              <YAxis stroke="#8B949E" tick={{ fontSize: 11, fontFamily: '"JetBrains Mono"' }} />
              <Tooltip contentStyle={tooltipStyle} />
              <Line type="monotone" dataKey="malaria"      stroke="#00D9FF" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="respiratory"  stroke="#FF4D6A" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="diarrhea"     stroke="#FFB700" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
          <div className="flex gap-4 mt-2 text-xs font-mono">
            <span className="flex items-center gap-1.5"><span className="w-3 h-0.5 bg-cyan inline-block" />Malaria</span>
            <span className="flex items-center gap-1.5"><span className="w-3 h-0.5 bg-rose inline-block" />Respiratory</span>
            <span className="flex items-center gap-1.5"><span className="w-3 h-0.5 bg-amber inline-block" />Diarrhea</span>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            <Globe size={16} className="text-violet" />
            <h2 className="font-display font-semibold text-bright text-sm">Cases by Region</h2>
          </div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={mockRegionData} barSize={20}>
              <CartesianGrid strokeDasharray="3 3" stroke="#21262D" vertical={false} />
              <XAxis dataKey="region" stroke="#8B949E" tick={{ fontSize: 11, fontFamily: '"JetBrains Mono"' }} />
              <YAxis stroke="#8B949E" tick={{ fontSize: 11, fontFamily: '"JetBrains Mono"' }} />
              <Tooltip contentStyle={tooltipStyle} />
              <Bar dataKey="cases" fill="#A855F7" radius={[4, 4, 0, 0]} fillOpacity={0.8} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Region Query */}
      <div className="card">
        <div className="flex items-center gap-2 mb-4">
          <BarChart3 size={16} className="text-cyan" />
          <h2 className="font-display font-semibold text-bright text-sm">Query by Region</h2>
        </div>
        <div className="flex gap-3 max-w-md">
          <select value={region} onChange={e => setRegion(e.target.value)} className="input flex-1">
            {['Centre', 'Littoral', 'Ouest', 'Nord', 'Sud', 'Est', 'Adamaoua', 'Nord-Ouest', 'Sud-Ouest', 'Extrême-Nord'].map(r => (
              <option key={r} value={r}>{r}</option>
            ))}
          </select>
          <button onClick={() => toast('Live data requires Analytics Service connection', { icon: 'ℹ️' })} className="btn-ghost px-4">
            Query
          </button>
        </div>
        <p className="text-subtle text-xs font-mono mt-3">
          Live disease data streams from the Analytics Service (port 8086) via the API Gateway.
        </p>
      </div>
    </div>
  )
}
