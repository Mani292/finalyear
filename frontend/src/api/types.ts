export interface TrafficStream {
  location: string;
  camera: string;
  direction: string;
  time: string;
  total_vehicles_5min: number;
  car_5min: number;
  motorbike_5min: number;
  bus_5min: number;
  truck_5min: number;
  hour: number;
  minute: number;
}

export interface HorizonForecast {
  volume: number;
  congestion: string;
  confidence: number;
  description: string;
}

export interface PredictionResponse {
  location: string;
  camera: string;
  direction: string;
  model_used: string;
  forecast: {
    "15min": HorizonForecast;
    "30min": HorizonForecast;
    "60min": HorizonForecast;
  };
}

export interface RouteDetail {
  id: string;
  name: string;
  distance_km: number;
  expected_travel_time_min: number;
  expected_delay_min: number;
  predicted_congestion: string;
  predicted_future_volume: number;
  route_score: number;
  camera_used?: string;
  direction_used?: string;
}

export interface RouteRecommendationResponse {
  source: string;
  destination: string;
  departure_time: string;
  model_used: string;
  routing_provider?: string;
  recommended_route: RouteDetail;
  alternative_routes: RouteDetail[];
  explanation: string;
}

export interface ModelMetrics {
  model: string;
  mae: number;
  rmse: number;
  r2: number;
  smape: number;
  accuracy: number;
  precision: number;
  recall: number;
  f1: number;
}

export interface ModelComparisonResponse {
  feature_cols: string[];
  lookback: number;
  horizons: number[];
  models: ModelMetrics[];
}
