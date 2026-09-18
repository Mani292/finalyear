# TrafficSense AI: Intelligent Traffic Congestion Prediction & Future-Route Recommendation System

TrafficSense AI is a production-quality Intelligent Transportation System (ITS) platform designed to forecast future traffic conditions and recommend routes based on **FUTURE congestion** rather than relying solely on current traffic snapshots.

Built as a final-year engineering project, TrafficSense AI implements a state-of-the-art **Hybrid Neural Network Architecture (CNN + BiLSTM + Transformer)** to extract spatial/local patterns, temporal sequences, and long-range dependencies from multi-stream traffic data. It features a nationwide routing provider abstraction integrating Google Routes API and OpenStreetMap to scale future-aware congestion routing across India.

---

## Key System Features

1. **Multi-Horizon Multi-Stream Forecasting:** Predicts traffic volume & congestion level for **15 mins**, **30 mins**, and **60 mins** ahead across independent location-camera-direction traffic streams.
2. **Hybrid Deep Learning Model:** Combines:
   - **CNN Branch:** Local temporal pattern and short-term spike feature extraction.
   - **BiLSTM Branch:** Bidirectional temporal sequence learning.
   - **Transformer Branch:** Multi-head self-attention for long-range temporal dependencies.
3. **Future-Aware Route Engine & Nationwide Provider Abstraction:** Evaluates candidate routes across Pune local intersections and nationwide Indian cities using forecasted congestion at estimated arrival times ($T+5\text{m}, T+15\text{m}, T+30\text{m}$), avoiding route selection traps caused by temporary transient conditions. Integrated with Google Routes API, HERE API, and OpenStreetMap fallback.
4. **Interactive ITS Engineering Dashboard:** Built with React, TypeScript, Vite, Tailwind CSS, Recharts, and Leaflet maps featuring:
   - Live Traffic Overview
   - Stream Multi-Horizon Forecast Explorer
   - Interactive Schematic Congestion Map
   - Future-Aware Route Planning (Pune Nodes + Nationwide India Search) & "Why this route?" Explanation Panel
   - ML Model Benchmark & Comparison Visualizer
   - Historical Traffic Analytics Page
   - DEMO Mode Toggle for offline historical dataset demonstration

---

## System Architecture

```text
               Pune & India Traffic Datasets / Live APIs
                                  |
                                  v
                       Data Preprocessing Pipeline
                     (Deduplication & Stream Grouping)
                                  |
                                  v
                         Feature Engineering
                (Lags, Rolling Stats, Cyclic Time Encodings)
                                  |
                                  v
                        Chronological Sequences
                                  |
          +-----------------------+-----------------------+
          |                       |                       |
          v                       v                       v
      1D-CNN                   BiLSTM                Transformer
  (Local Patterns)       (Temporal Sequences)     (Long-Range Attention)
          |                       |                       |
          +-----------------------+-----------------------+
                                  |
                                  v
                            Feature Fusion
                                  |
                                  v
                      Hybrid Forecasting Model
                                  |
          +-----------------------+-----------------------+
          |                       |                       |
          v                       v                       v
      15 min                   30 min                  60 min
          |                       |                       |
          +-----------------------+-----------------------+
                                  |
                                  v
                        Congestion Classification
                    (LOW, MODERATE, HIGH, SEVERE)
                                  |
                                  v
               Future-Aware Routing Engine & Provider Abstraction
                     (Google Routes API / HERE / OSM)
                                  |
                                  v
                         FastAPI REST Backend
                                  |
                                  v
                   React + TypeScript Web Dashboard
```

---

## Dataset & Preprocessing

- **Dataset Source:** Pune Heterogeneous Traffic Dataset (5-minute resolution).
- **Monitored Locations:** `AlankarChowk`, `JehangirChowk`, `RTOChowk`.
- **Cameras:** `a2, a3, j1, j2, j3, r1, r2, r3`.
- **Directions:** `UP`, `DOWN`, `LEFT`, `RIGHT`.
- **Data Quality Report:** 1,208 cleaned, unique stream observations aggregated across 32 independent streams (`location + camera + direction + time`).

---

## Model Benchmark Results

Models evaluated on an unseen chronological 80/20 train/test split:

| Model Architecture | MAE (veh) | RMSE (veh) | R² Score | sMAPE (%) | Classification Accuracy | Macro F1 Score |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Transformer** | **11.52** | **14.71** | **0.8202** | **6.01%** | **96.14%** | **0.6457** |
| **CNN 1D** | 11.62 | 15.48 | 0.8014 | 6.08% | 95.79% | 0.6433 |
| **BiLSTM** | 12.13 | 15.34 | 0.8045 | 6.31% | 96.14% | 0.6457 |
| **Hybrid (CNN + BiLSTM + Transformer)** | 12.23 | 15.64 | 0.7964 | 6.27% | 96.14% | 0.6457 |
| **RandomForest Baseline** | 12.41 | 16.17 | 0.7831 | 6.52% | 94.39% | 0.6336 |

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

## Environment Variables (.env)

Create a `.env` file in the root directory:
```env
SUPABASE_URL=https://your-supabase-project.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key

GOOGLE_MAPS_API_KEY=your-google-routes-api-key
HERE_API_KEY=your-here-api-key
```

If external credentials are not set, TrafficSense AI automatically falls back to local SQLite persistence and OpenStreetMap nationwide routing abstractions.

---

## API Documentation

- `GET /api/health` - API and database connectivity status.
- `GET /api/locations` - Available Pune locations, cameras, directions.
- `GET /api/traffic/current` - Live/latest stream traffic volume & vehicle counts.
- `GET /api/traffic/forecast?location=AlankarChowk&camera=a2&direction=DOWN` - Multi-horizon 15/30/60m forecast.
- `POST /api/predict` - Multi-horizon inference request payload.
- `POST /api/routes/recommend` - Future-aware route scoring and candidate recommendation (Pune local or Nationwide India search).
- `GET /api/models/comparison` - Model benchmarks (MAE, RMSE, R², sMAPE, Accuracy, Precision, Recall, F1).
- `GET /api/analytics` - System metrics and dataset statistics.
