import { useState } from 'react'
import { Search, UserPlus, User, Copy, CheckCircle } from 'lucide-react'
import { patientApi } from '../services/api'
import toast from 'react-hot-toast'

interface Patient {
  id: string; mpiId: string; nationalId: string
  firstName: string; lastName: string; dateOfBirth: string
  gender: string; phoneNumber: string; email: string
  region: string; bloodType: string; active: boolean
}

export function PatientsPage() {
  const [tab, setTab] = useState<'search' | 'register'>('search')
  const [query, setQuery] = useState('')
  const [searchType, setSearchType] = useState<'mpi' | 'national' | 'name'>('mpi')
  const [results, setResults] = useState<Patient[]>([])
  const [selected, setSelected] = useState<Patient | null>(null)
  const [loading, setLoading] = useState(false)
  const [copied, setCopied] = useState('')

  // Register form state
  const [form, setForm] = useState({
    nationalId: '', firstName: '', lastName: '', dateOfBirth: '',
    gender: 'FEMALE', phoneNumber: '', email: '', region: '',
    bloodType: '', facilityId: 'FAC-001', facilityName: 'Demo Facility'
  })

  const handleSearch = async () => {
    if (!query.trim()) return
    setLoading(true)
    try {
      let res
      if (searchType === 'mpi')      res = await patientApi.getByMpi(query)
      else if (searchType === 'national') res = await patientApi.getByNationalId(query)
      else res = await patientApi.search({ firstName: query })

      const data = Array.isArray(res.data) ? res.data : [res.data]
      setResults(data)
      if (data.length === 0) toast('No patients found', { icon: '🔍' })
    } catch {
      toast.error('Patient not found')
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await patientApi.register(form)
      toast.success(`Patient registered! MPI: ${res.data.mpiId}`)
      setSelected(res.data)
      setTab('search')
    } catch (err: any) {
      toast.error(err.response?.data?.detail ?? 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const copyMpi = (mpiId: string) => {
    navigator.clipboard.writeText(mpiId)
    setCopied(mpiId)
    toast.success('MPI ID copied!')
    setTimeout(() => setCopied(''), 2000)
  }

  return (
    <div className="space-y-6 animate-slide-up">
      <div>
        <h1 className="font-display font-bold text-2xl text-bright">Patient Identity (MPI)</h1>
        <p className="text-subtle text-sm mt-1">Master Patient Index — cross-facility patient identification</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2">
        <button onClick={() => setTab('search')} className={tab === 'search' ? 'btn-primary' : 'btn-ghost'}>
          <Search size={14} className="inline mr-2" />Search Patient
        </button>
        <button onClick={() => setTab('register')} className={tab === 'register' ? 'btn-primary' : 'btn-ghost'}>
          <UserPlus size={14} className="inline mr-2" />Register Patient
        </button>
      </div>

      {tab === 'search' && (
        <div className="space-y-4">
          <div className="card">
            <div className="flex gap-3 mb-4">
              {(['mpi', 'national', 'name'] as const).map(t => (
                <button key={t} onClick={() => setSearchType(t)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-mono uppercase transition-all ${
                    searchType === t ? 'bg-cyan-dim text-cyan border border-cyan/20' : 'text-subtle hover:text-body border border-transparent'
                  }`}>
                  {t === 'mpi' ? 'MPI ID' : t === 'national' ? 'National ID' : 'Name'}
                </button>
              ))}
            </div>
            <div className="flex gap-3">
              <input value={query} onChange={e => setQuery(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()}
                placeholder={searchType === 'mpi' ? 'MPI-XXXXXXXXXXXX' : searchType === 'national' ? 'CMR-XXXX-XXX' : 'First name...'}
                className="input flex-1" />
              <button onClick={handleSearch} disabled={loading} className="btn-primary px-6">
                {loading ? <span className="w-4 h-4 border-2 border-void/30 border-t-void rounded-full animate-spin" /> : <Search size={16} />}
              </button>
            </div>
          </div>

          {results.length > 0 && (
            <div className="space-y-3">
              {results.map(p => (
                <div key={p.id} className={`card cursor-pointer transition-all hover:border-cyan/30 ${selected?.id === p.id ? 'border-cyan/40 shadow-cyan-glow' : ''}`}
                  onClick={() => setSelected(selected?.id === p.id ? null : p)}>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-full bg-cyan-dim border border-cyan/20 flex items-center justify-center flex-shrink-0">
                        <User size={16} className="text-cyan" />
                      </div>
                      <div>
                        <div className="font-display font-semibold text-bright">{p.firstName} {p.lastName}</div>
                        <div className="text-subtle text-xs font-mono">{p.nationalId} · {p.region}</div>
                      </div>
                    </div>
                    <div className="text-right">
                      <button onClick={e => { e.stopPropagation(); copyMpi(p.mpiId) }}
                        className="flex items-center gap-1.5 text-xs font-mono text-cyan hover:text-bright transition-colors">
                        {copied === p.mpiId ? <CheckCircle size={12} /> : <Copy size={12} />}
                        {p.mpiId}
                      </button>
                      <span className={p.active ? 'badge-green badge mt-1' : 'badge-red badge mt-1'}>
                        {p.active ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                  </div>

                  {selected?.id === p.id && (
                    <div className="mt-4 pt-4 border-t border-border grid grid-cols-2 md:grid-cols-4 gap-4">
                      {[
                        ['Date of Birth', p.dateOfBirth],
                        ['Gender', p.gender],
                        ['Blood Type', p.bloodType || '—'],
                        ['Phone', p.phoneNumber || '—'],
                      ].map(([label, val]) => (
                        <div key={label}>
                          <div className="text-subtle text-xs font-mono uppercase tracking-wider mb-1">{label}</div>
                          <div className="text-body text-sm">{val}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {tab === 'register' && (
        <form onSubmit={handleRegister} className="card space-y-4 max-w-2xl">
          <h2 className="font-display font-semibold text-bright text-sm border-b border-border pb-3">
            New Patient Registration
          </h2>
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: 'National ID *', key: 'nationalId', placeholder: 'CMR-1990-001' },
              { label: 'First Name *', key: 'firstName', placeholder: 'Amara' },
              { label: 'Last Name *', key: 'lastName', placeholder: 'Diallo' },
              { label: 'Date of Birth *', key: 'dateOfBirth', type: 'date' },
              { label: 'Phone', key: 'phoneNumber', placeholder: '+237612345678' },
              { label: 'Email', key: 'email', placeholder: 'patient@example.com' },
              { label: 'Region', key: 'region', placeholder: 'Centre' },
              { label: 'Blood Type', key: 'bloodType', placeholder: 'O+' },
            ].map(({ label, key, placeholder, type }) => (
              <div key={key}>
                <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">{label}</label>
                <input type={type || 'text'} value={(form as any)[key]}
                  onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
                  placeholder={placeholder} className="input" />
              </div>
            ))}
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Gender *</label>
              <select value={form.gender} onChange={e => setForm(f => ({ ...f, gender: e.target.value }))} className="input">
                <option value="FEMALE">Female</option>
                <option value="MALE">Male</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Registering...' : 'Register Patient'}
            </button>
            <button type="button" onClick={() => setTab('search')} className="btn-ghost">Cancel</button>
          </div>
        </form>
      )}
    </div>
  )
}
