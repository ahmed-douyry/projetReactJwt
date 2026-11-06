import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
export default function ProtectedRoute() {
  const { session } = useAuth(); const location = useLocation()
  return session ? <Outlet /> : <Navigate to="/login" state={{ from: location.pathname }} replace />
}
