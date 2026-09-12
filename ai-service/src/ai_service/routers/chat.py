from fastapi import APIRouter

from ai_service.schemas.chat import SupportChatRequest, SupportChatResponse
from ai_service.services.rag import retrieve_and_answer

router = APIRouter(prefix="/chat", tags=["chat"])


@router.post("/support", response_model=SupportChatResponse)
async def support_chat(request: SupportChatRequest) -> SupportChatResponse:
    """Answer a tenant customer's support question.

    Stubbed: retrieval and generation are both placeholders (see
    ai_service.services.rag) until a vector store and LLM are wired up.
    The contract - tenant-scoped question in, grounded answer with
    sources out - is real.
    """
    answer, sources = retrieve_and_answer(request.tenant_id, request.message)
    return SupportChatResponse(tenant_id=request.tenant_id, answer=answer, sources=sources)
