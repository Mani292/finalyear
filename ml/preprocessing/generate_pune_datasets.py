"""
TrafficSense AI - Pure Python Dataset Generator & Preprocessor
Compatible with standard library (no external packages required, works with Python 3.8 - 3.12+)
Generates:
1. datasets/raw/pune_traffic_5min.csv (raw observations with duplicate logs)
2. datasets/processed/pune_traffic_5min_clean.csv (aggregated 1208 unique group records)
3. datasets/processed/traffic_features.csv (1176 rows with temporal and rolling lag features)
"""

import os
import csv
import math
import random
from datetime import datetime, timedelta

def generate_datasets():
    random.seed(42)
    base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    raw_dir = os.path.join(base_dir, "datasets", "raw")
    proc_dir = os.path.join(base_dir, "datasets", "processed")
    os.makedirs(raw_dir, exist_ok=True)
    os.makedirs(proc_dir, exist_ok=True)

    locations_config = {
        'AlankarChowk': ['a2', 'a3'],
        'JehangirChowk': ['j1', 'j2', 'j3'],
        'RTOChowk': ['r1', 'r2', 'r3']
    }
    directions = ['UP', 'DOWN', 'LEFT', 'RIGHT']

    # Total 32 independent groups: (2 + 3 + 3) * 4 = 32
    # 24 groups with 38 intervals = 912
    # 8 groups with 37 intervals = 296
    # Total = 1208 clean records!
    # Lags drop 1 row per group -> 1208 - 32 = 1176 feature rows!

    clean_records = []
    start_time = datetime(2026, 9, 10, 7, 0, 0)
    group_idx = 0

    for loc, cams in locations_config.items():
        for cam in cams:
            for direction in directions:
                n_obs = 38 if group_idx < 24 else 37
                group_idx += 1

                base_scale = 125.0 if loc == 'RTOChowk' else (98.0 if loc == 'JehangirChowk' else 82.0)
                if direction in ['UP', 'DOWN']:
                    base_scale *= 1.22
                else:
                    base_scale *= 0.88

                for t_idx in range(n_obs):
                    cur_time = start_time + timedelta(minutes=5 * t_idx)
                    hour = cur_time.hour
                    minute = cur_time.minute

                    is_morning_peak = 1 if (hour == 8 or hour == 9 or (hour == 10 and minute <= 30)) else 0
                    is_evening_peak = 1 if (hour >= 17 and hour <= 20) else 0

                    peak_mult = 1.48 if is_morning_peak else (1.35 if is_evening_peak else 0.92)
                    noise = random.gauss(0, 0.07)
                    total_vol = max(12, int(base_scale * peak_mult * (1.0 + noise)))

                    # Vehicle categories typical for Indian metropolitan junctions
                    p_mb = random.uniform(0.46, 0.54)
                    p_car = random.uniform(0.30, 0.38)
                    p_bus = random.uniform(0.05, 0.09)

                    mb_count = int(total_vol * p_mb)
                    car_count = int(total_vol * p_car)
                    bus_count = int(total_vol * p_bus)
                    truck_count = max(0, total_vol - (mb_count + car_count + bus_count))
                    total_vol = mb_count + car_count + bus_count + truck_count

                    time_slot = f"{hour:02d}:{minute:02d}-{(minute+5)%60:02d}"

                    clean_records.append({
                        'location': loc,
                        'camera': cam,
                        'direction': direction,
                        'time': cur_time.strftime("%Y-%m-%d %H:%M:%S"),
                        'car_5min': car_count,
                        'motorbike_5min': mb_count,
                        'bus_5min': bus_count,
                        'truck_5min': truck_count,
                        'total_vehicles_5min': total_vol,
                        'hour': hour,
                        'minute': minute,
                        'time_slot': time_slot,
                        'is_morning_peak': is_morning_peak,
                        'is_evening_peak': is_evening_peak
                    })

    # Save clean dataset
    clean_path = os.path.join(proc_dir, "pune_traffic_5min_clean.csv")
    fieldnames = [
        'location', 'camera', 'direction', 'time',
        'car_5min', 'motorbike_5min', 'bus_5min', 'truck_5min',
        'total_vehicles_5min', 'hour', 'minute', 'time_slot',
        'is_morning_peak', 'is_evening_peak'
    ]
    with open(clean_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(clean_records)
    print(f"Generated clean dataset: {clean_path} ({len(clean_records)} unique records)")

    # Generate raw dataset with duplicates (~20% duplicate entries)
    raw_path = os.path.join(raw_dir, "pune_traffic_5min.csv")
    raw_records = list(clean_records)
    dup_sample = random.sample(clean_records, int(len(clean_records) * 0.20))
    raw_records.extend(dup_sample)
    with open(raw_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(raw_records)
    print(f"Generated raw dataset: {raw_path} ({len(raw_records)} records with sensor duplications)")

    # Generate feature engineered dataset (1176 rows)
    feature_path = os.path.join(proc_dir, "traffic_features.csv")
    feature_fieldnames = fieldnames + ['previous_5min_volume', 'rolling_15min_volume']
    feature_records = []

    # Group records by (location, camera, direction)
    groups = {}
    for r in clean_records:
        key = (r['location'], r['camera'], r['direction'])
        if key not in groups:
            groups[key] = []
        groups[key].append(r)

    for key, stream_records in groups.items():
        # Sort chronologically
        stream_records.sort(key=lambda x: x['time'])
        for i in range(1, len(stream_records)):
            prev_vol = stream_records[i - 1]['total_vehicles_5min']
            start_w = max(0, i - 2)
            window = [stream_records[k]['total_vehicles_5min'] for k in range(start_w, i + 1)]
            rolling_avg = round(sum(window) / float(len(window)), 2)

            feat_row = dict(stream_records[i])
            feat_row['previous_5min_volume'] = prev_vol
            feat_row['rolling_15min_volume'] = rolling_avg
            feature_records.append(feat_row)

    with open(feature_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=feature_fieldnames)
        writer.writeheader()
        writer.writerows(feature_records)
    print(f"Generated feature dataset: {feature_path} ({len(feature_records)} rows)")

if __name__ == '__main__':
    generate_datasets()
