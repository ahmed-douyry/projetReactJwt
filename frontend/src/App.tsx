import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import AuthProvider from './context/AuthProvider'
import ProtectedRoute from './components/ProtectedRoute'
import RoleRoute from './components/RoleRoute'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Profile from './pages/Profile'
import MyIncidents from './pages/MyIncidents'
import AssignedIncidents from './pages/AssignedIncidents'
import AdminIncidents from './pages/AdminIncidents'
import NewIncident from './pages/NewIncident'
import IncidentDetails from './pages/IncidentDetails'
import AdminUsers from './pages/AdminUsers'
export default function App() {
  return <BrowserRouter><AuthProvider><Routes><Route path="/login" element={<Login />} /><Route path="/register" element={<Register />} /><Route element={<ProtectedRoute />}><Route element={<Navbar />}><Route index element={<Dashboard />} /><Route path="profile" element={<Profile />} /><Route path="incidents/:id" element={<IncidentDetails />} /><Route element={<RoleRoute roles={['EMPLOYEE']} />}><Route path="my-incidents" element={<MyIncidents />} /><Route path="incidents/new" element={<NewIncident />} /></Route><Route element={<RoleRoute roles={['TECHNICIAN']} />}><Route path="assigned-incidents" element={<AssignedIncidents />} /></Route><Route element={<RoleRoute roles={['ADMIN']} />}><Route path="admin/incidents" element={<AdminIncidents />} /><Route path="admin/users" element={<AdminUsers />} /></Route><Route path="*" element={<section className="empty panel"><h1>Page introuvable</h1><Link className="button" to="/">Revenir à l’accueil</Link></section>} /></Route></Route></Routes></AuthProvider></BrowserRouter>
}
