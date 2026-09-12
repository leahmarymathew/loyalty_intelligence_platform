from fastapi import FastAPI

from ai_service.routers import chat, health, recommendations

app = FastAPI(
    title="Loyalty Intelligence Platform - AI Service",
    description="Next-best-offer recommender and retrieval-augmented support chatbot.",
    version="0.1.0",
)

app.include_router(health.router)
app.include_router(recommendations.router)
app.include_router(chat.router)
