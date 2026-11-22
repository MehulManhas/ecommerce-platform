# E-Commerce Frontend

A React + Vite single-page storefront for the platform: login/signup, product
catalog with search, cart, checkout (places an order), and order history. It
talks to the **API gateway** on `:8080`; the Vite dev server proxies `/api`
there (see `vite.config.js`), so no backend CORS config is needed in development.

## Run

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

The backend must be running (gateway + user, product, order services, and their
infra). Flow: sign up → browse the catalog → add to cart → checkout → see it
under Orders.

## Build

```bash
npm run build      # outputs to dist/
```

## Notes

- The JWT from login is stored in `localStorage` and sent as
  `Authorization: Bearer <token>` by an axios interceptor (`src/api/client.js`).
- Catalog search filters the loaded product list client-side.
- To point at a different gateway host, change the proxy target in `vite.config.js`.
