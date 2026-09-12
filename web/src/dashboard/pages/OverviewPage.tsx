import { Card } from "../../shared/components/Card";
import { stubTenants } from "../../shared/api/stubData";
import { formatDate } from "../../shared/format";

/**
 * Dashboard landing page: lists tenants on the platform. Stub data only —
 * core-api has no REST layer yet to fetch this from (see
 * src/shared/api/stubData.ts).
 */
export function OverviewPage() {
  return (
    <div>
      <h1>Tenants</h1>
      <p>Retail brands currently on the platform.</p>
      <div className="dashboard-grid">
        <Card title="Tenants">
          <ul className="tenant-list">
            {stubTenants.map((tenant) => (
              <li key={tenant.id}>
                <span>{tenant.name}</span>
                <span>
                  <code>{tenant.slug}</code> · since {formatDate(tenant.createdAt)}
                </span>
              </li>
            ))}
          </ul>
        </Card>
      </div>
    </div>
  );
}
