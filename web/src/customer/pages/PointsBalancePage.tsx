import { Card } from "../../shared/components/Card";
import {
  computeStubBalance,
  stubCustomer,
  stubLedgerEntries,
  stubTiers,
} from "../../shared/api/stubData";
import { formatDate, formatPoints } from "../../shared/format";

/**
 * Customer-facing points balance screen. Stub data only — core-api's ledger
 * service exists but has no REST layer yet (see root README "Status"), so
 * the balance shown here is computed the same way core-api computes it
 * (sum over ledger entries, see computeStubBalance) but against local stub
 * data rather than a live endpoint.
 */
export function PointsBalancePage() {
  const balance = computeStubBalance(stubCustomer.id);
  const tier = stubTiers.find((t) => t.id === stubCustomer.currentTierId);
  const recentEntries = [...stubLedgerEntries]
    .filter((entry) => entry.customerId === stubCustomer.id)
    .sort((a, b) => (a.occurredAt < b.occurredAt ? 1 : -1));

  return (
    <div>
      <div className="balance-hero">
        <span className="balance-number">{formatPoints(balance.balance)}</span>
        <span>points</span>
        {tier && <div className="tier-badge">{tier.name} tier</div>}
      </div>

      <Card title="Recent activity">
        <ul className="ledger-list">
          {recentEntries.map((entry) => (
            <li key={entry.id}>
              <span>
                {entry.reference ?? entry.entryType}
                <br />
                <small>{formatDate(entry.occurredAt)}</small>
              </span>
              <span
                className={`ledger-points ${entry.points >= 0 ? "positive" : "negative"}`}
              >
                {entry.points >= 0 ? "+" : ""}
                {formatPoints(entry.points)}
              </span>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
}
