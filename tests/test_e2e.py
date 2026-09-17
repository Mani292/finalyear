import pytest
from fastapi.testclient import TestClient
from backend.main import app

client = TestClient(app)

def test_health_endpoint():
    response = client.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "online"

def test_locations_endpoint():
    response = client.get("/api/locations")
    assert response.status_code == 200
    data = response.json()
    assert "AlankarChowk" in data["locations"]

def test_current_traffic():
    response = client.get("/api/traffic/current")
    assert response.status_code == 200
    data = response.json()
    assert data["total_streams"] == 32

def test_predict_endpoint():
    payload = {
        "location": "AlankarChowk",
        "camera": "a2",
        "direction": "DOWN",
        "model_name": "Hybrid"
    }
    response = client.post("/api/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "forecast" in data
    assert "15min" in data["forecast"]

def test_route_recommend_endpoint():
    payload = {
        "source": "AlankarChowk",
        "destination": "RTOChowk",
        "departure_time": "Immediate",
        "model_name": "Hybrid"
    }
    response = client.post("/api/routes/recommend", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "recommended_route" in data
    assert "explanation" in data
