import uuid


async def test_next_best_offer_returns_tenant_scoped_ranked_offers(client):
    tenant_id = str(uuid.uuid4())
    customer_id = str(uuid.uuid4())

    response = await client.post(
        "/recommendations/next-best-offer",
        json={"tenant_id": tenant_id, "customer_id": customer_id, "max_results": 2},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["tenant_id"] == tenant_id
    assert body["customer_id"] == customer_id
    assert len(body["offers"]) == 2
    scores = [offer["score"] for offer in body["offers"]]
    assert scores == sorted(scores, reverse=True)
    for offer in body["offers"]:
        uuid.UUID(offer["offer_id"])
        assert 0.0 <= offer["score"] <= 1.0
        assert offer["reason"]


async def test_next_best_offer_defaults_max_results_to_three(client):
    response = await client.post(
        "/recommendations/next-best-offer",
        json={"tenant_id": str(uuid.uuid4()), "customer_id": str(uuid.uuid4())},
    )

    assert response.status_code == 200
    assert len(response.json()["offers"]) == 3


async def test_next_best_offer_requires_tenant_and_customer_id(client):
    response = await client.post("/recommendations/next-best-offer", json={})

    assert response.status_code == 422
    missing_fields = {error["loc"][-1] for error in response.json()["detail"]}
    assert {"tenant_id", "customer_id"} <= missing_fields


async def test_next_best_offer_is_deterministic_per_tenant_and_customer(client):
    tenant_id = str(uuid.uuid4())
    customer_id = str(uuid.uuid4())
    payload = {"tenant_id": tenant_id, "customer_id": customer_id, "max_results": 1}

    first = await client.post("/recommendations/next-best-offer", json=payload)
    second = await client.post("/recommendations/next-best-offer", json=payload)

    assert first.json()["offers"] == second.json()["offers"]
