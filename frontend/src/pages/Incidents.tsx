import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Search, SlidersHorizontal } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { incidentService } from '../services/incidentService'
import { errorMessage } from '../services/api'
import { IncidentTable, Loading, ErrorAlert } from '../components/ui'
import { statusLabels, priorityLabels } from '../services/format'
import type { Incident, Status, Priority } from '../types'
export default function Incidents() {
  const { session } = useAuth(); const role = session!.role
  const [incidents, setIncidents] = useState<Incident[] | null>(null); const [error, setError] = useState('')
  const [query, setQuery] = useState(''); const [status, setStatus] = useState(''); const [priority, setPriority] = useState(''); const [revision, setRevision] = useState(0)
  useEffect(() => { let active = true; incidentService.list(role).then(d => { if(active) { setIncidents(d); setError('') } }).catch(e => { if(active) setError(errorMessage(e)) }); return () => { active = false } }, [role, revision])
  const filtered = incidents?.filter(i => (!status || i.status === status) && (!priority || i.priority === priority) && `${i.title} ${i.id} ${i.description} ${i.createdBy.fullName}`.toLocaleLowerCase().includes(query.toLocaleLowerCase())) || []
  return <><div className="page-heading"><div><span className="eyebrow">SIGNALEMENTS</span><h1>{role === 'ADMIN' ? 'Tous les incidents' : role === 'TECHNICIAN' ? 'Incidents affectés' : 'Mes incidents'}<span className="serif-dot">.</span></h1><p>{role === 'ADMIN' ? 'Consultez les signalements et organisez leur prise en charge.' : role === 'TECHNICIAN' ? 'Retrouvez les incidents qui vous sont confiés.' : 'Un suivi clair, du signalement à la résolution.'}</p></div>{role === 'EMPLOYEE' && <Link className="button" to="/incidents/new"><Plus size={18} />Déclarer un incident</Link>}</div>
    <section className="panel"><div className="filter-bar"><div className="search-field"><Search size={18} /><input aria-label="Rechercher un incident" placeholder="Rechercher un incident…" value={query} onChange={e => setQuery(e.target.value)} /></div><div className="filter-selects"><SlidersHorizontal size={17} /><select aria-label="Filtrer par statut" value={status} onChange={e => setStatus(e.target.value)}><option value="">Tous les statuts</option>{Object.entries(statusLabels).map(([key]) => <option key={key} value={key}>{statusLabels[key as Status]}</option>)}</select><select aria-label="Filtrer par priorité" value={priority} onChange={e => setPriority(e.target.value)}><option value="">Toutes les priorités</option>{Object.entries(priorityLabels).map(([key]) => <option key={key} value={key}>{priorityLabels[key as Priority]}</option>)}</select></div></div>
    {error ? <ErrorAlert message={error} retry={() => setRevision(x => x+1)} /> : incidents ? <><div className="results-count">{filtered.length} incident{filtered.length > 1 ? 's' : ''}</div><IncidentTable incidents={filtered} empty="Aucun incident trouvé." /></> : <Loading />}</section></>
}
