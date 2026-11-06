export type Role = 'EMPLOYEE' | 'TECHNICIAN' | 'ADMIN'
export type Status = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export interface Person { id: number; fullName: string; email: string; role: Role; enabled: boolean; createdAt: string }
export interface Session { token: string; refreshToken: string; expiresAt: string; role: Role; fullName: string; user: Person }
export interface Incident { id: number; title: string; description: string; category: string; priority: Priority; status: Status; createdAt: string; updatedAt: string; createdBy: Person; assignedTo: Person | null; history: {id: number; message: string; author: Person; createdAt: string}[]; comments: {id: number; content: string; author: Person; createdAt: string}[] }
