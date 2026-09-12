"""Retrieval-augmented support chatbot.

Placeholder implementation: no vector store is wired up (see
``VECTOR_STORE_URL`` in config.py, currently unused) and no LLM call is
made regardless of ``LLM_PROVIDER``. ``retrieve_and_answer`` stands in for
the retrieve -> augment -> generate pipeline so the endpoint contract is
real; a genuine implementation would embed ``message``, query the tenant's
document/FAQ index (scoped by tenant_id, same isolation requirement as
everything else in this platform), and pass the retrieved chunks plus the
question to an LLM.
"""

from uuid import UUID

from ai_service.schemas.chat import RetrievedSource


def retrieve_and_answer(tenant_id: UUID, message: str) -> tuple[str, list[RetrievedSource]]:
    """Return a placeholder answer plus the (fake) sources it was grounded in."""
    sources = [
        RetrievedSource(
            document_id="faq-loyalty-points-101",
            title="How loyalty points work",
            excerpt="Points are earned on eligible purchases and never expire mid-tier.",
        )
    ]
    answer = (
        "This is a stub response from the support chatbot for tenant "
        f"{tenant_id}. No LLM or vector store is connected yet; your "
        f"question was: {message!r}"
    )
    return answer, sources
