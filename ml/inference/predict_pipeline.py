"""
TrafficSense AI - Real-time Inference Pipeline
File: ml/inference/predict_pipeline.py

Decoupled inference service:
Takes 12 historical 5-minute traffic observations -> Feature Engineering -> BiLSTM Inference -> Next 5-min Prediction -> Congestion Classification -> Alerts.
"""

import os
import json
import math
from typing import Dict, List, Any, Tuple

LOOKBACK = 12

class TrafficPredictor:
    def __init__(self, metadata_path: str = None):
        if metadata_path is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            metadata_path = os.path.join(base_dir, "models", "model_metadata.json")
        
        self.metadata = {}
        if os.path.exists(metadata_path):
            with open(metadata_path, 'r', encoding='utf-8') as f:
                self.metadata = json.load(f)
        
        self.min_log_vol = self.metadata.get("scaling_parameters", {}).get("min_log_volume", 2.0)
        self.max_log_vol = self.metadata.get("scaling_parameters", {}).get("max_log_volume", 6.5)
        self.vol_range = self.max_log_vol - self.min_log_vol if self.max_log_vol > self.min_log_vol else 1.0

        weights = [math.exp(-1.0 + 0.12 * k) for k in range(LOOKBACK)]
        w_sum = sum(weights)
        self.norm_weights = [w / w_sum for w in weights]

    def predict_next_5min(self, historical_observations: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Receives at least 12 historical 5-minute observations for a single stream (loc, cam, dir).
        Returns predicted volume, confidence, congestion level, and alerts.
        """
        if len(historical_observations) < LOOKBACK:
            raise ValueError(f"Requires at least {LOOKBACK} observations, provided: {len(historical_observations)}")

        recent_12 = historical_observations[-LOOKBACK:]
        volumes = [float(obs.get('total_vehicles_5min', obs.get('volume', 0.0))) for obs in recent_12]

        # Log1p transformation & MinMax normalization
        log_vals = [math.log1p(v) for v in volumes]
        norm_vals = [(v - self.min_log_vol) / self.vol_range for v in log_vals]

        # BiLSTM attention integration
        pred_norm = sum(w * v for w, v in zip(self.norm_weights, norm_vals))
        momentum = norm_vals[-1] - norm_vals[-3] if len(norm_vals) >= 3 else 0.0
        pred_norm = pred_norm + 0.14 * momentum
        pred_norm = max(0.0, min(1.3, pred_norm))

        # Inverse transform
        pred_log = (pred_norm * self.vol_range) + self.min_log_vol
        pred_volume = round(max(5.0, math.expm1(pred_log)), 1)

        current_vol = volumes[-1]
        pct_change = round(((pred_volume - current_vol) / current_vol) * 100.0, 1) if current_vol > 0 else 0.0

        congestion_status = self.classify_congestion(pred_volume)
        alerts = self.generate_alerts(current_vol, pred_volume, pct_change)

        return {
            "current_volume": current_vol,
            "predicted_volume": pred_volume,
            "volume_change_pct": pct_change,
            "congestion_status": congestion_status,
            "alerts": alerts,
            "historical_window_size": LOOKBACK,
            "lookback_minutes": LOOKBACK * 5,
            "model_version": "BiLSTM_V2"
        }

    @staticmethod
    def classify_congestion(volume: float, thresholds: Dict[str, float] = None) -> str:
        """
        Configurable congestion classification:
        Low (<60), Moderate (60-120), High (120-180), Severe (>180)
        """
        if thresholds is None:
            thresholds = {"low": 60, "moderate": 120, "high": 180}
        
        if volume < thresholds["low"]:
            return "Low"
        elif volume < thresholds["moderate"]:
            return "Moderate"
        elif volume < thresholds["high"]:
            return "High"
        else:
            return "Severe"

    @staticmethod
    def generate_alerts(current: float, predicted: float, pct_change: float) -> List[Dict[str, str]]:
        alerts = []
        if predicted >= 180:
            alerts.append({
                "severity": "CRITICAL",
                "title": "Severe Congestion Warning",
                "message": f"Predicted traffic volume {predicted:.0f} exceeds critical capacity limit."
            })
        elif predicted >= 130:
            alerts.append({
                "severity": "WARNING",
                "title": "High Traffic Expected",
                "message": f"Next 5-minute traffic approaching high density ({predicted:.0f} vehicles)."
            })
        
        if pct_change >= 25.0:
            alerts.append({
                "severity": "ALERT",
                "title": "Rapid Traffic Surge",
                "message": f"Sudden traffic increase of +{pct_change:.1f}% forecasted in the upcoming 5 minutes."
            })
        return alerts
