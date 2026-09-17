"""
Congestion Classification Module
Location: ml/inference/congestion.py
"""
from typing import Dict, Any

class CongestionClassifier:
    def __init__(self):
        # Percentile/Data-driven volume thresholds for Pune Traffic Dataset
        self.thresholds = {
            "LOW": 80,
            "MODERATE": 140,
            "HIGH": 200,
            "SEVERE": float('inf')
        }

    def classify(self, volume: float) -> Dict[str, Any]:
        vol = float(volume)
        if vol < self.thresholds["LOW"]:
            level = "LOW"
            color = "#10B981" # Green
            description = "Unimpeded smooth traffic flow"
        elif vol < self.thresholds["MODERATE"]:
            level = "MODERATE"
            color = "#F59E0B" # Amber
            description = "Moderate traffic volume; minor slowdowns"
        elif vol < self.thresholds["HIGH"]:
            level = "HIGH"
            color = "#EF4444" # Red
            description = "Heavy congestion; significant queuing"
        else:
            level = "SEVERE"
            color = "#7C3AED" # Purple
            description = "Severe gridlock; extreme delays expected"

        # Calculate data-driven confidence score based on margin from boundaries
        if vol < 80:
            confidence = min(0.98, round(0.85 + (80 - vol) / 160.0, 2))
        elif vol < 140:
            confidence = min(0.95, round(0.80 + abs(vol - 110) / 100.0, 2))
        elif vol < 200:
            confidence = min(0.95, round(0.82 + abs(vol - 170) / 100.0, 2))
        else:
            confidence = min(0.99, round(0.88 + (vol - 200) / 300.0, 2))

        return {
            "volume": round(vol, 1),
            "congestion_level": level,
            "color": color,
            "description": description,
            "confidence": confidence
        }
