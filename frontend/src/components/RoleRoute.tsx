import { Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { Role } from '../types'
import { Link } from 'react-router-dom'
export default function RoleRoute({ roles }: { roles: Role[] }) {
  const { session } = useAuth()
  return session && roles.includes(session.role) ? <Outlet /> : <section className="empty panel"><h1>Accès interdit</h1><p>Votre rôle ne permet pas de consulter cette page.</p><Link className="button" to="/">Revenir au tableau de bord</Link></section>
}
