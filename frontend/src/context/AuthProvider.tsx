import { useState, useEffect, useCallback } from 'react'
import type { ReactNode } from 'react'
import { AuthContext } from './AuthContext'
import { readSession, SESSION_KEY } from '../services/api'
import { authService } from '../services/authService'
import type { Person, Session } from '../types'
export default function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(readSession)
  const [expired, setExpired] = useState(false)
  const signOut = useCallback((isExpired = false) => {
    localStorage.removeItem(SESSION_KEY); setSession(null); setExpired(isExpired)
  }, [])
  useEffect(() => {
    const onExpiry = () => signOut(true)
    const onStorage = () => setSession(readSession())
    window.addEventListener('sentinel:expired', onExpiry); window.addEventListener('storage', onStorage)
    return () => { window.removeEventListener('sentinel:expired', onExpiry); window.removeEventListener('storage', onStorage) }
  }, [signOut])
  useEffect(() => {
    if (!session) return
    const delay = new Date(session.expiresAt).getTime() - Date.now()
    const timer = window.setTimeout(() => signOut(true), Math.max(0, delay))
    return () => window.clearTimeout(timer)
  }, [session, signOut])
  useEffect(() => { if (readSession()) authService.profile().catch(() => {}) }, [])
  function signIn(value: Session) { localStorage.setItem(SESSION_KEY, JSON.stringify(value)); setSession(value); setExpired(false) }
  function updateUser(user: Person) { if (session) signIn({ ...session, user, fullName: user.fullName }) }
  return <AuthContext.Provider value={{ session, expired, signIn, signOut, updateUser }}>{children}</AuthContext.Provider>
}
