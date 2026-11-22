import { useEffect, useState } from 'react'
import client from '../api/client'

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [paying, setPaying] = useState(null)

  // The backend scopes /orders to the authenticated user via the bearer token,
  // so no userId is sent from the client.
  function loadOrders() {
    client.get('/orders')
      .then(({ data }) => setOrders(data))
      .catch(() => setError('Could not load orders.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadOrders() }, [])

  function pay(order) {
    setPaying(order.id)
    setError('')
    client.post('/payments', { orderId: order.id, amount: order.totalAmount })
      .then(() => {
        // Settlement is asynchronous (payment.completed -> order becomes PAID),
        // so re-fetch after a short delay to show the updated status.
        setTimeout(loadOrders, 1200)
      })
      .catch(() => setError('Payment failed.'))
      .finally(() => setPaying(null))
  }

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
              <li key={idx}>{it.productTitle} × {it.quantity} - ₹{it.unitPrice}</li>
            ))}
          </ul>
          {o.status === 'CREATED' && (
            <button onClick={() => pay(o)} disabled={paying === o.id}>
              {paying === o.id ? 'Paying…' : 'Pay now'}
            </button>
          )}
        </div>
      ))}
    </div>
  )
}
