import { Route, Routes } from "react-router-dom";
import { DashboardLayout } from "./dashboard/DashboardLayout";
import { OverviewPage } from "./dashboard/pages/OverviewPage";
import { CampaignsPage } from "./dashboard/pages/CampaignsPage";
import { CustomerLayout } from "./customer/CustomerLayout";
import { PointsBalancePage } from "./customer/pages/PointsBalancePage";
import { RootLandingPage } from "./RootLandingPage";

/**
 * Two route groups sharing one Vite app, per the README's "brand-side
 * dashboard and a ... customer PWA": /dashboard/* is the internal brand
 * tool, /customer/* is the installable customer-facing surface.
 */
export function App() {
  return (
    <Routes>
      <Route path="/" element={<RootLandingPage />} />

      <Route path="/dashboard" element={<DashboardLayout />}>
        <Route index element={<OverviewPage />} />
        <Route path="campaigns" element={<CampaignsPage />} />
      </Route>

      <Route path="/customer" element={<CustomerLayout />}>
        <Route index element={<PointsBalancePage />} />
      </Route>
    </Routes>
  );
}

export default App;
