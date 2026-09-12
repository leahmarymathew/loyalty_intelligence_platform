from datetime import datetime, timezone
from uuid import UUID

from pydantic import BaseModel, Field

from ai_service.schemas.common import TenantScopedRequest


class OfferRecommendationRequest(TenantScopedRequest):
    """Ask for the next-best-offer(s) for one customer of a tenant."""

    customer_id: UUID = Field(..., description="Customer to recommend offers to, scoped to tenant_id.")
    max_results: int = Field(default=3, ge=1, le=20, description="Maximum number of offers to return.")


class RecommendedOffer(BaseModel):
    offer_id: UUID
    score: float = Field(..., ge=0.0, le=1.0, description="Relative confidence/priority score, higher is better.")
    reason: str = Field(..., description="Human-readable explanation for why this offer was surfaced.")


class OfferRecommendationResponse(BaseModel):
    tenant_id: UUID
    customer_id: UUID
    offers: list[RecommendedOffer]
    generated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
