import { createContext, useContext } from 'react'
import type { Session, Person } from '../types'
export interface AuthState {
  session: Session | null
  expired: boolean
  signIn: (session: Session) => void
  signOut: (expired?: boolean) => void
  updateUser: (user: Person) => void
}
export const AuthContext = createContext<AuthState | null>(null)
export function useAuth() { const context = useContext(AuthContext); if (!context) throw new Error('AuthProvider absent'); return context }
