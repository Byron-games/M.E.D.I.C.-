import { useState } from 'react'
import { Calendar, Plus, Clock, User, Video, MapPin } from 'lucide-react'
import { appointmentApi } from '../services/api'
import toast from 'react-hot-toast'

export function AppointmentsPage() {
  const [tab, setTab] = useState<'list' | 'book'>('list')
  const [mpiId, setMpiId] = useState('')
  const [appointments, setAppointments] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    mpiId: '', patientName: '', facilityId: 'FAC-001', facilityName: 'Demo Facility',
    clinicianId: 'doc-1', clinicianName: 'Dr. Amara Diallo', clinicianSpecialty: 'General Medicine',
    type: 'IN_PERSON', scheduledAt: '', durationMinutes: 30, notes: ''
  })

  const fetchAppointments = async () => {
    if (!mpiId.trim()) return
    setLoading(true)
    try {
      const res = await appointmentApi.getPatient(mpiId)
      setAppointments(Array.isArray(res.data) ? res.data : [])
    } catch {
      toast.error('No appointments found')
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const bookAppointment = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await appointmentApi.create(form)
      toast.success('Appointment booked!')
      setTab('list')
      if (mpiId) fetchAppointments()
    } catch (err: any) {
      toast.error(err.response?.data?.detail ?? 'Booking failed')
    }
  }

  const updateStatus = async (id: string, status: string) => {
    try {
      await appointmentApi.updateStatus(id, { status })
      toast.success(`Appointment ${status.toLowerCase()}`)
      fetchAppointments()
    } catch {
      toast.error('Update failed')
    }
  }

  const statusColors: Record<string, string> = {
    SCHEDULED: 'badge-cyan', CONFIRMED: 'badge-green',
    COMPLETED: 'badge-violet', CANCELLED: 'badge-red', NO_SHOW: 'badge-amber'
  }

  return (
    <div className="space-y-6 animate-slide-up">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="font-display font-bold text-2xl text-bright">Appointments</h1>
          <p className="text-subtle text-sm mt-1">Schedule in-person and telemedicine consultations</p>
        </div>
        <button onClick={() => setTab(tab === 'book' ? 'list' : 'book')} className="btn-primary">
          <Plus size={14} className="inline mr-2" />Book Appointment
        </button>
      </div>

      {tab === 'list' && (
        <div className="space-y-4">
          <div className="card flex gap-3">
            <input value={mpiId} onChange={e => setMpiId(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && fetchAppointments()}
              placeholder="Enter patient MPI ID to view appointments"
              className="input flex-1" />
            <button onClick={fetchAppointments} disabled={loading} className="btn-primary px-6">
              {loading ? <span className="w-4 h-4 border-2 border-void/30 border-t-void rounded-full animate-spin" /> : <Calendar size={16} />}
            </button>
          </div>

          {appointments.length > 0 ? (
            <div className="space-y-3">
              {appointments.map((a: any) => (
                <div key={a.id} className="card hover:border-border/80 transition-all">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-4">
                      <div className={`w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 ${
                        a.type === 'TELEMEDICINE' ? 'bg-violet-dim border border-violet/20' : 'bg-cyan-dim border border-cyan/20'
                      }`}>
                        {a.type === 'TELEMEDICINE' ? <Video size={16} className="text-violet" /> : <MapPin size={16} className="text-cyan" />}
                      </div>
                      <div>
                        <div className="font-display font-semibold text-bright text-sm">{a.patientName}</div>
                        <div className="flex items-center gap-3 text-subtle text-xs font-mono mt-0.5">
                          <span className="flex items-center gap-1"><Clock size={10} />{new Date(a.scheduledAt).toLocaleString()}</span>
                          <span className="flex items-center gap-1"><User size={10} />{a.clinicianName}</span>
                        </div>
                        {a.notes && <div className="text-subtle text-xs mt-1">{a.notes}</div>}
                      </div>
                    </div>
                    <div className="flex items-center gap-2 flex-shrink-0">
                      <span className={`badge ${statusColors[a.status] || 'badge-cyan'}`}>{a.status}</span>
                      {a.status === 'SCHEDULED' && (
                        <div className="flex gap-1">
                          <button onClick={() => updateStatus(a.id, 'CONFIRMED')} className="text-xs px-2 py-1 rounded bg-emerald-dim text-emerald border border-emerald/20 hover:bg-emerald/20">Confirm</button>
                          <button onClick={() => updateStatus(a.id, 'CANCELLED')} className="text-xs px-2 py-1 rounded bg-rose-dim text-rose border border-rose/20 hover:bg-rose/20">Cancel</button>
                        </div>
                      )}
                    </div>
                  </div>
                  {a.telemedicineJoinUrl && (
                    <div className="mt-3 pt-3 border-t border-border">
                      <a href={a.telemedicineJoinUrl} target="_blank" rel="noreferrer"
                        className="inline-flex items-center gap-2 text-violet text-xs font-mono hover:underline">
                        <Video size={12} />Join Session: {a.telemedicineJoinUrl}
                      </a>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="card text-center py-12">
              <Calendar size={32} className="text-muted mx-auto mb-3" />
              <div className="text-subtle text-sm">Enter an MPI ID to view appointments</div>
            </div>
          )}
        </div>
      )}

      {tab === 'book' && (
        <form onSubmit={bookAppointment} className="card space-y-4 max-w-2xl">
          <h2 className="font-display font-semibold text-bright text-sm border-b border-border pb-3">Book New Appointment</h2>
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: 'Patient MPI ID *', key: 'mpiId', placeholder: 'MPI-...' },
              { label: 'Patient Name *', key: 'patientName', placeholder: 'Full name' },
              { label: 'Clinician ID', key: 'clinicianId', placeholder: 'doc-1' },
              { label: 'Clinician Name', key: 'clinicianName', placeholder: 'Dr. Name' },
              { label: 'Specialty', key: 'clinicianSpecialty', placeholder: 'General Medicine' },
              { label: 'Duration (min)', key: 'durationMinutes', placeholder: '30', type: 'number' },
            ].map(({ label, key, placeholder, type }) => (
              <div key={key}>
                <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">{label}</label>
                <input type={type || 'text'} value={(form as any)[key]}
                  onChange={e => setForm(f => ({ ...f, [key]: type === 'number' ? Number(e.target.value) : e.target.value }))}
                  placeholder={placeholder} className="input" />
              </div>
            ))}
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Type *</label>
              <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))} className="input">
                <option value="IN_PERSON">In Person</option>
                <option value="TELEMEDICINE">Telemedicine</option>
              </select>
            </div>
            <div>
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Date & Time *</label>
              <input type="datetime-local" value={form.scheduledAt}
                onChange={e => setForm(f => ({ ...f, scheduledAt: e.target.value }))} className="input" required />
            </div>
            <div className="col-span-2">
              <label className="block text-subtle text-xs font-mono uppercase tracking-wider mb-1.5">Notes</label>
              <input value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
                placeholder="Reason for visit..." className="input" />
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary">Book Appointment</button>
            <button type="button" onClick={() => setTab('list')} className="btn-ghost">Cancel</button>
          </div>
        </form>
      )}
    </div>
  )
}
