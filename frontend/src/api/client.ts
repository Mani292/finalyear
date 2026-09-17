import axios from 'axios';
import {
  PredictionResponse,
  RouteRecommendationResponse,
  ModelComparisonResponse,
  TrafficStream
} from './types';

const API_BASE = '/api';

export const api = {
  getHealth: async () => {
    const res = await axios.get(`${API_BASE}/health`);
    return res.data;
  },

  getLocations: async () => {
    const res = await axios.get(`${API_BASE}/locations`);
    return res.data;
  },

  getCurrentTraffic: async (): Promise<{ total_streams: number; traffic: TrafficStream[] }> => {
    const res = await axios.get(`${API_BASE}/traffic/current`);
    return res.data;
  },

  getTrafficForecast: async (location: string, camera: string, direction: string, model: string = 'Hybrid') => {
    const res = await axios.get(`${API_BASE}/traffic/forecast`, {
      params: { location, camera, direction, model_name: model }
    });
    return res.data;
  },

  predict: async (location: string, camera: string, direction: string, modelName: string = 'Hybrid'): Promise<PredictionResponse> => {
    const res = await axios.post(`${API_BASE}/predict`, {
      location,
      camera,
      direction,
      model_name: modelName
    });
    return res.data;
  },

  recommendRoute: async (source: string, destination: string, departureTime: string = 'Immediate', modelName: string = 'Hybrid'): Promise<RouteRecommendationResponse> => {
    const res = await axios.post(`${API_BASE}/routes/recommend`, {
      source,
      destination,
      departure_time: departureTime,
      model_name: modelName
    });
    return res.data;
  },

  getModelComparison: async (): Promise<ModelComparisonResponse> => {
    const res = await axios.get(`${API_BASE}/models/comparison`);
    return res.data;
  },

  getAnalytics: async () => {
    const res = await axios.get(`${API_BASE}/analytics`);
    return res.data;
  }
};
