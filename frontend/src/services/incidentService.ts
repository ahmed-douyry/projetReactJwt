import { api } from './api'
import type { Incident, Role, Status, Person } from '../types'
export const incidentService = {
  list: (role: Role) => api.get<Incident[]>(role === 'ADMIN' ? '/api/incidents' : role === 'TECHNICIAN' ? '/api/incidents/assigned' : '/api/incidents/my').then(r => r.data),
  detail: (id: string) => api.get<Incident>(`/api/incidents/${id}`).then(r => r.data),
  create: (data: {title: string; description: string; category: string; priority: string}) => api.post<Incident>('/api/incidents', data).then(r => r.data),
  assign: (id: number, technician: number) => api.put<Incident>(`/api/incidents/${id}/assign/${technician}`).then(r => r.data),
  status: (id: number, status: Status) => api.put<Incident>(`/api/incidents/${id}/status`, { status }).then(r => r.data),
  comment: (id: number, content: string) => api.post<Incident>(`/api/incidents/${id}/comments`, { content }).then(r => r.data),
}
export const adminService = {
  users: () => api.get<Person[]>('/api/admin/users').then(r => r.data),
  enabled: (id: number, enabled: boolean) => api.put<Person>(`/api/admin/users/${id}/${enabled ? 'enable' : 'disable'}`).then(r => r.data),
  statistics: () => api.get<Record<string, number>>('/api/admin/statistics').then(r => r.data),
}
