import { Card } from "../../shared/components/Card";
import { stubCampaigns } from "../../shared/api/stubData";
import { formatDate } from "../../shared/format";
import type { Campaign } from "../../shared/types/domain";

function statusClass(status: Campaign["status"]): string {
  return status.toLowerCase();
}

/**
 * Campaign list for the currently-selected tenant. Stub data only — the
 * campaign worker and its REST endpoints are not built in core-api yet (see
 * root README "Status").
 */
export function CampaignsPage() {
  return (
    <div>
      <h1>Campaigns</h1>
      <p>Points and bonus campaigns for Acme Outfitters.</p>
      <Card title="Campaigns">
        <ul className="campaign-list">
          {stubCampaigns.map((campaign) => (
            <li key={campaign.id}>
              <span>
                <strong>{campaign.name}</strong>
                <br />
                <small>{campaign.description}</small>
                <br />
                <small>
                  {formatDate(campaign.startsAt)} – {formatDate(campaign.endsAt)}
                </small>
              </span>
              <span className={`status-badge ${statusClass(campaign.status)}`}>
                {campaign.status}
              </span>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
}
