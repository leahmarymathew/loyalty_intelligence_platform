import { NavLink, Outlet } from "react-router-dom";
import "./dashboard.css";

/**
 * Shell for the brand-side dashboard. This is the internal tool brand staff
 * use to see tenant/customer/campaign data — not the installable surface
 * (that's the customer PWA, see src/customer/).
 */
export function DashboardLayout() {
  return (
    <div className="dashboard-shell">
      <aside className="dashboard-sidebar">
        <div className="dashboard-brand">Loyalty Intelligence</div>
        <nav>
          <NavLink to="/dashboard" end>
            Overview
          </NavLink>
          <NavLink to="/dashboard/campaigns">Campaigns</NavLink>
        </nav>
      </aside>
      <main className="dashboard-content">
        <Outlet />
      </main>
    </div>
  );
}
