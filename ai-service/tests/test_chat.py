import uuid


async def test_support_chat_returns_tenant_scoped_answer_with_sources(client):
    tenant_id = str(uuid.uuid4())

    response = await client.post(
        "/chat/support",
        json={"tenant_id": tenant_id, "message": "How do I redeem my points?"},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["tenant_id"] == tenant_id
    assert body["answer"]
    assert len(body["sources"]) >= 1
    for source in body["sources"]:
        assert source["document_id"]
        assert source["title"]
        assert source["excerpt"]


async def test_support_chat_accepts_optional_customer_id(client):
    response = await client.post(
        "/chat/support",
        json={
            "tenant_id": str(uuid.uuid4()),
            "customer_id": str(uuid.uuid4()),
            "message": "What tier am I?",
        },
    )

    assert response.status_code == 200


async def test_support_chat_requires_non_empty_message(client):
    response = await client.post(
        "/chat/support",
        json={"tenant_id": str(uuid.uuid4()), "message": ""},
    )

    assert response.status_code == 422


async def test_support_chat_requires_tenant_id(client):
    response = await client.post("/chat/support", json={"message": "hi"})

    assert response.status_code == 422
