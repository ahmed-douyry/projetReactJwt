import type { Status, Priority, Role } from '../types'
export const statusLabels: Record<Status, string> = { OPEN: 'Ouvert', ASSIGNED: 'Affecté', IN_PROGRESS: 'En cours', RESOLVED: 'Résolu', CLOSED: 'Clôturé' }
export const priorityLabels: Record<Priority, string> = { LOW: 'Faible', MEDIUM: 'Moyenne', HIGH: 'Haute', CRITICAL: 'Critique' }
export const roleLabels: Record<Role, string> = { EMPLOYEE: 'Employé', TECHNICIAN: 'Technicien', ADMIN: 'Administrateur' }
export const categories: Record<string, string> = { NETWORK: 'Réseau', MALWARE: 'Malware', PHISHING: 'Phishing', ACCESS: 'Accès aux comptes', SOFTWARE: 'Logiciel', HARDWARE: 'Matériel', DATA_LEAK: 'Fuite de données', OTHER: 'Autre' }
export function date(value: string, time = false) { return new Intl.DateTimeFormat('fr-MA', { day: '2-digit', month: 'short', ...(time ? { hour: '2-digit', minute: '2-digit' } : {year: 'numeric'}) }).format(new Date(value)) }
export function initials(name: string) { return name.split(' ').map(x => x[0]).slice(0, 2).join('').toUpperCase() }
