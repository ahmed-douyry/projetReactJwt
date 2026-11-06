import { NavLink, Link, Outlet, useLocation } from 'react-router-dom'
import { ShieldCheck, LayoutDashboard, Files, Plus, Users, UserRound, LogOut, ArrowUpRight, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { authService } from '../services/authService'
import { initials, roleLabels } from '../services/format'
export default function Navbar() {
  const { session, signOut } = useAuth(); const [open, setOpen] = useState(false); const location = useLocation()
  if (!session) return null
  const role = session.role
  const incidentPath = role === 'ADMIN' ? '/admin/incidents' : role === 'TECHNICIAN' ? '/assigned-incidents' : '/my-incidents'
  const nav = [{ to: '/', title: 'Vue d’ensemble', Icon: LayoutDashboard }, { to: incidentPath, title: role === 'TECHNICIAN' ? 'Incidents affectés' : role === 'ADMIN' ? 'Tous les incidents' : 'Mes incidents', Icon: Files }, ...(role === 'ADMIN' ? [{ to: '/admin/users', title: 'Utilisateurs', Icon: Users }] : []), ...(role === 'EMPLOYEE' ? [{ to: '/incidents/new', title: 'Déclarer un incident', Icon: Plus }] : [])]
  const title = location.pathname === '/' ? 'Tableau de bord' : location.pathname.includes('profile') ? 'Mon profil' : location.pathname.includes('users') ? 'Administration' : 'Gestion des incidents'
  async function logout() { try { await authService.logout() } finally { signOut() } }
  return <div className="app-shell"><button className="mobile-toggle icon-button" aria-label={open ? 'Fermer le menu' : 'Ouvrir le menu'} onClick={() => setOpen(!open)}>{open ? <X /> : <Menu />}</button>{open && <button className="sidebar-scrim" aria-label="Fermer le menu" onClick={() => setOpen(false)} />}
    <aside className={`sidebar ${open ? 'is-open' : ''}`}><Link to="/" className="brand"><span className="brand-mark"><ShieldCheck size={24} /></span>sentinel<span className="brand-dot">.</span></Link><span className="workspace-label">ESPACE {roleLabels[role].toUpperCase()}</span><nav aria-label="Navigation principale">{nav.map(({to, title, Icon}) => <NavLink to={to} key={to} end={to === '/'} onClick={() => setOpen(false)} className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}><Icon size={19} />{title}</NavLink>)}</nav>
    <div className="sidebar-bottom"><div className="sidebar-note"><span className="live-dot" /><strong>Un environnement plus sûr.</strong><p>Chaque signalement compte.</p></div><NavLink className="nav-item" to="/profile" onClick={() => setOpen(false)}><UserRound size={19} />Mon profil</NavLink><button className="nav-item logout" onClick={() => void logout().catch(() => {})}><LogOut size={19} />Se déconnecter</button><div className="sidebar-user"><span className="avatar">{initials(session.fullName)}</span><div><strong>{session.fullName}</strong><span>{roleLabels[role]}</span></div></div></div></aside>
    <div className="main-shell"><header className="topbar"><span className="breadcrumb">Espace de travail <span>/</span> <strong>{title}</strong></span><div className="topbar-right"><span className="security-indicator"><ShieldCheck size={14} /> Session sécurisée</span><Link to="/profile" className="avatar small" aria-label="Mon profil">{initials(session.fullName)}</Link></div></header><main><Outlet /></main><footer className="app-footer"><span>sentinel. <span className="muted">Gestion des incidents</span></span><span>Spring Security · JWT · React <ArrowUpRight size={13} /></span></footer></div></div>
}
