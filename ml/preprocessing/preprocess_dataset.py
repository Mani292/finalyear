"""
Data Preprocessing Pipeline for TrafficSense AI
Location: ml/preprocessing/preprocess_dataset.py
"""
import os
import json
import pandas as pd
import numpy as np

def run_preprocessing():
    raw_path = "datasets/raw/pune_traffic_5min.csv"
    processed_dir = "datasets/processed"
    os.makedirs(processed_dir, exist_ok=True)

    if not os.path.exists(raw_path):
        print(f"Raw dataset not found at {raw_path}")
        return

    print(f"Loading raw data from {raw_path}...")
    df = pd.read_csv(raw_path, encoding='utf-8-sig')

    # Normalize column names
    df.columns = [col.strip().lower() for col in df.columns]

    # Check vehicle totals
    vehicle_cols = ['car_5min', 'motorbike_5min', 'bus_5min', 'truck_5min']
    for col in vehicle_cols:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0).clip(lower=0)

    df['total_vehicles_5min'] = df[vehicle_cols].sum(axis=1)

    # Aggregate repeated observations for location + camera + direction + time
    group_cols = ['location', 'camera', 'direction', 'time']
    df_clean = df.groupby(group_cols, as_index=False).agg({
        'car_5min': 'mean',
        'motorbike_5min': 'mean',
        'bus_5min': 'mean',
        'truck_5min': 'mean',
        'total_vehicles_5min': 'mean'
    })

    # Round counts
    for col in vehicle_cols + ['total_vehicles_5min']:
        df_clean[col] = df_clean[col].round().astype(int)

    # Temporal features
    if 'time' in df_clean.columns:
        df_clean['time_dt'] = pd.to_datetime(df_clean['time'], errors='coerce')
        df_clean['hour'] = df_clean['time_dt'].dt.hour
        df_clean['minute'] = df_clean['time_dt'].dt.minute
        df_clean['time_slot'] = df_clean['hour'] * 12 + df_clean['minute'] // 5
        df_clean['is_morning_peak'] = ((df_clean['hour'] >= 8) & (df_clean['hour'] <= 10)).astype(int)
        df_clean['is_evening_peak'] = ((df_clean['hour'] >= 17) & (df_clean['hour'] <= 19)).astype(int)
        df_clean = df_clean.drop(columns=['time_dt'])

    clean_path = os.path.join(processed_dir, "pune_traffic_5min_clean.csv")
    df_clean.to_csv(clean_path, index=False)
    print(f"Saved cleaned dataset with {len(df_clean)} unique observations to {clean_path}")

    # Report
    report = {
        "raw_rows": len(df),
        "clean_rows": len(df_clean),
        "locations": df_clean['location'].unique().tolist(),
        "cameras": df_clean['camera'].unique().tolist(),
        "directions": df_clean['direction'].unique().tolist(),
        "unique_streams": len(df_clean.groupby(['location', 'camera', 'direction']))
    }
    with open(os.path.join(processed_dir, "data_quality_report.json"), "w") as f:
        json.dump(report, f, indent=2)

if __name__ == "__main__":
    run_preprocessing()
