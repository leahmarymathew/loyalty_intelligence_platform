from fastapi import APIRouter

from ai_service.schemas.recommendations import OfferRecommendationRequest, OfferRecommendationResponse
from ai_service.services.recommender import score_offers

router = APIRouter(prefix="/recommendations", tags=["recommendations"])


@router.post("/next-best-offer", response_model=OfferRecommendationResponse)
async def next_best_offer(request: OfferRecommendationRequest) -> OfferRecommendationResponse:
    """Return the next-best-offer(s) for a tenant's customer.

    Stubbed: scoring has no model or feature store behind it yet (see
    ai_service.services.recommender). The contract - tenant-scoped
    request in, ranked offers out - is real.
    """
    offers = score_offers(request.tenant_id, request.customer_id, request.max_results)
    return OfferRecommendationResponse(
        tenant_id=request.tenant_id,
        customer_id=request.customer_id,
        offers=offers,
    )
