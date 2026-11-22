import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

export default function Navbar() {
  const { auth, logout } = useAuth()
  const { items } = useCart()
  const navigate = useNavigate()
  const count = items.reduce((s, i) => s + i.quantity, 0)

  return (
    <nav className="navbar">
      <Link to="/" className="brand">E-Store</Link>
      <div className="nav-links">
        <Link to="/">Catalog</Link>
        <Link to="/cart">Cart ({count})</Link>
        {auth ? (
          <>
            <Link to="/orders">Orders</Link>
            <span className="user">{auth.email}</span>
            <button onClick={() => { logout(); navigate('/') }}>Logout</button>
          </>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </div>
    </nav>
  )
}
