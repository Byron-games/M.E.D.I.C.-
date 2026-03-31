import { useState } from 'react'
import { FileText, Search, Plus, ChevronDown, ChevronUp, Stethoscope } from 'lucide-react'
import { emrApi } from '../services/api'
import toast from 'react-hot-toast'

const recordTypeColors: Record<string, string> = {
  OUTPATIENT_VISIT: 'badge-cyan',
  TELEMEDICINE_CONSULTATION: 'badge-violet',
  LAB_RESULT: 'badge-amber',
  PRESCRIPTION: 'badge-green',
  EMERGENCY: 'badge-red',
  VACCINATION: 'badge-green',
  INPATIENT_ADMISSION: 'badge-amber',
}

export function EMRPage() {
  const [mpiId, setMpiId] = useState('')
  const [records, setRecords] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [expanded, setExpanded] = useState<string | null>(null)
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({
    mpiId: '', recordType: 'OUTPATIENT_VISIT', chiefComplaint: '',
    clinicalNotes: '', diagnosisCodes: '', visitDate: new Date().toISOString().slice(0, 16),
    attendingClinicianId: 'doc-1', facilityId: 'FAC-001'
  })

  const search = async () => {
    if (!mpiId.trim()) return
    setLoading(true)
    try {
      const res = await emrApi.getRecords(mpiId)
      const data = res.data?.content ?? res.data ?? []
      setRecords(Array.isArray(data) ? data : [data])
      setForm(f => ({ ...f, mpiId }))
    } catch {
      toast.error('No records found for this MPI ID')
      setRecords([])
    } finally {
      setLoading(false)
    }
  }

  const createRecord = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await emrApi.createRecord({
        ...form,
        diagnosisCodes: JSON.stringify([form.diagnosisCodes]),
        clinicalNotes: JSON.stringify({ notes: form.clinicalNotes }),
        sharedToNetwork: true
      })
      toast.success('Medical record created')
      setShowCreate(false)
      search()
    } catch {
      toast.error('Failed to create record')
    }
  }

  return (
    <div className="space-y-6 animate-slide-up">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="font-display font-bold text-2xl text-bright">Electronic Medical Records</h1>
          <p className="text-subtle text-sm mt-1">Standardized health records across all facilities</p>
        </div>
        <button onClick={() => setShowCreate(!showCreate)} className="btn-primary">
          <Plus size={14} className="inline mr-2" />New Record
        </button>
      </div>

      {/* Search */}
      <div className="card">
        <div className="flex gap-3">
          <input value={mpiId} onChange={e => setMpiId(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && search()}
            placeholder="Enter patient MPI ID (e.g. MPI-XXXXXXXXXXXX)"
            className="input flex-1" />
          <button onClick={search} disabled={loading} className="btn-primary px-6">
            {loading ? <span className="w-4 h-4 border-2 border-void/30 border-t-void rounded-full animate-spin" /> : <Search size={16} />}
          </button>
        </div>
      </div>

      {/* Create Record Form */}
      {showCreate && (
        <form onSubmit={createRecord} className="card space-y-4 border-cyan/20 shadow-cyan-glow">
          <h2 className="font-display font-semibold text-bright text-sm flex items-center gap-2">
            <Stethoscope size={16} className="text-cyan" />New Clinical Record
          </h2>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Patient MPI ID *</label>
              <input value={form.mpiId} onChange={e => setForm(f => ({...f, mpiId: e.target.value}))} className="input" placeholder="MPI-..." required />
            </div>
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Record Type *</label>
              <select value={form.recordType} onChange={e => setForm(f => ({...f, recordType: e.target.value}))} className="input">
                {['OUTPATIENT_VISIT','TELEMEDICINE_CONSULTATION','LAB_RESULT','VACCINATION','PRESCRIPTION','EMERGENCY','INPATIENT_ADMISSION'].map(t => (
                  <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Visit Date *</label>
              <input type="datetime-local" value={form.visitDate} onChange={e => setForm(f => ({...f, visitDate: e.target.value}))} className="input" required />
            </div>
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">ICD-10 Diagnosis Code</label>
              <input value={form.diagnosisCodes} onChange={e => setForm(f => ({...f, diagnosisCodes: e.target.value}))} className="input" placeholder="J06.9" />
            </div>
            <div className="col-span-2">
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Chief Complaint</label>
              <input value={form.chiefComplaint} onChange={e => setForm(f => ({...f, chiefComplaint: e.target.value}))} className="input" placeholder="Patient presents with..." />
            </div>
            <div className="col-span-2">
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Clinical Notes</label>
              <textarea value={form.clinicalNotes} onChange={e => setForm(f => ({...f, clinicalNotes: e.target.value}))}
                className="input min-h-[80px] resize-none" placeholder="SOAP notes, observations..." />
            </div>
          </div>
          <div className="flex gap-3">
            <button type="submit" className="btn-primary">Save Record</button>
            <button type="button" onClick={() => setShowCreate(false)} className="btn-ghost">Cancel</button>
          </div>
        </form>
      )}

      {/* Records List */}
      {records.length > 0 && (
        <div className="space-y-3">
          <div className="text-subtle text-xs font-mono">{records.length} record(s) found</div>
          {records.map((r: any) => (
            <div key={r.id} className="card hover:border-border/80 transition-all">
              <div className="flex items-start justify-between cursor-pointer"
                onClick={() => setExpanded(expanded === r.id ? null : r.id)}>
                <div className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-lg bg-surface border border-border flex items-center justify-center flex-shrink-0">
                    <FileText size={16} className="text-subtle" />
                  </div>
                  <div>
                    <div className="font-display font-semibold text-bright text-sm">{r.chiefComplaint || 'Clinical Record'}</div>
                    <div className="text-subtle text-xs font-mono mt-0.5">
                      {new Date(r.visitDate || r.createdAt).toLocaleDateString()} · {r.facilityName || r.facilityId}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`badge ${recordTypeColors[r.recordType] || 'badge-cyan'}`}>
                    {r.recordType?.replace(/_/g, ' ')}
                  </span>
                  {expanded === r.id ? <ChevronUp size={16} className="text-subtle" /> : <ChevronDown size={16} className="text-subtle" />}
                </div>
              </div>

              {expanded === r.id && (
                <div className="mt-4 pt-4 border-t border-border space-y-3">
                  {r.clinicalNotes && (
                    <div>
                      <div className="text-subtle text-xs font-mono uppercase tracking-wider mb-1">Clinical Notes</div>
                      <div className="text-body text-sm bg-surface rounded-lg p-3 font-mono text-xs">
                        {typeof r.clinicalNotes === 'string' ? r.clinicalNotes : JSON.stringify(r.clinicalNotes, null, 2)}
                      </div>
                    </div>
                  )}
                  {r.diagnosisCodes && (
                    <div>
                      <div className="text-subtle text-xs font-mono uppercase tracking-wider mb-1">Diagnosis Codes</div>
                      <div className="text-body text-sm font-mono">{r.diagnosisCodes}</div>
                    </div>
                  )}
                  <div className="grid grid-cols-3 gap-4 text-xs">
                    <div><span className="text-subtle font-mono">CLINICIAN</span><div className="text-body mt-0.5">{r.attendingClinicianName || r.attendingClinicianId}</div></div>
                    <div><span className="text-subtle font-mono">SHARED</span><div className={`mt-0.5 ${r.sharedToNetwork ? 'text-emerald' : 'text-subtle'}`}>{r.sharedToNetwork ? 'Yes' : 'No'}</div></div>
                    <div><span className="text-subtle font-mono">RECORD ID</span><div className="text-subtle mt-0.5 font-mono text-xs truncate">{r.id?.substring(0, 12)}...</div></div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
