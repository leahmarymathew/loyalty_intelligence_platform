/**
 * Domain types shared by both the brand-side dashboard and the customer PWA.
 *
 * These mirror the JPA entities in `core-api/src/main/java/com/loyaltyplatform/coreapi/domain/`
 * as of this scaffold (Tenant, Customer, PointsLedgerEntry, LedgerEntryType).
 * core-api has no REST layer yet (see repo root README "Status" section), so
 * there is nothing to fetch from — these types exist so UI code has something
 * real to compile against once the REST layer lands, and the stub data below
 * matches these shapes.
 *
 * `Tier`, `Campaign`, and `Offer` are NOT yet backed by core-api entities
 * (the tier engine and campaign worker are listed as "not yet built" in the
 * root README). They're included here, clearly marked, because the
 * dashboard's placeholder screens need *some* shape for tier/campaign data
 * and the README explicitly calls out tiers/campaigns/offers as concepts the
 * platform manages. Treat their field names as provisional until core-api
 * defines the real entities.
 */

/** Mirrors `Tenant` (core-api domain). */
export interface Tenant {
  id: string;
  slug: string;
  name: string;
  createdAt: string; // ISO 8601 instant
}

/** Mirrors `Customer` (core-api domain). */
export interface Customer {
  id: string;
  tenantId: string;
  externalRef: string | null;
  email: string;
  fullName: string;
  birthDate: string | null; // ISO 8601 date
  currentTierId: string | null;
  enrolledAt: string; // ISO 8601 instant
  createdAt: string; // ISO 8601 instant
}

/** Mirrors `LedgerEntryType` (core-api domain enum). */
export type LedgerEntryType =
  | "EARN"
  | "REDEEM"
  | "ADJUSTMENT"
  | "EXPIRY"
  | "CAMPAIGN_BONUS";

/**
 * Mirrors `PointsLedgerEntry` (core-api domain). A customer's points balance
 * is never stored directly — it's derived as `SUM(points)` over these rows
 * (see docs/ARCHITECTURE.md, "Why an append-only points ledger").
 */
export interface PointsLedgerEntry {
  id: string;
  tenantId: string;
  customerId: string;
  transactionId: string | null;
  entryType: LedgerEntryType;
  points: number;
  occurredAt: string; // ISO 8601 instant
  reference: string | null;
  createdAt: string; // ISO 8601 instant
}

/**
 * PROVISIONAL — no core-api entity yet. Shape inferred from
 * `Customer.currentTierId` and the "tier evaluation engine" / "tier_rules"
 * references in docs/ARCHITECTURE.md.
 */
export interface Tier {
  id: string;
  tenantId: string;
  name: string;
  minPoints: number;
  sortOrder: number;
}

/**
 * PROVISIONAL — no core-api entity yet ("campaign worker not yet built" per
 * root README). Shape is a reasonable placeholder for a brand-run
 * points/bonus campaign.
 */
export interface Campaign {
  id: string;
  tenantId: string;
  name: string;
  description: string;
  startsAt: string; // ISO 8601 instant
  endsAt: string; // ISO 8601 instant
  status: "DRAFT" | "ACTIVE" | "ENDED";
}

/**
 * PROVISIONAL — no core-api entity yet. Placeholder for a redeemable reward
 * tied to a campaign or standing catalog.
 */
export interface Offer {
  id: string;
  tenantId: string;
  title: string;
  pointsCost: number;
}

/** Convenience shape for "how many points does this customer have right now". */
export interface PointsBalance {
  customerId: string;
  balance: number;
  asOf: string; // ISO 8601 instant
}
