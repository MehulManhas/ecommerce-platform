import { useEffect, useState } from 'react'
import client from '../api/client'
import { useAuth } from '../context/AuthContext.jsx'

export default function Orders() {
  const { auth } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    client.get('/orders', { params: { userId: auth.userId } })
      .then(({ data }) => setOrders(data))
      .catch(() => setError('Could not load orders.'))
      .finally(() => setLoading(false))
  }, [auth.userId])

  if (loading) return <p>Loading…</p>
  if (error) return <p className="error">{error}</p>
  if (orders.length === 0) return <p>No orders yet.</p>

  return (
    <div>
      <h2>Your Orders</h2>
      {orders.map((o) => (
        <div className="card" key={o.id}>
          <div className="order-head">
            <span className="muted">#{o.id}</span>
            <span className="badge">{o.status}</span>
            <span className="price">₹{o.totalAmount}</span>
          </div>
          <ul>
            {o.items.map((it, idx) => (
              <li key={idx}>{it.productTitle} × {it.quantity} — ₹{it.unitPrice}</li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  )
}
