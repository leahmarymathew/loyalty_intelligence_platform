import { Outlet } from "react-router-dom";
import "./customer.css";

/**
 * Shell for the mobile-first, installable customer PWA. Kept visually and
 * structurally separate from the dashboard shell (src/dashboard/) even
 * though both live in one Vite app — this is the surface the PWA manifest
 * and service worker target (see vite.config.ts).
 */
export function CustomerLayout() {
  return (
    <div className="customer-shell">
      <header className="customer-header">Acme Outfitters Rewards</header>
      <main className="customer-content">
        <Outlet />
      </main>
    </div>
  );
}
