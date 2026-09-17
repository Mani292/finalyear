import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { PredictionResponse } from '../api/types';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts';
import { Clock, Cpu, Sparkles } from 'lucide-react';

export const ForecastPage: React.FC = () => {
  const [location, setLocation] = useState('AlankarChowk');
  const [camera, setCamera] = useState('a2');
  const [direction, setDirection] = useState('DOWN');
  const [model, setModel] = useState('Hybrid');
  const [prediction, setPrediction] = useState<PredictionResponse | null>(null);

  const fetchForecast = () => {
    api.predict(location, camera, direction, model)
      .then((data) => {
        setPrediction(data);
      })
      .catch(() => {});
  };

  useEffect(() => {
    fetchForecast();
  }, [location, camera, direction, model]);

  const chartData = [
    { time: 'T-15m', volume: 110, type: 'Historical' },
    { time: 'T-10m', volume: 118, type: 'Historical' },
    { time: 'T-5m', volume: 125, type: 'Historical' },
    { time: 'Current (T)', volume: 132, type: 'Historical' },
    { time: 'T+15m', volume: prediction ? prediction.forecast['15min'].volume : 140, type: 'Predicted' },
    { time: 'T+30m', volume: prediction ? prediction.forecast['30min'].volume : 155, type: 'Predicted' },
    { time: 'T+60m', volume: prediction ? prediction.forecast['60min'].volume : 180, type: 'Predicted' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Multi-Horizon Traffic Forecast</h1>
        <p className="text-sm text-slate-400">15-minute, 30-minute, and 60-minute future volume & congestion predictions</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 bg-slate-800 p-4 rounded-xl border border-slate-700">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">LOCATION</label>
          <select
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="AlankarChowk">AlankarChowk</option>
            <option value="JehangirChowk">JehangirChowk</option>
            <option value="RTOChowk">RTOChowk</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">CAMERA</label>
          <select
            value={camera}
            onChange={(e) => setCamera(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="a2">a2</option>
            <option value="a3">a3</option>
            <option value="j1">j1</option>
            <option value="j2">j2</option>
            <option value="r1">r1</option>
            <option value="r2">r2</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">DIRECTION</label>
          <select
            value={direction}
            onChange={(e) => setDirection(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="UP">UP</option>
            <option value="DOWN">DOWN</option>
            <option value="LEFT">LEFT</option>
            <option value="RIGHT">RIGHT</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">AI MODEL</label>
          <select
            value={model}
            onChange={(e) => setModel(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="Hybrid">Hybrid (CNN-BiLSTM-Trans)</option>
            <option value="Transformer">Transformer</option>
            <option value="BiLSTM">BiLSTM</option>
            <option value="CNN">CNN 1D</option>
            <option value="RandomForest">Random Forest Baseline</option>
          </select>
        </div>
      </div>

      {prediction && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            { horizon: '15 Minutes', data: prediction.forecast['15min'], icon: Clock, color: 'emerald' },
            { horizon: '30 Minutes', data: prediction.forecast['30min'], icon: Sparkles, color: 'amber' },
            { horizon: '60 Minutes', data: prediction.forecast['60min'], icon: Cpu, color: 'purple' },
          ].map((item, idx) => (
            <div key={idx} className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60 shadow-md">
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">{item.horizon} Forecast</span>
                <item.icon className="w-5 h-5 text-emerald-400" />
              </div>
              <div className="text-3xl font-extrabold text-white mb-1">
                {item.data.volume} <span className="text-xs font-normal text-slate-400">vehicles</span>
              </div>
              <div className="flex items-center gap-2 mt-2">
                <span className={`px-2.5 py-0.5 rounded-md text-xs font-bold ${
                  item.data.congestion === 'SEVERE' ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30' :
                  item.data.congestion === 'HIGH' ? 'bg-red-500/20 text-red-300 border border-red-500/30' :
                  item.data.congestion === 'MODERATE' ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30' :
                  'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                }`}>
                  {item.data.congestion}
                </span>
                <span className="text-xs text-slate-400">Confidence: {(item.data.confidence * 100).toFixed(0)}%</span>
              </div>
              <p className="text-xs text-slate-400 mt-3 border-t border-slate-700/60 pt-2">{item.data.description}</p>
            </div>
          ))}
        </div>
      )}

      <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60">
        <h3 className="font-semibold text-white mb-4">Historical vs. Multi-Horizon Projected Volume</h3>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="time" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#475569', color: '#fff' }} />
              <Legend />
              <Line type="monotone" dataKey="volume" stroke="#10b981" strokeWidth={3} dot={{ r: 5 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
