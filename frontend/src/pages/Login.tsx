import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ShieldCheck, ArrowRight, Eye, EyeOff, LockKeyhole } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { authService } from '../services/authService'
import { errorMessage } from '../services/api'
import { ErrorAlert } from '../components/ui'

export default function Login({ register = false }: { register?: boolean }) {
  const { session, signIn, expired } = useAuth(); const navigate = useNavigate(); const location = useLocation()
  const [busy, setBusy] = useState(false); const [error, setError] = useState(''); const [show, setShow] = useState(false)
  const [email, setEmail] = useState(''); const [password, setPassword] = useState(''); const [name, setName] = useState('')
  if (session) return <Navigate to="/" replace />
  async function submit(e: FormEvent) {
    e.preventDefault(); setError(''); setBusy(true)
    try { signIn(register ? await authService.register(name, email, password) : await authService.login(email, password)); navigate(location.state?.from || '/', { replace: true }) }
    catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }
  return <div className="auth-shell"><section className="auth-story"><Link className="brand" to="/login"><span className="brand-mark"><ShieldCheck size={24} /></span>sentinel<span className="brand-dot">.</span></Link><div className="auth-story-content"><span className="eyebrow light">GESTION DES INCIDENTS · SÉCURITÉ IT</span><h1>Un incident signalé.<br />Une équipe<br /><em>qui prend le relais.</em></h1><p>Déclarez, suivez et résolvez les incidents de votre entreprise, dans un espace sécurisé.</p><div className="auth-workflow"><span>01<br /><strong>Signaler</strong></span><i /><span>02<br /><strong>Prendre en charge</strong></span><i /><span>03<br /><strong>Résoudre</strong></span></div></div><div className="auth-footnote"><LockKeyhole size={15} /> Accès protégé · Autorisations selon votre rôle</div></section><section className="auth-form-side"><div className="auth-form-wrap"><span className="eyebrow">VOTRE ESPACE DE TRAVAIL</span><h2>{register ? 'Créer un compte.' : 'Heureux de vous retrouver.'}</h2><p className="auth-intro">{register ? 'Rejoignez votre équipe et signalez votre premier incident.' : 'Connectez-vous pour accéder à votre espace de gestion.'}</p>
    {expired && !register && <div className="alert" role="status">Votre session a expiré. Veuillez vous reconnecter.</div>}{location.state?.message && <div className="alert">{location.state.message}</div>}{error && <ErrorAlert message={error} />}
    <form onSubmit={submit}>{register && <label>Nom complet<input autoComplete="name" value={name} onChange={e => setName(e.target.value)} maxLength={100} required placeholder="Ahmed Benali" /></label>}<label>Adresse email<input type="email" autoComplete="email" value={email} onChange={e => setEmail(e.target.value)} maxLength={254} required placeholder="vous@entreprise.ma" /></label><label>Mot de passe<div className="password-field"><input type={show ? 'text' : 'password'} autoComplete={register ? 'new-password' : 'current-password'} value={password} onChange={e => setPassword(e.target.value)} minLength={register ? 8 : undefined} maxLength={72} required placeholder={register ? '8 caractères minimum' : 'Votre mot de passe'} /><button type="button" className="icon-button" aria-label={show ? 'Masquer le mot de passe' : 'Afficher le mot de passe'} onClick={() => setShow(!show)}>{show ? <EyeOff size={18} /> : <Eye size={18} />}</button></div></label><button disabled={busy} className="button full" type="submit">{busy ? 'Veuillez patienter…' : register ? 'Créer mon compte' : 'Se connecter'}<ArrowRight size={18} /></button></form><p className="auth-switch">{register ? 'Déjà un compte ?' : 'Vous nous rejoignez ?'} <Link to={register ? '/login' : '/register'}>{register ? 'Se connecter' : 'Créer un compte'}</Link></p>
    {!register && <details className="demo-accounts"><summary>Essayer avec un compte de démonstration</summary><p>Mot de passe commun : <code>Demo2026!</code></p><div>{[{label:'Employé', email:'employe@sentinel.ma'}, {label:'Technicien', email:'technicien@sentinel.ma'}, {label:'Admin', email:'admin@sentinel.ma'}].map(a => <button className="button secondary" type="button" key={a.email} onClick={() => { setEmail(a.email); setPassword('Demo2026!') }}>{a.label}</button>)}</div></details>}
    {register && <p className="form-hint"><ShieldCheck size={15} /> Votre compte sera créé avec le rôle Employé.</p>}</div><span className="auth-bottom">Sécurité des systèmes distribués · Projet d’application</span></section></div>
}
