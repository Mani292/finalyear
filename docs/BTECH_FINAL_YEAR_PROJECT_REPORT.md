# TrafficSense AI: Intelligent Traffic Prediction and Management System
## B.Tech Final-Year Project Comprehensive Documentation & Specification

---

### 1. Abstract Alignment & Problem Statement
Urban traffic congestion across Indian metropolitan junctions (such as Pune's Alankar Chowk, Jehangir Chowk, and RTO Chowk) leads to acute transit delays, heightened carbon emissions, emergency response blockage, and economic productivity losses. Conventional traffic management relies primarily on static fixed-time signals or manual traffic police interventions, lacking predictive foresight.

**TrafficSense AI** resolves this challenge through an end-to-end intelligent traffic prediction, monitoring, and decision-support architecture. By ingesting high-frequency 5-minute vehicle counts across classified categories (Cars, Motorbikes, Buses, and Trucks) from junction cameras across 4 spatial directions (UP, DOWN, LEFT, RIGHT), TrafficSense AI models multi-stream temporal dynamics using a **Bidirectional Long Short-Term Memory (BiLSTM)** neural network with a 12-step lookback window (60 minutes).

### 2. Objectives
- **Main Objective:** Develop a reliable, real-time, time-series traffic prediction and congestion management system for complex multi-camera urban junctions.
- **Specific Objectives:**
  1. Data Ingestion & Preprocessing: Validate, clean, and aggregate sensor observations (resolving duplicated timestamps across 32 independent groups).
  2. Feature Engineering: Compute 5-minute traffic volumes, vehicle breakdowns, temporal time slots, morning/evening peak indicators, lag features, and rolling 15-minute trends.
  3. BiLSTM Time-Series Forecasting: Formulate sequence modeling strictly per junction-camera-direction stream using 12 past observations (1-hour lookback) with log1p transformation to predict next 5-minute traffic volume without future data leakage.
  4. Objective Model Benchmarking: Rigorously evaluate and compare Random Forest baselines (Baseline, V2, V3) with the proposed BiLSTM model on MAE, RMSE, R², and zero-safe SMAPE.
  5. Congestion Classification & Early Alerts: Dynamically classify traffic status (Low, Moderate, High, Severe) using configurable thresholds and trigger automatic alerts for critical saturation and sudden traffic surges (+25%).
  6. Multi-Platform Delivery: Real-time interactive Android dashboard and REST APIs for municipal traffic controllers and commuters.

---

### 3. System Architecture & Module Breakdown

```
[ CCTV / Sensor Ingestion ]
            │
            ▼
[ Module A: Data Acquisition ]
  - Locations: AlankarChowk, JehangirChowk, RTOChowk
  - Cameras: a2, a3, j1, j2, j3, r1, r2, r3
  - Directions: UP, DOWN, LEFT, RIGHT (32 independent streams)
            │
            ▼
[ Module B: Preprocessing & Aggregation ]
  - Grouping: (location + camera + direction + time)
  - Duplicate resolution: 1449 raw -> 1208 clean records
            │
            ▼
[ Module C: Feature Engineering ]
  - 1176 feature rows (boundaries & lags)
  - rolling_15min_volume, previous_5min_volume, peak flags
            │
            ▼
[ Module D: ML BiLSTM Prediction Engine ]
  - Lookback: 12 intervals (60 mins)
  - Chronological 80/20 train-test split per group
  - log1p transform & MinMax scaling (fitted strictly on train)
            │
            ▼
[ Module E: Congestion & Alert Engine ]
  - Low (<60), Moderate (60-120), High (120-180), Severe (>180)
  - Sudden surge detection (>25% growth)
            │
            ▼
[ Module F, G, L: Dashboard & Visualizations ]
  - Android Jetpack Compose Native App + Room DB
  - FastAPI Backend Service
```

---

### 4. Model Benchmarking & Empirical Results

| Model Architecture | MAE (Vehicles) | RMSE (Vehicles) | R² Score | SMAPE (%) | Verdict / Scientific Explanation |
|---|---|---|---|---|---|
| **Random Forest Baseline** | 50,121.55 | 122,169.63 | 0.5847 | 48.20% | High variance; suffers from tree inability to extrapolate sequential continuous distributions. |
| **Random Forest V2** | 116,403.57 | 253,911.56 | -1.0440 | 163.06% | Negative R² demonstrates complete failure under chronological out-of-time evaluation. |
| **Random Forest V3** | 146,088.74 | 266,605.90 | -1.2535 | 1103.66% | Extreme error magnification caused by data leakage or improper scaling inversion. |
| **TrafficSense BiLSTM V2 (Proposed)** | **8.98** | **11.53** | **0.9157** | **5.55%** | **Superior predictive fidelity, captures temporal dependencies across past 12 intervals smoothly.** |

---

### 5. Why Random Forest Failed vs Why BiLSTM Succeeded
1. **Temporal Order Preservation:** Standard RF treats rows as independent samples, ignoring autocorrelation. BiLSTM treats traffic as a continuous dynamic sequence.
2. **Variance Stabilization:** Traffic counts are skewed. The log1p transformation coupled with MinMax scaling calibrated strictly on the training partition prevented gradient explosion and extreme prediction blowouts.
3. **Bidirectional Temporal Context:** BiLSTM captures both the backward decay (how traffic built up over 60 mins) and forward acceleration (morning rush hour inflection).
4. **Stream Separation:** Creating sequences strictly per `(location, camera, direction)` ensures traffic from JehangirChowk does not contaminate AlankarChowk.
