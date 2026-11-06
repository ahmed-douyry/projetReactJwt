import { api } from './api'
import type { Session, Person } from '../types'
export const authService = {
  login: (email: string, password: string) => api.post<Session>('/auth/login', { email, password }).then(r => r.data),
  register: (fullName: string, email: string, password: string) => api.post<Session>('/auth/register', { fullName, email, password }).then(r => r.data),
  profile: () => api.get<Person>('/api/profile').then(r => r.data),
  updateProfile: (fullName: string, email: string) => api.put<Person>('/api/profile', { fullName, email }).then(r => r.data),
  password: (oldPassword: string, newPassword: string, confirmation: string) => api.put('/api/profile/password', { oldPassword, newPassword, confirmation }),
  refresh: (refreshToken: string) => api.post<Session>('/auth/refresh', { refreshToken }).then(r => r.data),
  logout: () => api.post('/api/logout'),
}
