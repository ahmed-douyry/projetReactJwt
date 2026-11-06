import axios from 'axios'
import type { Session } from '../types'
export const SESSION_KEY = 'sentinel.session'
export function readSession(): Session | null {
  try { const s = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null'); return s?.token && s?.user ? s : null }
  catch { return null }
}
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081', timeout: 15000 })
api.interceptors.request.use(config => {
  const session = readSession()
  if (session && !config.url?.startsWith('/auth/')) config.headers.Authorization = `Bearer ${session.token}`
  return config
})
api.interceptors.response.use(response => response, error => {
  if (error.response?.status === 401 && !error.config?.url?.startsWith('/auth/')) {
    localStorage.removeItem(SESSION_KEY)
    window.dispatchEvent(new Event('sentinel:expired'))
  }
  return Promise.reject(error)
})
export function errorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    if (!error.response) return 'Le serveur est injoignable. Vérifiez que le backend est démarré sur le port 8081.'
    const fields = error.response.data?.errors as Record<string, string> | undefined
    return error.response.data?.message + (fields && Object.keys(fields).length ? ' : ' + Object.keys(fields).join(', ') : '') || 'Une erreur est survenue.'
  }
  return 'Une erreur est survenue.'
}
