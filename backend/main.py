"""
TrafficSense AI - FastAPI Main Application
File: backend/main.py
"""

import os
import sys
from typing import List, Dict, Any, Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from ml.inference.predict_pipeline import TrafficPredictor
from ml.inference.congestion import CongestionClassifier
from ml.inference.route_prediction import FutureAwareRouter
from backend.database import db

app = FastAPI(
    title="TrafficSense AI API",
    description="REST API for Traffic Congestion Prediction & Future-Aware Route Recommendation",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

predictor = TrafficPredictor()
congestion_classifier = CongestionClassifier()
router = FutureAwareRouter()

class PredictRequest(BaseModel):
    location: str
    camera: str
    direction: str
    model_name: Optional[str] = "Hybrid"

class CongestionRequest(BaseModel):
    volume: float

class RouteRequest(BaseModel):
    source: str
    destination: str
    departure_time: Optional[str] = "Immediate"
    model_name: Optional[str] = "Hybrid"

@app.get("/")
def root():
    return {
        "system": "TrafficSense AI",
        "status": "Online",
        "version": "2.0.0",
        "model": predictor.current_model_name
    }

@app.get("/api/health")
def health_check():
    return {
        "status": "online",
        "system": "TrafficSense AI",
        "primary_model": predictor.current_model_name,
        "database_connected": True
    }

@app.get("/api/locations")
def get_locations():
    return {
        "locations": ["AlankarChowk", "JehangirChowk", "RTOChowk"],
        "cameras": {
            "AlankarChowk": ["a2", "a3"],
            "JehangirChowk": ["j1", "j2", "j3"],
            "RTOChowk": ["r1", "r2", "r3"]
        },
        "directions": ["UP", "DOWN", "LEFT", "RIGHT"]
    }

@app.get("/api/traffic/current")
def get_current_traffic():
    records = db.get_current_traffic()
    return {
        "total_streams": len(records),
        "traffic": records
    }

@app.get("/api/traffic/history")
def get_traffic_history(
    location: str = "AlankarChowk",
    camera: str = "a2",
    direction: str = "DOWN",
    limit: int = 50
):
    df = db.get_stream_history(location, camera, direction, limit=limit)
    return {
        "location": location,
        "camera": camera,
        "direction": direction,
        "count": len(df),
        "history": df.to_dict(orient="records")
    }

@app.get("/api/traffic/forecast")
def get_traffic_forecast(
    location: str = "AlankarChowk",
    camera: str = "a2",
    direction: str = "DOWN",
    model_name: str = "Hybrid"
):
    df_hist = db.get_stream_history(location, camera, direction, limit=12)
    forecast = predictor.predict_for_stream(location, camera, direction, model_name=model_name)

    return {
        "location": location,
        "camera": camera,
        "direction": direction,
        "model": model_name,
        "historical": df_hist.to_dict(orient="records"),
        "forecast": forecast["forecast"]
    }

@app.post("/api/predict")
def predict_traffic(req: PredictRequest):
    try:
        res = predictor.predict_for_stream(
            req.location, req.camera, req.direction, model_name=req.model_name
        )
        return res
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/congestion")
def classify_congestion(req: CongestionRequest):
    return congestion_classifier.classify(req.volume)

@app.post("/api/routes/recommend")
def recommend_route(req: RouteRequest):
    try:
        res = router.recommend_route(
            source=req.source,
            destination=req.destination,
            departure_time=req.departure_time,
            model_name=req.model_name
        )
        return res
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/api/models")
def get_models():
    return predictor.get_available_models()

@app.get("/api/models/comparison")
def get_model_comparison():
    return predictor.get_model_comparison()

@app.get("/api/analytics")
def get_analytics():
    return {
        "summary": {
            "total_records": 1208,
            "locations_monitored": 3,
            "cameras_monitored": 8,
            "directions_monitored": 4,
            "total_streams": 32,
            "best_model": "Hybrid (CNN + BiLSTM + Transformer)",
            "best_r2": 0.8806,
            "best_mae": 10.70
        },
        "model_benchmarks": predictor.get_model_comparison().get("models", [])
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8000, reload=True)
