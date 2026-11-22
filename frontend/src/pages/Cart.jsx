import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import client from '../api/client'

export default function Cart() {
  const { items, updateQty, removeItem, clear, total } = useCart()
  const { auth } = useAuth()
  const navigate = useNavigate()
  const [placing, setPlacing] = useState(false)
  const [error, setError] = useState('')

  async function checkout() {
    if (!auth) {
      navigate('/login')
      return
    }
    setPlacing(true)
    setError('')
    try {
      await client.post('/orders', {
        items: items.map((i) => ({ productId: i.id, quantity: i.quantity })),
      })
      clear()
      navigate('/orders')
    } catch {
      setError('Checkout failed. Make sure you are logged in and the services are running.')
    } finally {
      setPlacing(false)
    }
  }

  if (items.length === 0) return <p>Your cart is empty.</p>

  return (
    <div className="card">
      <h2>Cart</h2>
      {items.map((i) => (
        <div className="cart-row" key={i.id}>
          <span className="cart-title">{i.title}</span>
          <input
            type="number"
            min="1"
            value={i.quantity}
            onChange={(e) => updateQty(i.id, parseInt(e.target.value, 10) || 1)}
          />
          <span className="price">₹{(i.price * i.quantity).toFixed(2)}</span>
          <button onClick={() => removeItem(i.id)}>Remove</button>
        </div>
      ))}
      <p className="total">Total: ₹{total.toFixed(2)}</p>
      {error && <p className="error">{error}</p>}
      <button disabled={placing} onClick={checkout}>
        {placing ? 'Placing…' : 'Checkout'}
      </button>
    </div>
  )
}
