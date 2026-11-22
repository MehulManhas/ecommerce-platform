import { useEffect, useState } from 'react'
import client from '../api/client'
import { useCart } from '../context/CartContext.jsx'

export default function Catalog() {
  const { addItem } = useCart()
  const [products, setProducts] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    client.get('/products')
      .then(({ data }) => setProducts(data.productList || []))
      .catch(() => setError('Could not load products. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  const filtered = products.filter((p) =>
    (p.title || '').toLowerCase().includes(query.toLowerCase()),
  )

  if (loading) return <p>Loading…</p>
  if (error) return <p className="error">{error}</p>

  return (
    <div>
      <input
        className="search"
        placeholder="Search products…"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      <div className="grid">
        {filtered.map((p) => (
          <div className="card product" key={p.id}>
            <h3>{p.title}</h3>
            <p className="muted">{p.category}</p>
            <p className="price">₹{p.price}</p>
            <button onClick={() => addItem(p)}>Add to cart</button>
          </div>
        ))}
        {filtered.length === 0 && <p>No products found.</p>}
      </div>
    </div>
  )
}
