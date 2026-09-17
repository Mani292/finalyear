# TrafficSense AI: Intelligent Traffic Congestion Prediction & Future-Route Recommendation System

TrafficSense AI is a production-quality Intelligent Transportation System (ITS) platform designed to forecast future traffic conditions and recommend routes based on **FUTURE congestion** rather than relying solely on current traffic snapshots.

Built as a final-year engineering project, TrafficSense AI implements a state-of-the-art **Hybrid Neural Network Architecture (CNN + BiLSTM + Transformer)** to extract spatial/local patterns, temporal sequences, and long-range dependencies from multi-stream traffic data.

---

## Key Features

1. **Multi-Horizon Multi-Stream Forecasting:** Predicts traffic volume & congestion level for **15 mins**, **30 mins**, and **60 mins** ahead across independent location-camera-direction traffic streams.
2. **Hybrid Deep Learning Model:** Combines:
   - **CNN Branch:** Local temporal pattern and short-term spike extraction.
   - **BiLSTM Branch:** Bidirectional temporal sequence learning.
   - **Transformer Branch:** Multi-head self-attention for long-range temporal dependencies.
3. **Future-Aware Route Engine:** Evaluates candidate routes between Pune intersections using forecasted congestion at estimated arrival times ($T+5\text{m}, T+15\text{m}, T+30\text{m}$), avoiding route selection traps caused by temporary transient conditions.
4. **Interactive ITS Engineering Dashboard:** Built with React, TypeScript, Vite, Tailwind CSS, Recharts, and Leaflet maps featuring:
   - Live Traffic Overview
   - Stream Multi-Horizon Forecast Explorer
   - Interactive Schematic Congestion Map
   - Future-Aware Route Planning & "Why this route?" Explanation Panel
   - ML Model Benchmark & Comparison Visualizer
   - Traffic Analytics Page
   - DEMO Mode Toggle for offline historical dataset demonstration

---

## Dataset & Preprocessing

- **Dataset Source:** Pune Heterogeneous Traffic Dataset (5-minute resolution).
- **Monitored Locations:** `AlankarChowk`, `JehangirChowk`, `RTOChowk`.
- **Cameras:** `a2, a3, j1, j2, j3, r1, r2, r3`.
- **Directions:** `UP`, `DOWN`, `LEFT`, `RIGHT`.
- **Data Quality Report:** 1,208 cleaned, unique stream observations aggregated across 32 independent streams (`location + camera + direction + time`).

---

## Model Benchmark Results

Models evaluated on unseen chronological split (80/20 train/test):

| Model | MAE | RMSE | R² Score | Classification Accuracy | Macro F1 Score |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Hybrid (CNN + BiLSTM + Transformer)** | **12.12** | **15.48** | **0.8005** | **96.14%** | **0.6457** |
| **Transformer** | 12.41 | 15.63 | 0.7971 | 96.14% | 0.6457 |
| **CNN** | 12.33 | 16.15 | 0.7834 | 96.14% | 0.6457 |
| **BiLSTM** | 13.23 | 16.34 | 0.7779 | 96.14% | 0.6457 |
| **RandomForest Baseline** | 12.41 | 16.17 | 0.7831 | 94.39% | 0.6336 |

---

## Quickstart & Installation

### Prerequisites
- Python 3.10+
- Node.js 18+ & npm

### Setup Instructions

1. **Clone & Install Python Dependencies:**
   ```bash
   python -m pip install -r requirements.txt
   ```

2. **Run ML Preprocessing & Training Pipeline:**
   ```bash
   python ml/preprocessing/preprocess_dataset.py
   python ml/preprocessing/create_traffic_features.py
   python ml/training/train_models.py
   ```

3. **Start FastAPI Backend Server:**
   ```bash
   python -m uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
   ```

4. **Start React Frontend Dashboard:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Run Automated End-to-End Tests:**
   ```bash
   PYTHONPATH=. python -m pytest tests/
   ```

---

## API Documentation

- `GET /api/health` - API and database status.
- `GET /api/locations` - Available Pune locations, cameras, directions.
- `GET /api/traffic/current` - Live/latest stream traffic volume & counts.
- `GET /api/traffic/forecast?location=AlankarChowk&camera=a2&direction=DOWN` - Multi-horizon 15/30/60m forecast.
- `POST /api/predict` - Stream multi-horizon inference payload.
- `POST /api/routes/recommend` - Future-aware route scoring and candidate recommendation.
- `GET /api/models/comparison` - Model benchmarks (MAE, RMSE, R², Accuracy, F1).
