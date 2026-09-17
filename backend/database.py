"""
Database Access Layer (Supabase PostgreSQL Client with SQLite Fallback)
Location: backend/database.py
"""
import os
import sqlite3
import pandas as pd
from typing import List, Dict, Any

class DatabaseManager:
    def __init__(self):
        self.supabase_url = os.getenv("SUPABASE_URL")
        self.supabase_key = os.getenv("SUPABASE_ANON_KEY")
        self.client = None

        if self.supabase_url and self.supabase_key:
            try:
                from supabase import create_client
                self.client = create_client(self.supabase_url, self.supabase_key)
                print("Connected to Supabase PostgreSQL Database.")
            except Exception as e:
                print(f"Supabase connection attempt failed: {e}. Falling back to SQLite.")

        self.sqlite_db_path = "backend/trafficsense.db"
        self._init_sqlite()

    def _init_sqlite(self):
        os.makedirs(os.path.dirname(self.sqlite_db_path), exist_ok=True)
        conn = sqlite3.connect(self.sqlite_db_path)
        feat_path = "datasets/processed/traffic_features.csv"
        if os.path.exists(feat_path):
            df = pd.read_csv(feat_path)
            df.to_sql("traffic_observations", conn, if_exists="replace", index=False)
        conn.close()

    def get_current_traffic(self) -> List[Dict[str, Any]]:
        conn = sqlite3.connect(self.sqlite_db_path)
        df = pd.read_sql_query("""
            SELECT t1.location, t1.camera, t1.direction, t1.time, t1.total_vehicles_5min, t1.car_5min, t1.motorbike_5min, t1.bus_5min, t1.truck_5min, t1.hour, t1.minute
            FROM traffic_observations t1
            INNER JOIN (
                SELECT location, camera, direction, MAX(time) as max_time
                FROM traffic_observations
                GROUP BY location, camera, direction
            ) t2 ON t1.location = t2.location AND t1.camera = t2.camera AND t1.direction = t2.direction AND t1.time = t2.max_time
        """, conn)
        conn.close()
        return df.to_dict(orient="records")

    def get_stream_history(self, location: str, camera: str, direction: str, limit: int = 50) -> pd.DataFrame:
        conn = sqlite3.connect(self.sqlite_db_path)
        query = """
            SELECT * FROM traffic_observations
            WHERE location = ? AND camera = ? AND direction = ?
            ORDER BY time ASC
        """
        df = pd.read_sql_query(query, conn, params=(location, camera, direction))
        conn.close()
        return df.tail(limit)

db = DatabaseManager()
