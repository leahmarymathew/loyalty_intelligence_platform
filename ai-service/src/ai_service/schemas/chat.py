from datetime import datetime, timezone
from uuid import UUID

from pydantic import BaseModel, Field

from ai_service.schemas.common import TenantScopedRequest


class SupportChatRequest(TenantScopedRequest):
    """A single-turn question to the retrieval-augmented support chatbot."""

    customer_id: UUID | None = Field(
        default=None,
        description="Customer asking the question, if authenticated. Anonymous support queries may omit this.",
    )
    message: str = Field(..., min_length=1, max_length=4000, description="The customer's support question.")


class RetrievedSource(BaseModel):
    document_id: str
    title: str
    excerpt: str


class SupportChatResponse(BaseModel):
    tenant_id: UUID
    answer: str
    sources: list[RetrievedSource]
    generated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
