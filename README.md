# TrafficSense AI 🚦
### Intelligent Traffic Prediction & Management System
*B.Tech Final-Year Project*

---

## 📌 Project Overview
**TrafficSense AI** is a comprehensive intelligent traffic management system engineered for high-density urban intersections. It couples CCTV/sensor-level vehicle classification data across 32 independent junction streams in Pune (Alankar Chowk, Jehangir Chowk, and RTO Chowk) with a **Bidirectional Long Short-Term Memory (BiLSTM)** deep learning model for 5-minute traffic volume forecasting.

The repository includes:
1. **Machine Learning Pipeline (`ml/`)**: Preprocessing, dataset generation (1208 clean records, 1176 feature rows), BiLSTM training (`train_bilstm_v2.py`), and real-time inference.
2. **Datasets (`datasets/`)**: Raw sensor logs (`pune_traffic_5min.csv`), cleaned data (`pune_traffic_5min_clean.csv`), and engineered feature dataset (`traffic_features.csv`).
3. **Backend API (`backend/`)**: FastAPI REST service exposing live junction feeds, prediction endpoints, and benchmarking metrics.
4. **Android Native App (`app/`)**: High-fidelity Jetpack Compose application with Room local database, interactive junction/camera switcher, lookback-12 sequence visualization, live simulation player, configurable alert thresholds, and academic model comparison.
5. **Academic Documentation (`docs/`)**: Full B.Tech final-year technical report.

---

## 🚀 Quick Start

### 1. Run Machine Learning Training Pipeline
```bash
# Generate datasets & train BiLSTM model
python ml/training/train_bilstm_v2.py
```
This script evaluates the BiLSTM against Random Forest baselines and outputs:
- `ml/models/traffic_predictions_bilstm_v2.csv`
- `ml/models/model_metadata.json`

### 2. Launch FastAPI Backend
```bash
uvicorn backend.main:app --reload --port 8000
```

### 3. Android Mobile Application
Open the repository in Android Studio or preview via AI Studio streaming emulator. The app includes local Room persistence, offline simulation, live BiLSTM inference, and alert management.
