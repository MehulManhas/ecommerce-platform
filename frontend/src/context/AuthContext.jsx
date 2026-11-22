import { createContext, useContext, useState } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('token')
    if (!token) return null
    return {
      token,
      userId: localStorage.getItem('userId'),
      email: localStorage.getItem('email'),
    }
  })

  async function login(email, password) {
    const { data } = await client.post('/auth/login', { email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('email', data.email)
    setAuth({ token: data.token, userId: data.userId, email: data.email })
  }

  async function signup(email, password) {
    await client.post('/auth/signup', { email, password })
    await login(email, password)
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('email')
    setAuth(null)
  }

  return (
    <AuthContext.Provider value={{ auth, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
