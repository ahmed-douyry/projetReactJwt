import { Link } from 'react-router-dom'
import { ArrowUpRight, Inbox, RefreshCw } from 'lucide-react'
import { statusLabels, priorityLabels, categories, date, initials } from '../services/format'
import type { Incident, Status, Priority } from '../types'
export function StatusBadge({ value }: { value: Status }) { return <span className={`badge status-${value.toLowerCase()}`}><i />{statusLabels[value]}</span> }
export function PriorityBadge({ value }: { value: Priority }) { return <span className={`priority priority-${value.toLowerCase()}`}><span>▰</span> {priorityLabels[value]}</span> }
export function ErrorAlert({ message, retry }: { message: string; retry?: () => void }) { return <div className="alert error" role="alert">{message}{retry && <button className="text-button" onClick={retry}><RefreshCw size={14} /> Réessayer</button>}</div> }
export function Loading() { return <div className="loading" role="status"><RefreshCw className="spin" size={20} /> Chargement…</div> }
export function IncidentTable({ incidents, empty = 'Aucun incident pour le moment.' }: { incidents: Incident[]; empty?: string }) {
  if (!incidents.length) return <div className="empty"><Inbox size={32} /><h3>{empty}</h3><p>Les incidents correspondant à votre sélection apparaîtront ici.</p></div>
  return <div className="table-scroll"><table><thead><tr><th>Incident</th><th>Priorité</th><th>Statut</th><th>Technicien</th><th>Création</th><th><span className="sr-only">Consulter</span></th></tr></thead><tbody>{incidents.map(i => <tr key={i.id}><td><Link className="ticket-title" to={`/incidents/${i.id}`}><span className="ticket-ref">INC-{String(i.id).padStart(4, '0')}</span>{i.title}</Link><span className="table-category">{categories[i.category]}</span></td><td><PriorityBadge value={i.priority} /></td><td><StatusBadge value={i.status} /></td><td>{i.assignedTo ? <span className="person"><span className="mini-avatar">{initials(i.assignedTo.fullName)}</span>{i.assignedTo.fullName}</span> : <span className="muted">Non affecté</span>}</td><td className="nowrap muted">{date(i.createdAt)}</td><td><Link className="icon-button" aria-label={`Consulter ${i.title}`} to={`/incidents/${i.id}`}><ArrowUpRight size={18} /></Link></td></tr>)}</tbody></table></div>
}
