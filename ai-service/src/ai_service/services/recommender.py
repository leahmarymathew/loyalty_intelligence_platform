"""Next-best-offer recommender.

Placeholder implementation: there is no feature store, no trained model, and
no connection to core-api's offers/campaigns tables yet. This returns a
small, deterministic set of synthetic offers so the endpoint contract is
real and testable end-to-end. Swapping in an actual scoring model later
should only require changing ``score_offers`` - the router and schemas
should not need to change.

Note on the "avoid leaking future information into training features"
design question flagged as TBD in docs/ARCHITECTURE.md: whatever replaces
this stub must only score against data available as of the request time
(e.g. ledger balance and transactions up to `now`), never against
outcomes that happened after the offer would have been shown.
"""

from uuid import UUID, uuid5, NAMESPACE_URL

from ai_service.schemas.recommendations import RecommendedOffer

# Fixed, deterministic namespace so re-running with the same inputs produces
# the same synthetic offer_ids - useful for tests, meaningless once this is
# backed by real offer data.
_OFFER_NAMESPACE = uuid5(NAMESPACE_URL, "loyalty-intelligence-platform/ai-service/offers")

_CANDIDATE_REASONS = (
    "Frequently purchased category discount",
    "Points balance nearing next reward tier",
    "Re-engagement offer for lapsed activity",
)


def score_offers(tenant_id: UUID, customer_id: UUID, max_results: int) -> list[RecommendedOffer]:
    """Return a ranked, placeholder list of offers for one tenant's customer."""
    count = min(max_results, len(_CANDIDATE_REASONS))
    offers = []
    for rank, reason in enumerate(_CANDIDATE_REASONS[:count]):
        offer_id = uuid5(_OFFER_NAMESPACE, f"{tenant_id}:{customer_id}:{rank}")
        score = round(1.0 - (rank * 0.2), 2)
        offers.append(RecommendedOffer(offer_id=offer_id, score=score, reason=reason))
    return offers
