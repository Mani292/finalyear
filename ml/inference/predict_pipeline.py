"""
Inference Pipeline for TrafficSense AI
Location: ml/inference/predict_pipeline.py
"""
import os
import json
import torch
import joblib
import pandas as pd
import numpy as np
from ml.training.train_models import CNNModel, BiLSTMModel, TransformerModel, HybridModel, FEATURE_COLS, LOOKBACK
from ml.inference.congestion import CongestionClassifier

class TrafficPredictor:
    def __init__(self, models_dir: str = "ml/models"):
        self.models_dir = models_dir
        self.current_model_name = "Hybrid"
        self.classifier = CongestionClassifier()
        self.feature_cols = FEATURE_COLS
        self.lookback = LOOKBACK
        
        # Load Scalers
        scaler_X_path = os.path.join(models_dir, "scaler_X.pkl")
        scaler_y_path = os.path.join(models_dir, "scaler_y.pkl")
        
        self.scaler_X = joblib.load(scaler_X_path) if os.path.exists(scaler_X_path) else None
        self.scaler_y = joblib.load(scaler_y_path) if os.path.exists(scaler_y_path) else None

        # Load PyTorch Models
        F = len(self.feature_cols)
        self.models = {}

        cnn = CNNModel(F)
        if os.path.exists(os.path.join(models_dir, "cnn_model.pt")):
            cnn.load_state_dict(torch.load(os.path.join(models_dir, "cnn_model.pt")))
            cnn.eval()
            self.models["CNN"] = cnn

        bilstm = BiLSTMModel(F)
        if os.path.exists(os.path.join(models_dir, "bilstm_model.pt")):
            bilstm.load_state_dict(torch.load(os.path.join(models_dir, "bilstm_model.pt")))
            bilstm.eval()
            self.models["BiLSTM"] = bilstm

        trans = TransformerModel(F)
        if os.path.exists(os.path.join(models_dir, "transformer_model.pt")):
            trans.load_state_dict(torch.load(os.path.join(models_dir, "transformer_model.pt")))
            trans.eval()
            self.models["Transformer"] = trans

        hybrid = HybridModel(F)
        if os.path.exists(os.path.join(models_dir, "hybrid_model.pt")):
            hybrid.load_state_dict(torch.load(os.path.join(models_dir, "hybrid_model.pt")))
            hybrid.eval()
            self.models["Hybrid"] = hybrid

        # Load Random Forest
        rf_path = os.path.join(models_dir, "randomforest_model.pkl")
        if os.path.exists(rf_path):
            self.models["RandomForest"] = joblib.load(rf_path)

        # Load Comparison Metadata
        meta_path = os.path.join(models_dir, "model_comparison.json")
        if os.path.exists(meta_path):
            with open(meta_path, "r") as f:
                self.metadata = json.load(f)
        else:
            self.metadata = {"models": []}

    def predict_for_stream(self, location: str, camera: str, direction: str, model_name: str = "Hybrid") -> dict:
        feat_path = "datasets/processed/traffic_features.csv"
        if not os.path.exists(feat_path):
            from ml.preprocessing.create_traffic_features import create_features
            create_features()

        df = pd.read_csv(feat_path)
        sub = df[(df['location'] == location) & (df['camera'] == camera) & (df['direction'] == direction)]

        if len(sub) < self.lookback:
            raise ValueError(f"Insufficient historical data for stream {location}/{camera}/{direction}")

        sub = sub.sort_values('time').tail(self.lookback)
        raw_feat = sub[self.feature_cols].values

        # Scale
        flat_feat = raw_feat.reshape(-1, len(self.feature_cols))
        scaled_feat = self.scaler_X.transform(flat_feat).reshape(1, self.lookback, len(self.feature_cols))

        model_key = model_name if model_name in self.models else "Hybrid"
        model = self.models.get(model_key, self.models.get("Hybrid"))

        if model_key == "RandomForest":
            rf_in = scaled_feat[:, -1, :]
            preds_raw = model.predict(rf_in)[0] # shape (3,)
        else:
            with torch.no_grad():
                tensor_in = torch.tensor(scaled_feat, dtype=torch.float32)
                preds_scaled = model(tensor_in).numpy()
            preds_raw = self.scaler_y.inverse_transform(preds_scaled)[0]

        vol_15 = max(0, float(preds_raw[0]))
        vol_30 = max(0, float(preds_raw[1]))
        vol_60 = max(0, float(preds_raw[2]))

        c15 = self.classifier.classify(vol_15)
        c30 = self.classifier.classify(vol_30)
        c60 = self.classifier.classify(vol_60)

        return {
            "location": location,
            "camera": camera,
            "direction": direction,
            "model_used": model_key,
            "forecast": {
                "15min": {
                    "volume": round(vol_15, 1),
                    "congestion": c15["congestion_level"],
                    "confidence": c15["confidence"],
                    "description": c15["description"]
                },
                "30min": {
                    "volume": round(vol_30, 1),
                    "congestion": c30["congestion_level"],
                    "confidence": c30["confidence"],
                    "description": c30["description"]
                },
                "60min": {
                    "volume": round(vol_60, 1),
                    "congestion": c60["congestion_level"],
                    "confidence": c60["confidence"],
                    "description": c60["description"]
                }
            }
        }

    def get_available_models(self) -> dict:
        return {
            "available_models": list(self.models.keys()),
            "primary_model": "Hybrid",
            "feature_count": len(self.feature_cols),
            "lookback_steps": self.lookback
        }

    def get_model_comparison(self) -> dict:
        return self.metadata
