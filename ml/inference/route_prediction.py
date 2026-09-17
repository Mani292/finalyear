"""
Future-Aware Route Recommendation Engine
Location: ml/inference/route_prediction.py
"""
from typing import Dict, Any, List
import pandas as pd
from ml.inference.predict_pipeline import TrafficPredictor
from ml.inference.congestion import CongestionClassifier

class FutureAwareRouter:
    def __init__(self):
        self.predictor = TrafficPredictor()
        self.classifier = CongestionClassifier()

        # Graph definition connecting Pune junctions
        self.graph = {
            ("AlankarChowk", "JehangirChowk"): [
                {"id": "route_A", "name": "Via Station Arterial Road", "distance_km": 1.8, "base_time_min": 4.0, "camera": "a2", "direction": "DOWN"},
                {"id": "route_B", "name": "Via Sangam Bridge Bypass", "distance_km": 2.4, "base_time_min": 5.0, "camera": "a3", "direction": "RIGHT"}
            ],
            ("AlankarChowk", "RTOChowk"): [
                {"id": "route_A", "name": "Via Collectorate Road", "distance_km": 2.2, "base_time_min": 5.0, "camera": "a2", "direction": "UP"},
                {"id": "route_B", "name": "Via Wellesley Road Expressway", "distance_km": 2.8, "base_time_min": 6.0, "camera": "a3", "direction": "LEFT"}
            ],
            ("JehangirChowk", "RTOChowk"): [
                {"id": "route_A", "name": "Via Sassoon Flyover", "distance_km": 1.5, "base_time_min": 3.5, "camera": "j1", "direction": "DOWN"},
                {"id": "route_B", "name": "Via Ambedkar Road Loop", "distance_km": 2.1, "base_time_min": 4.5, "camera": "j2", "direction": "LEFT"}
            ],
            ("JehangirChowk", "AlankarChowk"): [
                {"id": "route_A", "name": "Via Station Arterial Road", "distance_km": 1.8, "base_time_min": 4.0, "camera": "j1", "direction": "UP"},
                {"id": "route_B", "name": "Via Sangam Bridge Bypass", "distance_km": 2.4, "base_time_min": 5.0, "camera": "j3", "direction": "LEFT"}
            ],
            ("RTOChowk", "AlankarChowk"): [
                {"id": "route_A", "name": "Via Collectorate Road", "distance_km": 2.2, "base_time_min": 5.0, "camera": "r1", "direction": "DOWN"},
                {"id": "route_B", "name": "Via Wellesley Road Expressway", "distance_km": 2.8, "base_time_min": 6.0, "camera": "r2", "direction": "RIGHT"}
            ],
            ("RTOChowk", "JehangirChowk"): [
                {"id": "route_A", "name": "Via Sassoon Flyover", "distance_km": 1.5, "base_time_min": 3.5, "camera": "r1", "direction": "UP"},
                {"id": "route_B", "name": "Via Ambedkar Road Loop", "distance_km": 2.1, "base_time_min": 4.5, "camera": "r3", "direction": "LEFT"}
            ]
        }

    def recommend_route(self, source: str, destination: str, departure_time: str = "Immediate", model_name: str = "Hybrid") -> Dict[str, Any]:
        key = (source, destination)
        if key not in self.graph:
            return {
                "source": source,
                "destination": destination,
                "error": f"No direct candidate route found between {source} and {destination}"
            }

        candidate_routes = self.graph[key]
        evaluated_routes = []

        # W1: predicted_congestion, W2: predicted_travel_time, W3: predicted_delay, W4: distance
        w1, w2, w3, w4 = 0.35, 0.30, 0.20, 0.15

        for rt in candidate_routes:
            # Fetch multi-horizon forecasts
            pred_res = self.predictor.predict_for_stream(
                location=source,
                camera=rt["camera"],
                direction=rt["direction"],
                model_name=model_name
            )

            f15 = pred_res["forecast"]["15min"]["volume"]
            f30 = pred_res["forecast"]["30min"]["volume"]
            f60 = pred_res["forecast"]["60min"]["volume"]

            # FUTURE-AWARE TIME COST CALCULATION
            # Segment 1 is reached at T+5m (15min horizon proxy)
            # Segment 2 at T+15m, Segment 3 at T+30m
            avg_future_volume = (f15 * 0.5) + (f30 * 0.35) + (f60 * 0.15)

            # Estimate delay factor from projected future congestion
            delay_factor = max(0.0, (avg_future_volume - 70.0) / 40.0)
            expected_delay = round(delay_factor * 2.5, 1)
            expected_travel_time = round(rt["base_time_min"] + expected_delay, 1)

            cong_res = self.classifier.classify(avg_future_volume)

            # Compute future route cost score
            route_score = round(
                w1 * (avg_future_volume / 2.0) +
                w2 * expected_travel_time +
                w3 * expected_delay * 2 +
                w4 * rt["distance_km"] * 5,
                2
            )

            evaluated_routes.append({
                "id": rt["id"],
                "name": rt["name"],
                "distance_km": rt["distance_km"],
                "expected_travel_time_min": expected_travel_time,
                "expected_delay_min": expected_delay,
                "predicted_congestion": cong_res["congestion_level"],
                "predicted_future_volume": round(avg_future_volume, 1),
                "route_score": route_score,
                "forecast_breakdown": pred_res["forecast"],
                "camera_used": rt["camera"],
                "direction_used": rt["direction"]
            })

        # Sort by route score (lower is better)
        evaluated_routes.sort(key=lambda x: x["route_score"])

        best_route = evaluated_routes[0]
        alt_routes = evaluated_routes[1:]

        # Build explanation
        if len(alt_routes) > 0:
            alt = alt_routes[0]
            if best_route["predicted_future_volume"] < alt["predicted_future_volume"]:
                explanation = f"{best_route['name']} is recommended because although current traffic may seem comparable, the model predicts significantly lower future congestion ({best_route['predicted_future_volume']} vehicles vs {alt['predicted_future_volume']} vehicles) over the next 30-60 minutes."
            else:
                explanation = f"{best_route['name']} is recommended as it achieves the optimal balance of distance ({best_route['distance_km']} km) and overall travel time ({best_route['expected_travel_time_min']} mins) under anticipated future traffic conditions."
        else:
            explanation = f"{best_route['name']} is the optimal route under future traffic forecasts."

        return {
            "source": source,
            "destination": destination,
            "departure_time": departure_time,
            "model_used": model_name,
            "recommended_route": best_route,
            "alternative_routes": alt_routes,
            "explanation": explanation
        }
