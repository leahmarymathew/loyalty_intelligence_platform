import { Link } from "react-router-dom";

/**
 * Not part of the product surfaces — a scaffold-only landing page so `/`
 * isn't a dead end while there's no auth/tenant-resolution to route on yet.
 */
export function RootLandingPage() {
  return (
    <div style={{ padding: "2rem", fontFamily: "system-ui, sans-serif" }}>
      <h1>Loyalty Intelligence Platform — web</h1>
      <p>Scaffold. Pick a surface:</p>
      <ul>
        <li>
          <Link to="/dashboard">Brand dashboard</Link>
        </li>
        <li>
          <Link to="/customer">Customer app</Link>
        </li>
      </ul>
    </div>
  );
}
