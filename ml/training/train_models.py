"""
Multi-Model ML Training Pipeline (CNN, BiLSTM, Transformer, Hybrid, Random Forest)
Location: ml/training/train_models.py
"""
import os
import json
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score, f1_score, accuracy_score

LOOKBACK = 12
HORIZONS = [3, 6, 12] # 15m, 30m, 60m

FEATURE_COLS = [
    'car_5min', 'motorbike_5min', 'bus_5min', 'truck_5min', 'total_vehicles_5min',
    'hour', 'minute', 'time_slot', 'is_morning_peak', 'is_evening_peak',
    'previous_5min_volume', 'lag_1', 'lag_2', 'lag_3', 'lag_6', 'lag_12',
    'rolling_15min_volume', 'rolling_mean_3', 'rolling_mean_6', 'rolling_mean_12', 'rolling_std_6',
    'volume_change', 'pct_volume_change', 'hour_sin', 'hour_cos', 'minute_sin', 'minute_cos'
]

class TrafficSequenceDataset(Dataset):
    def __init__(self, X, y):
        self.X = torch.tensor(X, dtype=torch.float32)
        self.y = torch.tensor(y, dtype=torch.float32)

    def __len__(self):
        return len(self.X)

    def __getitem__(self, idx):
        return self.X[idx], self.y[idx]

def build_sequences(df):
    X_seqs, y_seqs = [], []
    for _, group in df.groupby(['location', 'camera', 'direction']):
        group = group.sort_values('time').reset_index(drop=True)
        feat_data = group[FEATURE_COLS].values
        target_data = group['total_vehicles_5min'].values

        n = len(group)
        for i in range(n - LOOKBACK - 12 + 1):
            x_window = feat_data[i : i + LOOKBACK]
            y_window = [
                target_data[i + LOOKBACK + 3 - 1],  # 15 min
                target_data[i + LOOKBACK + 6 - 1],  # 30 min
                target_data[i + LOOKBACK + 12 - 1]  # 60 min
            ]
            X_seqs.append(x_window)
            y_seqs.append(y_window)

    return np.array(X_seqs), np.array(y_seqs)

# Architecture Definitions
class CNNModel(nn.Module):
    def __init__(self, input_dim, output_dim=3):
        super().__init__()
        self.conv1 = nn.Conv1d(input_dim, 64, kernel_size=3, padding=1)
        self.bn1 = nn.BatchNorm1d(64)
        self.relu = nn.ReLU()
        self.conv2 = nn.Conv1d(64, 128, kernel_size=3, padding=1)
        self.dropout = nn.Dropout(0.2)
        self.pool = nn.AdaptiveAvgPool1d(1)
        self.fc = nn.Linear(128, output_dim)

    def forward(self, x):
        # x shape: (batch, seq, feat) -> (batch, feat, seq)
        x = x.transpose(1, 2)
        x = self.relu(self.bn1(self.conv1(x)))
        x = self.dropout(self.relu(self.conv2(x)))
        x = self.pool(x).squeeze(-1)
        return self.fc(x)

class BiLSTMModel(nn.Module):
    def __init__(self, input_dim, hidden_dim=64, output_dim=3):
        super().__init__()
        self.lstm = nn.LSTM(input_dim, hidden_dim, num_layers=2, batch_first=True, bidirectional=True, dropout=0.2)
        self.fc = nn.Linear(hidden_dim * 2, output_dim)

    def forward(self, x):
        out, _ = self.lstm(x)
        return self.fc(out[:, -1, :])

class TransformerModel(nn.Module):
    def __init__(self, input_dim, d_model=64, nhead=4, output_dim=3):
        super().__init__()
        self.input_proj = nn.Linear(input_dim, d_model)
        encoder_layer = nn.TransformerEncoderLayer(d_model=d_model, nhead=nhead, dim_feedforward=128, dropout=0.2, batch_first=True)
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=2)
        self.fc = nn.Linear(d_model, output_dim)

    def forward(self, x):
        x = self.input_proj(x)
        out = self.transformer(x)
        return self.fc(out[:, -1, :])

class HybridModel(nn.Module):
    def __init__(self, input_dim, output_dim=3):
        super().__init__()
        # CNN Branch
        self.cnn_conv = nn.Conv1d(input_dim, 32, kernel_size=3, padding=1)
        self.cnn_pool = nn.AdaptiveAvgPool1d(1)

        # BiLSTM Branch
        self.bilstm = nn.LSTM(input_dim, 32, batch_first=True, bidirectional=True)

        # Transformer Branch
        self.trans_proj = nn.Linear(input_dim, 32)
        encoder_layer = nn.TransformerEncoderLayer(d_model=32, nhead=2, dim_feedforward=64, dropout=0.1, batch_first=True)
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=1)

        # Fusion
        # CNN: 32, BiLSTM: 64, Transformer: 32 -> Total = 128
        self.fusion = nn.Sequential(
            nn.Linear(32 + 64 + 32, 64),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(64, output_dim)
        )

    def forward(self, x):
        # CNN
        x_cnn = x.transpose(1, 2)
        cnn_feat = torch.relu(self.cnn_conv(x_cnn))
        cnn_feat = self.cnn_pool(cnn_feat).squeeze(-1)

        # BiLSTM
        lstm_out, _ = self.bilstm(x)
        bilstm_feat = lstm_out[:, -1, :]

        # Transformer
        trans_in = self.trans_proj(x)
        trans_out = self.transformer(trans_in)
        trans_feat = trans_out[:, -1, :]

        # Concatenate
        combined = torch.cat([cnn_feat, bilstm_feat, trans_feat], dim=1)
        return self.fusion(combined)

def train_torch_model(model, train_loader, val_loader, epochs=30, lr=0.001):
    criterion = nn.MSELoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=lr)

    best_loss = float('inf')
    best_weights = None

    for epoch in range(epochs):
        model.train()
        for batch_x, batch_y in train_loader:
            optimizer.zero_grad()
            preds = model(batch_x)
            loss = criterion(preds, batch_y)
            loss.backward()
            optimizer.step()

        model.eval()
        val_loss = 0
        with torch.no_grad():
            for batch_x, batch_y in val_loader:
                preds = model(batch_x)
                val_loss += criterion(preds, batch_y).item() * len(batch_x)
        val_loss /= len(val_loader.dataset)

        if val_loss < best_loss:
            best_loss = val_loss
            best_weights = model.state_dict()

    if best_weights:
        model.load_state_dict(best_weights)
    return model

def classify_vol(v):
    if v < 80: return 0 # LOW
    if v < 140: return 1 # MODERATE
    if v < 200: return 2 # HIGH
    return 3 # SEVERE

def evaluate(y_true, y_pred):
    mae = float(mean_absolute_error(y_true, y_pred))
    rmse = float(np.sqrt(mean_squared_error(y_true, y_pred)))
    r2 = float(r2_score(y_true, y_pred))

    # Classification metrics
    c_true = [classify_vol(v) for v in y_true.flatten()]
    c_pred = [classify_vol(v) for v in y_pred.flatten()]
    acc = float(accuracy_score(c_true, c_pred))
    f1 = float(f1_score(c_true, c_pred, average='macro', zero_division=0))

    return {
        "mae": round(mae, 2),
        "rmse": round(rmse, 2),
        "r2": round(r2, 4),
        "accuracy": round(acc, 4),
        "f1": round(f1, 4)
    }

def main():
    feat_path = "datasets/processed/traffic_features.csv"
    if not os.path.exists(feat_path):
        from ml.preprocessing.create_traffic_features import create_features
        create_features()

    df = pd.read_csv(feat_path)
    X_seqs, y_seqs = build_sequences(df)

    # Chronological Split (80% train, 20% test)
    split_idx = int(len(X_seqs) * 0.8)
    X_train_raw, X_test_raw = X_seqs[:split_idx], X_seqs[split_idx:]
    y_train_raw, y_test_raw = y_seqs[:split_idx], y_seqs[split_idx:]

    # Scale Features
    N_tr, S, F = X_train_raw.shape
    X_train_flat = X_train_raw.reshape(-1, F)
    scaler_X = StandardScaler()
    scaler_X.fit(X_train_flat)

    X_train_scaled = scaler_X.transform(X_train_flat).reshape(N_tr, S, F)

    N_te = X_test_raw.shape[0]
    X_test_scaled = scaler_X.transform(X_test_raw.reshape(-1, F)).reshape(N_te, S, F)

    # Scale Targets
    scaler_y = StandardScaler()
    y_train_scaled = scaler_y.fit_transform(y_train_raw)
    y_test_scaled = scaler_y.transform(y_test_raw)

    train_dataset = TrafficSequenceDataset(X_train_scaled, y_train_scaled)
    test_dataset = TrafficSequenceDataset(X_test_scaled, y_test_scaled)

    train_loader = DataLoader(train_dataset, batch_size=16, shuffle=False)
    test_loader = DataLoader(test_dataset, batch_size=16, shuffle=False)

    models = {
        "CNN": CNNModel(F),
        "BiLSTM": BiLSTMModel(F),
        "Transformer": TransformerModel(F),
        "Hybrid": HybridModel(F)
    }

    results = []
    trained_models = {}

    os.makedirs("ml/models", exist_ok=True)

    for name, m in models.items():
        print(f"Training {name}...")
        m = train_torch_model(m, train_loader, test_loader, epochs=40)
        m.eval()
        with torch.no_grad():
            preds_scaled = m(torch.tensor(X_test_scaled, dtype=torch.float32)).numpy()
        preds = scaler_y.inverse_transform(preds_scaled)

        metrics = evaluate(y_test_raw, preds)
        metrics["model"] = name
        results.append(metrics)

        torch.save(m.state_dict(), f"ml/models/{name.lower()}_model.pt")

    # Baseline Random Forest
    print("Training Random Forest baseline...")
    rf_X_tr = X_train_scaled[:, -1, :] # last step feature
    rf = RandomForestRegressor(n_estimators=100, random_state=42)
    rf.fit(rf_X_tr, y_train_raw)
    rf_preds = rf.predict(X_test_scaled[:, -1, :])
    rf_metrics = evaluate(y_test_raw, rf_preds)
    rf_metrics["model"] = "RandomForest"
    results.append(rf_metrics)

    import joblib
    joblib.dump(scaler_X, "ml/models/scaler_X.pkl")
    joblib.dump(scaler_y, "ml/models/scaler_y.pkl")
    joblib.dump(rf, "ml/models/randomforest_model.pkl")

    meta = {
        "feature_cols": FEATURE_COLS,
        "lookback": LOOKBACK,
        "horizons": [15, 30, 60],
        "models": results
    }
    with open("ml/models/model_comparison.json", "w") as f:
        json.dump(meta, f, indent=2)

    print("Model comparison metrics:")
    print(json.dumps(results, indent=2))

if __name__ == "__main__":
    main()
