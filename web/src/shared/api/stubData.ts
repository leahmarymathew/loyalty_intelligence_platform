/**
 * Stub/placeholder data standing in for core-api responses until the REST
 * layer is built (see src/shared/api/client.ts). Shapes match
 * src/shared/types/domain.ts.
 */
import type {
  Campaign,
  Customer,
  PointsBalance,
  PointsLedgerEntry,
  Tenant,
  Tier,
} from "../types/domain";

export const stubTenants: Tenant[] = [
  {
    id: "11111111-1111-4111-8111-111111111111",
    slug: "acme-outfitters",
    name: "Acme Outfitters",
    createdAt: "2025-01-15T09:00:00Z",
  },
  {
    id: "22222222-2222-4222-8222-222222222222",
    slug: "north-star-coffee",
    name: "North Star Coffee Co.",
    createdAt: "2025-03-02T09:00:00Z",
  },
];

export const stubTiers: Tier[] = [
  {
    id: "t-bronze",
    tenantId: stubTenants[0].id,
    name: "Bronze",
    minPoints: 0,
    sortOrder: 0,
  },
  {
    id: "t-silver",
    tenantId: stubTenants[0].id,
    name: "Silver",
    minPoints: 500,
    sortOrder: 1,
  },
  {
    id: "t-gold",
    tenantId: stubTenants[0].id,
    name: "Gold",
    minPoints: 2000,
    sortOrder: 2,
  },
];

export const stubCampaigns: Campaign[] = [
  {
    id: "c-1",
    tenantId: stubTenants[0].id,
    name: "Fall Double Points Weekend",
    description: "Earn 2x points on all purchases, Fri-Sun.",
    startsAt: "2026-09-18T00:00:00Z",
    endsAt: "2026-09-21T00:00:00Z",
    status: "ACTIVE",
  },
  {
    id: "c-2",
    tenantId: stubTenants[0].id,
    name: "Welcome Bonus",
    description: "500 bonus points for new members on first purchase.",
    startsAt: "2026-01-01T00:00:00Z",
    endsAt: "2026-12-31T00:00:00Z",
    status: "ACTIVE",
  },
  {
    id: "c-3",
    tenantId: stubTenants[0].id,
    name: "Summer Kickoff",
    description: "Triple points on outdoor gear.",
    startsAt: "2025-06-01T00:00:00Z",
    endsAt: "2025-06-30T00:00:00Z",
    status: "ENDED",
  },
];

export const stubCustomer: Customer = {
  id: "cu-1",
  tenantId: stubTenants[0].id,
  externalRef: "POS-88213",
  email: "jordan.rivera@example.com",
  fullName: "Jordan Rivera",
  birthDate: "1994-05-11",
  currentTierId: "t-silver",
  enrolledAt: "2025-02-10T14:30:00Z",
  createdAt: "2025-02-10T14:30:00Z",
};

export const stubLedgerEntries: PointsLedgerEntry[] = [
  {
    id: "le-1",
    tenantId: stubTenants[0].id,
    customerId: stubCustomer.id,
    transactionId: "tx-1",
    entryType: "EARN",
    points: 420,
    occurredAt: "2026-08-02T18:04:00Z",
    reference: "Purchase #A-10231",
    createdAt: "2026-08-02T18:04:01Z",
  },
  {
    id: "le-2",
    tenantId: stubTenants[0].id,
    customerId: stubCustomer.id,
    transactionId: null,
    entryType: "CAMPAIGN_BONUS",
    points: 500,
    occurredAt: "2026-01-03T00:00:00Z",
    reference: "Welcome Bonus",
    createdAt: "2026-01-03T00:00:01Z",
  },
  {
    id: "le-3",
    tenantId: stubTenants[0].id,
    customerId: stubCustomer.id,
    transactionId: null,
    entryType: "REDEEM",
    points: -200,
    occurredAt: "2026-08-20T12:00:00Z",
    reference: "Redeemed: $5 off coupon",
    createdAt: "2026-08-20T12:00:01Z",
  },
];

/** Derived the same way core-api derives it: SUM(points) over the ledger. */
export function computeStubBalance(customerId: string): PointsBalance {
  const balance = stubLedgerEntries
    .filter((entry) => entry.customerId === customerId)
    .reduce((sum, entry) => sum + entry.points, 0);

  return {
    customerId,
    balance,
    asOf: new Date().toISOString(),
  };
}
