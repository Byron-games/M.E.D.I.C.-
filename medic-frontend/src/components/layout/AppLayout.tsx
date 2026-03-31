import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'

export function AppLayout() {
  return (
    <div className="flex min-h-screen bg-void">
      <Sidebar />
      <main className="flex-1 min-w-0 overflow-auto">
        <div className="p-8 animate-fade-in">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
