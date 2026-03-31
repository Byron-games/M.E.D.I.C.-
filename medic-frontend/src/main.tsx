import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: '#161B22',
            color: '#C9D1D9',
            border: '1px solid #21262D',
            fontFamily: '"DM Sans", sans-serif',
            fontSize: '14px',
          },
          success: { iconTheme: { primary: '#00FF88', secondary: '#080C14' } },
          error:   { iconTheme: { primary: '#FF4D6A', secondary: '#080C14' } },
        }}
      />
    </BrowserRouter>
  </React.StrictMode>
)
