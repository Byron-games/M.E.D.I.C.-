import { useState } from 'react'
import { Pill, Search, AlertTriangle, Plus, Trash2, CheckCircle } from 'lucide-react'
import { pharmacyApi } from '../services/api'
import toast from 'react-hot-toast'

interface DrugEntry { drugName: string; dosage: string; frequency: string; durationDays: number }

export function PharmacyPage() {
  const [tab, setTab] = useState<'prescribe' | 'lookup' | 'patient'>('prescribe')
  const [drugs, setDrugs] = useState<DrugEntry[]>([{ drugName: '', dosage: '', frequency: '', durationDays: 7 }])
  const [form, setForm] = useState({ mpiId: '', prescriberId: 'doc-1', prescriberName: 'Dr. Amara Diallo', prescriberLicenseNo: 'LIC-001', issuingFacilityId: 'FAC-001', notes: '' })
  const [result, setResult] = useState<any>(null)
  const [rxCode, setRxCode] = useState('')
  const [rxResult, setRxResult] = useState<any>(null)
  const [patientMpi, setPatientMpi] = useState('')
  const [patientRx, setPatientRx] = useState<any[]>([])
  const [loading, setLoading] = useState(false)

  const addDrug = () => setDrugs(d => [...d, { drugName: '', dosage: '', frequency: '', durationDays: 7 }])
  const removeDrug = (i: number) => setDrugs(d => d.filter((_, j) => j !== i))
  const updateDrug = (i: number, field: keyof DrugEntry, value: any) =>
    setDrugs(d => d.map((drug, j) => j === i ? { ...drug, [field]: value } : drug))

  const issuePrescription = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await pharmacyApi.issue({
        ...form,
        drugs: JSON.stringify(drugs.filter(d => d.drugName))
      })
      setResult(res.data)
      toast.success(`Prescription issued: ${res.data.rxCode}`)
    } catch (err: any) {
      toast.error(err.response?.data?.detail ?? 'Failed to issue prescription')
    } finally {
      setLoading(false)
    }
  }

  const lookupRx = async () => {
    if (!rxCode.trim()) return
    try {
      const res = await pharmacyApi.getByRxCode(rxCode)
      setRxResult(res.data)
    } catch { toast.error('Prescription not found') }
  }

  const getPatientRx = async () => {
    if (!patientMpi.trim()) return
    try {
      const res = await pharmacyApi.getPatient(patientMpi)
      setPatientRx(Array.isArray(res.data) ? res.data : [])
    } catch { toast.error('No prescriptions found') }
  }

  const statusColors: Record<string, string> = {
    ISSUED: 'badge-cyan', SENT_TO_PHARMACY: 'badge-amber',
    DISPENSED: 'badge-green', EXPIRED: 'badge-red', CANCELLED: 'badge-red'
  }

  return (
    <div className="space-y-6 animate-slide-up">
      <div>
        <h1 className="font-display font-bold text-2xl text-bright">Pharmacy & Prescriptions</h1>
        <p className="text-subtle text-sm mt-1">e-Prescriptions with automatic drug interaction checking</p>
      </div>

      <div className="flex gap-2 flex-wrap">
        {(['prescribe', 'lookup', 'patient'] as const).map(t => (
          <button key={t} onClick={() => setTab(t)} className={tab === t ? 'btn-primary' : 'btn-ghost'}>
            {t === 'prescribe' ? '+ Issue Prescription' : t === 'lookup' ? 'Lookup by RX Code' : 'Patient History'}
          </button>
        ))}
      </div>

      {tab === 'prescribe' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <form onSubmit={issuePrescription} className="card space-y-4">
            <h2 className="font-display font-semibold text-bright text-sm border-b border-border pb-3 flex items-center gap-2">
              <Pill size={16} className="text-amber" />Issue Prescription
            </h2>
            <div className="grid grid-cols-2 gap-3">
              {[
                { label: 'Patient MPI *', key: 'mpiId', placeholder: 'MPI-...' },
                { label: 'Prescriber ID', key: 'prescriberId', placeholder: 'doc-1' },
                { label: 'Prescriber Name', key: 'prescriberName', placeholder: 'Dr. Name' },
                { label: 'License No.', key: 'prescriberLicenseNo', placeholder: 'LIC-001' },
              ].map(({ label, key, placeholder }) => (
                <div key={key}>
                  <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">{label}</label>
                  <input value={(form as any)[key]} onChange={e => setForm(f => ({...f, [key]: e.target.value}))}
                    placeholder={placeholder} className="input" />
                </div>
              ))}
              <div className="col-span-2">
                <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Notes</label>
                <input value={form.notes} onChange={e => setForm(f => ({...f, notes: e.target.value}))}
                  placeholder="Special instructions..." className="input" />
              </div>
            </div>

            {/* Drugs */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-subtle text-xs font-mono uppercase tracking-wider">Medications</label>
                <button type="button" onClick={addDrug} className="text-xs text-cyan hover:underline flex items-center gap-1">
                  <Plus size={12} />Add Drug
                </button>
              </div>
              <div className="space-y-2">
                {drugs.map((drug, i) => (
                  <div key={i} className="grid grid-cols-5 gap-2 p-3 rounded-lg bg-surface border border-border">
                    <input placeholder="Drug name" value={drug.drugName} onChange={e => updateDrug(i, 'drugName', e.target.value)} className="input col-span-2 text-xs py-1.5" />
                    <input placeholder="Dosage" value={drug.dosage} onChange={e => updateDrug(i, 'dosage', e.target.value)} className="input text-xs py-1.5" />
                    <input placeholder="Frequency" value={drug.frequency} onChange={e => updateDrug(i, 'frequency', e.target.value)} className="input text-xs py-1.5" />
                    <div className="flex gap-1">
                      <input type="number" placeholder="Days" value={drug.durationDays} onChange={e => updateDrug(i, 'durationDays', Number(e.target.value))} className="input text-xs py-1.5 w-full" />
                      {drugs.length > 1 && <button type="button" onClick={() => removeDrug(i)} className="text-rose hover:text-rose/80 p-1"><Trash2 size={12} /></button>}
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full justify-center flex items-center gap-2">
              {loading ? <span className="w-4 h-4 border-2 border-void/30 border-t-void rounded-full animate-spin" /> : <Pill size={14} />}
              Issue Prescription
            </button>
          </form>

          {/* Result */}
          {result && (
            <div className="card space-y-4">
              <div className="flex items-center gap-2 text-emerald">
                <CheckCircle size={16} />
                <span className="font-display font-semibold text-sm">Prescription Issued</span>
              </div>
              <div className="p-4 rounded-xl bg-surface border border-emerald/20 text-center">
                <div className="text-subtle text-xs font-mono uppercase tracking-wider mb-1">RX Code</div>
                <div className="font-mono font-bold text-2xl text-emerald">{result.rxCode}</div>
                <div className="text-subtle text-xs mt-1">Valid until {result.expiryDate}</div>
              </div>
              {result.interactionWarnings?.length > 0 && (
                <div className="p-3 rounded-lg bg-amber-dim border border-amber/20">
                  <div className="flex items-center gap-2 text-amber text-sm font-semibold mb-2">
                    <AlertTriangle size={14} />{result.interactionWarnings.length} Drug Interaction Warning(s)
                  </div>
                  {result.interactionWarnings.map((w: any, i: number) => (
                    <div key={i} className="text-xs text-body mt-1">
                      <span className={`badge mr-2 ${w.severity === 'CRITICAL' ? 'badge-red' : w.severity === 'HIGH' ? 'badge-red' : 'badge-amber'}`}>{w.severity}</span>
                      {w.drug1} + {w.drug2}: {w.description}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {tab === 'lookup' && (
        <div className="card max-w-lg space-y-4">
          <div className="flex gap-3">
            <input value={rxCode} onChange={e => setRxCode(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && lookupRx()}
              placeholder="RX-XXXXXXXXXX" className="input flex-1 font-mono" />
            <button onClick={lookupRx} className="btn-primary px-6"><Search size={16} /></button>
          </div>
          {rxResult && (
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-cyan">{rxResult.rxCode}</span>
                <span className={`badge ${statusColors[rxResult.status]}`}>{rxResult.status}</span>
              </div>
              <div className="grid grid-cols-2 gap-3 text-xs">
                <div><span className="text-subtle font-mono">PATIENT MPI</span><div className="text-body font-mono mt-0.5">{rxResult.mpiId}</div></div>
                <div><span className="text-subtle font-mono">PRESCRIBER</span><div className="text-body mt-0.5">{rxResult.prescriberName}</div></div>
                <div><span className="text-subtle font-mono">EXPIRY</span><div className="text-body mt-0.5">{rxResult.expiryDate}</div></div>
                <div><span className="text-subtle font-mono">DISPENSED</span><div className="text-body mt-0.5">{rxResult.dispensedAt || '—'}</div></div>
              </div>
            </div>
          )}
        </div>
      )}

      {tab === 'patient' && (
        <div className="space-y-4">
          <div className="card flex gap-3 max-w-lg">
            <input value={patientMpi} onChange={e => setPatientMpi(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && getPatientRx()}
              placeholder="Patient MPI ID" className="input flex-1" />
            <button onClick={getPatientRx} className="btn-primary px-6"><Search size={16} /></button>
          </div>
          {patientRx.map((rx: any) => (
            <div key={rx.id} className="card flex items-center justify-between">
              <div>
                <div className="font-mono font-bold text-cyan text-sm">{rx.rxCode}</div>
                <div className="text-subtle text-xs mt-0.5">{rx.prescriberName} · {rx.createdAt?.slice(0,10)}</div>
              </div>
              <div className="flex items-center gap-3">
                {rx.interactionWarnings?.length > 0 && <AlertTriangle size={14} className="text-amber" />}
                <span className={`badge ${statusColors[rx.status]}`}>{rx.status}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
