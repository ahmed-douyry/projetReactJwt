import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowLeft, ArrowRight, ShieldCheck } from 'lucide-react'
import { incidentService } from '../services/incidentService'
import { errorMessage } from '../services/api'
import { ErrorAlert } from '../components/ui'
import { categories, priorityLabels } from '../services/format'
export default function NewIncident() {
  const navigate = useNavigate(); const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault(); const values = Object.fromEntries(new FormData(e.currentTarget)) as {title: string; description: string; category: string; priority: string}
    setBusy(true); setError('')
    try { const i = await incidentService.create(values); navigate(`/incidents/${i.id}`, {state:{created:true}}) } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }
  return <><Link className="back-link" to="/my-incidents"><ArrowLeft size={16} />Mes incidents</Link><div className="page-heading"><div><span className="eyebrow">NOUVEAU SIGNALEMENT</span><h1>Déclarer un incident<span className="serif-dot">.</span></h1><p>Décrivez la situation pour aider votre équipe à intervenir.</p></div></div><div className="form-layout"><form className="panel form-panel" onSubmit={submit}>{error && <ErrorAlert message={error} />}<h2>Que s’est-il passé ?</h2><label>Titre de l’incident<input name="title" required maxLength={150} placeholder="Ex. Un email suspect reçu ce matin" /></label><div className="form-row"><label>Catégorie<select name="category" required defaultValue=""><option value="" disabled>Sélectionner une catégorie</option>{Object.entries(categories).map(([k,v]) => <option value={k} key={k}>{v}</option>)}</select></label><label>Priorité<select name="priority" required defaultValue="MEDIUM">{Object.entries(priorityLabels).map(([k,v]) => <option value={k} key={k}>{v}</option>)}</select></label></div><label>Description<textarea name="description" rows={7} maxLength={5000} required placeholder="Précisez ce que vous avez constaté, quand le problème est apparu et les équipements concernés…" /></label><div className="form-actions"><Link className="button secondary" to="/my-incidents">Annuler</Link><button className="button" disabled={busy}>{busy ? 'Envoi en cours…' : 'Envoyer le signalement'}<ArrowRight size={17} /></button></div></form><aside className="help-panel"><ShieldCheck size={26} /><h2>Les bons réflexes.</h2><p>Décrivez les faits aussi précisément que possible.</p><ul><li>Indiquez la date et l’heure du problème.</li><li>Précisez les services ou appareils touchés.</li><li>Ne partagez jamais votre mot de passe.</li><li>En cas de doute, choisissez la catégorie « Autre ».</li></ul><div className="help-bottom">Votre signalement sera visible par vous, l’administrateur et le technicien affecté.</div></aside></div></>
}
