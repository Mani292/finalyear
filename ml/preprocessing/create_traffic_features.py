"""
Feature Engineering Module for TrafficSense AI
Location: ml/preprocessing/create_traffic_features.py
"""
import os
import pandas as pd
import numpy as np

def create_features():
    clean_path = "datasets/processed/pune_traffic_5min_clean.csv"
    if not os.path.exists(clean_path):
        from ml.preprocessing.preprocess_dataset import run_preprocessing
        run_preprocessing()

    df = pd.read_csv(clean_path)

    # Sort chronologically by stream
    df['time_dt'] = pd.to_datetime(df['time'], errors='coerce')
    df = df.sort_values(by=['location', 'camera', 'direction', 'time_dt']).reset_index(drop=True)

    feature_dfs = []

    for (loc, cam, direct), group in df.groupby(['location', 'camera', 'direction']):
        group = group.copy()
        v = group['total_vehicles_5min']

        # Lag features
        group['previous_5min_volume'] = v.shift(1).bfill()
        group['lag_1'] = v.shift(1).bfill()
        group['lag_2'] = v.shift(2).bfill()
        group['lag_3'] = v.shift(3).bfill()
        group['lag_6'] = v.shift(6).bfill()
        group['lag_12'] = v.shift(12).bfill()

        # Rolling features
        group['rolling_15min_volume'] = v.rolling(window=3, min_periods=1).mean()
        group['rolling_mean_3'] = v.rolling(window=3, min_periods=1).mean()
        group['rolling_mean_6'] = v.rolling(window=6, min_periods=1).mean()
        group['rolling_mean_12'] = v.rolling(window=12, min_periods=1).mean()
        group['rolling_std_6'] = v.rolling(window=6, min_periods=1).std().fillna(0)

        # Change features
        group['volume_change'] = v - group['lag_1']
        group['pct_volume_change'] = ((v - group['lag_1']) / (group['lag_1'] + 1e-5)).clip(-1.0, 2.0)

        feature_dfs.append(group)

    full_df = pd.concat(feature_dfs, ignore_index=True)

    # Cyclic time features
    if 'hour' in full_df.columns and 'minute' in full_df.columns:
        full_df['hour_sin'] = np.sin(2 * np.pi * full_df['hour'] / 24.0)
        full_df['hour_cos'] = np.cos(2 * np.pi * full_df['hour'] / 24.0)
        full_df['minute_sin'] = np.sin(2 * np.pi * full_df['minute'] / 60.0)
        full_df['minute_cos'] = np.cos(2 * np.pi * full_df['minute'] / 60.0)

    full_df = full_df.drop(columns=['time_dt'], errors='ignore')
    out_path = "datasets/processed/traffic_features.csv"
    full_df.to_csv(out_path, index=False)
    print(f"Saved traffic features with {len(full_df)} rows and {len(full_df.columns)} columns to {out_path}")

if __name__ == "__main__":
    create_features()
