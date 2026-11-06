import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Plus, ArrowUpRight, Files, CircleDot, LoaderCircle, CircleCheck, Users, ShieldAlert } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { incidentService, adminService } from '../services/incidentService'
import { errorMessage } from '../services/api'
import { IncidentTable, ErrorAlert, Loading } from '../components/ui'
import { statusLabels } from '../services/format'
import type { Incident, Status } from '../types'
export default function Dashboard() {
  const { session } = useAuth(); const [data, setData] = useState<{incidents: Incident[]; stats: Record<string, number>} | null>(null)
  const [error, setError] = useState(''); const [revision, setRevision] = useState(0)
  const role = session!.role
  useEffect(() => { let active = true; Promise.all([incidentService.list(role), role === 'ADMIN' ? adminService.statistics() : Promise.resolve({})]).then(([incidents, stats]) => { if(active) { setData({ incidents, stats }); setError('') } }).catch(e => { if(active) setError(errorMessage(e)) }); return () => { active = false } }, [role, revision])
  if (error) return <ErrorAlert message={error} retry={() => setRevision(x => x + 1)} />
  if (!data) return <Loading />
  const { incidents, stats } = data
  const count = (s: Status) => incidents.filter(i => i.status === s).length
  const solved = count('RESOLVED') + count('CLOSED')
  const active = incidents.length - solved
  const cards = role === 'ADMIN' ? [
    {label:'Utilisateurs', value:stats.totalUsers, Icon:Users, note:`${stats.EMPLOYEE} employé${stats.EMPLOYEE > 1 ? 's' : ''} · ${stats.TECHNICIAN} technicien${stats.TECHNICIAN > 1 ? 's' : ''}`},
    {label:'Total des incidents', value:incidents.length, Icon:Files, note:'Tous les signalements'},
    {label:'Incidents ouverts', value:count('OPEN'), Icon:CircleDot, note:'En attente d’affectation'},
    {label:'Incidents en cours', value:count('IN_PROGRESS'), Icon:LoaderCircle, note:`${count('CLOSED')} incidents clôturés`},
  ] : role === 'TECHNICIAN' ? [
    {label:'Incidents affectés', value:incidents.length, Icon:Files, note:'Votre périmètre d’intervention'},
    {label:'Incidents critiques', value:incidents.filter(i => i.priority === 'CRITICAL').length, Icon:ShieldAlert, note:'Priorité à surveiller'},
    {label:'En cours de traitement', value:count('IN_PROGRESS'), Icon:LoaderCircle, note:'Interventions en cours'},
    {label:'Incidents résolus', value:solved, Icon:CircleCheck, note:'Résolus et clôturés'},
  ] : [
    {label:'Mes incidents', value:incidents.length, Icon:Files, note:'Tous vos signalements'},
    {label:'Incidents ouverts', value:count('OPEN'), Icon:CircleDot, note:'En attente de prise en charge'},
    {label:'En cours de traitement', value:count('IN_PROGRESS'), Icon:LoaderCircle, note:'Votre équipe intervient'},
    {label:'Incidents résolus', value:solved, Icon:CircleCheck, note:'Résolus et clôturés'},
  ]
  const path = role === 'ADMIN' ? '/admin/incidents' : role === 'TECHNICIAN' ? '/assigned-incidents' : '/my-incidents'
  return <><div className="page-heading"><div><span className="eyebrow">VUE D’ENSEMBLE</span><h1>Bonjour, {session!.fullName.split(' ')[0]}<span className="serif-dot">.</span></h1><p>{role === 'ADMIN' ? 'Gardez une vue claire sur les incidents et votre équipe.' : role === 'TECHNICIAN' ? 'Vos interventions et les incidents qui attendent votre expertise.' : 'Retrouvez vos signalements et suivez leur prise en charge.'}</p></div>{role === 'EMPLOYEE' ? <Link className="button" to="/incidents/new"><Plus size={18} />Déclarer un incident</Link> : <Link className="button" to={path}>Consulter les incidents<ArrowUpRight size={18} /></Link>}</div>
    <section className="stats-grid" aria-label="Statistiques">{cards.map(({label,value,Icon,note}, index) => <article className={`stat-card ${index === 0 ? 'featured' : ''}`} key={label}><div className="stat-top"><span>{label}</span><Icon size={19} /></div><strong>{value}</strong><span className="stat-note">{note}</span></article>)}</section>
    <div className="dashboard-middle"><section className="panel recent-panel"><div className="section-heading"><div><span className="eyebrow">ACTIVITÉ</span><h2>Derniers incidents</h2></div><Link className="text-link" to={path}>Tout voir<ArrowRight size={16} /></Link></div><IncidentTable incidents={incidents.slice(0,5)} /></section><aside className="panel distribution"><span className="eyebrow">RÉPARTITION</span><h2>Où en sommes-nous ?</h2><div className="resolution-value"><strong>{incidents.length ? Math.round(solved / incidents.length * 100) : 0}<small>%</small></strong><span>d’incidents résolus</span></div><div className="segmented-bar" aria-label={`${solved} incidents résolus sur ${incidents.length}`}>{(['OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'] as Status[]).map(s => <span key={s} className={`segment-${s.toLowerCase()}`} style={{flex: count(s)}} />)}</div><div className="legend">{(['OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'] as Status[]).map(s => <div key={s}><span><i className={`segment-${s.toLowerCase()}`} />{statusLabels[s]}</span><strong>{count(s)}</strong></div>)}</div><div className="distribution-foot"><span className="live-dot" />{active} incident{active > 1 ? 's' : ''} à suivre</div></aside></div>
    <section className="dashboard-note"><ShieldAlert size={23} /><div><strong>{role === 'EMPLOYEE' ? 'Un doute sur un email ou un accès inhabituel ?' : 'Chaque intervention laisse une trace.'}</strong><p>{role === 'EMPLOYEE' ? 'Signalez-le rapidement. Votre équipe technique prend le relais.' : 'Consultez l’historique pour retrouver les affectations, les changements de statut et les notes.'}</p></div><Link to={role === 'EMPLOYEE' ? '/incidents/new' : path} aria-label="Ouvrir les incidents"><ArrowUpRight size={22} /></Link></section></>
}
