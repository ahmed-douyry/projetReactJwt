import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { LockKeyhole, Save } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { authService } from '../services/authService'
import { errorMessage } from '../services/api'
import { ErrorAlert } from '../components/ui'
import { initials, roleLabels, date } from '../services/format'
export default function Profile() {
  const { session, updateUser, signOut, signIn } = useAuth(); const navigate = useNavigate()
  const [error, setError] = useState(''); const [notice, setNotice] = useState(''); const [busy, setBusy] = useState(false)
  const user = session!.user
  async function profile(e: FormEvent<HTMLFormElement>) {
    e.preventDefault(); const values = new FormData(e.currentTarget); setError(''); setNotice(''); setBusy(true)
    try { const next = await authService.updateProfile(String(values.get('fullName')), String(values.get('email'))); if(next.email !== user.email) { signOut(); navigate('/login', {state:{message:'Email modifié. Veuillez vous reconnecter.'}}) } else { updateUser(next); setNotice('Votre profil a été mis à jour.') } } catch(e) { setError(errorMessage(e)) } finally { setBusy(false) }
  }
  async function password(e: FormEvent<HTMLFormElement>) {
    e.preventDefault(); const v = new FormData(e.currentTarget); setError(''); setNotice('');
    if(v.get('newPassword') !== v.get('confirmation')) { setError('La confirmation ne correspond pas au nouveau mot de passe.'); return }
    setBusy(true)
    try { await authService.password(String(v.get('oldPassword')), String(v.get('newPassword')), String(v.get('confirmation'))); signOut(); navigate('/login', {state:{message:'Mot de passe modifié. Veuillez vous reconnecter.'}}) } catch(e) { setError(errorMessage(e)) } finally { setBusy(false) }
  }
  async function renew() { setBusy(true); setError(''); setNotice(''); try { signIn(await authService.refresh(session!.refreshToken)); setNotice('Session renouvelée pour cinq minutes.') } catch(e) { setError(errorMessage(e)) } finally { setBusy(false) } }
  return <><div className="page-heading"><div><span className="eyebrow">MON COMPTE</span><h1>Votre profil<span className="serif-dot">.</span></h1><p>Mettez à jour vos informations et protégez votre compte.</p></div></div>{error && <ErrorAlert message={error} />}{notice && <div className="alert success" role="status">{notice}</div>}<div className="profile-layout"><aside className="panel profile-card"><span className="avatar large">{initials(user.fullName)}</span><h2>{user.fullName}</h2><p>{user.email}</p><span className="badge status-resolved">{roleLabels[user.role]}</span><div className="profile-since">Membre depuis le {date(user.createdAt)}<p>Session jusqu’à {new Date(session!.expiresAt).toLocaleTimeString('fr-MA', {hour:'2-digit',minute:'2-digit'})}</p><button type="button" className="button secondary compact" disabled={busy} onClick={() => void renew()}>Renouveler la session</button></div></aside><div><form className="panel form-panel" onSubmit={profile}><h2>Informations personnelles</h2><label>Nom complet<input name="fullName" defaultValue={user.fullName} maxLength={100} autoComplete="name" required /></label><label>Adresse email<input type="email" name="email" defaultValue={user.email} maxLength={254} autoComplete="email" required /></label><p className="form-hint">Modifier votre email vous demandera de vous reconnecter.</p><div className="form-actions"><button className="button" disabled={busy}><Save size={16} />Enregistrer</button></div></form><form className="panel form-panel password-panel" onSubmit={password}><h2><LockKeyhole size={19} />Changer le mot de passe</h2><label>Ancien mot de passe<input name="oldPassword" type="password" autoComplete="current-password" required /></label><div className="form-row"><label>Nouveau mot de passe<input name="newPassword" type="password" minLength={8} maxLength={72} autoComplete="new-password" required /></label><label>Confirmation<input name="confirmation" type="password" minLength={8} maxLength={72} autoComplete="new-password" required /></label></div><p className="form-hint">8 caractères minimum. Toutes vos sessions seront révoquées.</p><div className="form-actions"><button className="button" disabled={busy}>Modifier le mot de passe</button></div></form></div></div></>
}
