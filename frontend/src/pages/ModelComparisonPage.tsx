import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import { ModelMetrics } from '../api/types';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { Trophy, CheckCircle2 } from 'lucide-react';

export const ModelComparisonPage: React.FC = () => {
  const [models, setModels] = useState<ModelMetrics[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getModelComparison().then((data) => {
      setModels(data.models || []);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="p-8 text-slate-400">Loading model benchmarks...</div>;
  }

  const bestModel = models.reduce((prev, curr) => (curr.r2 > prev.r2 ? curr : prev), models[0] || {});

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Model Benchmarks & Comparison</h1>
        <p className="text-sm text-slate-400">Chronological 80/20 train/test evaluation across 5 baseline & deep learning architectures</p>
      </div>

      {bestModel.model && (
        <div className="bg-gradient-to-r from-purple-900/60 to-slate-800 p-5 rounded-xl border border-purple-500/50 shadow-xl flex items-center gap-4">
          <div className="p-3 bg-purple-500/20 text-purple-300 rounded-xl border border-purple-500/40">
            <Trophy className="w-8 h-8" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold bg-purple-500/30 text-purple-200 px-2.5 py-0.5 rounded-full border border-purple-400/40">BEST PERFORMING ARCHITECTURE</span>
              <span className="text-sm font-bold text-emerald-400">Validated by R² Metric</span>
            </div>
            <h2 className="text-2xl font-bold text-white mt-1">{bestModel.model}</h2>
            <p className="text-xs text-slate-300 mt-1">
              MAE: {bestModel.mae} • RMSE: {bestModel.rmse} • R²: {bestModel.r2} • sMAPE: {bestModel.smape}% • Accuracy: {(bestModel.accuracy * 100).toFixed(1)}%
            </p>
          </div>
        </div>
      )}

      <div className="bg-slate-800/80 rounded-xl border border-slate-700/60 overflow-hidden shadow-lg">
        <table className="w-full text-left text-sm text-slate-300">
          <thead className="bg-slate-900/80 text-xs text-slate-400 uppercase font-semibold border-b border-slate-700">
            <tr>
              <th className="px-5 py-3.5">Model Architecture</th>
              <th className="px-5 py-3.5">MAE (veh)</th>
              <th className="px-5 py-3.5">RMSE (veh)</th>
              <th className="px-5 py-3.5">R² Score</th>
              <th className="px-5 py-3.5">sMAPE (%)</th>
              <th className="px-5 py-3.5">Accuracy</th>
              <th className="px-5 py-3.5">F1 Score</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-700/60">
            {models.map((m, idx) => (
              <tr key={idx} className={m.model === bestModel.model ? 'bg-purple-950/20 font-semibold' : 'hover:bg-slate-700/30'}>
                <td className="px-5 py-3.5 text-white flex items-center gap-2">
                  {m.model === bestModel.model && <CheckCircle2 className="w-4 h-4 text-purple-400" />}
                  {m.model}
                </td>
                <td className="px-5 py-3.5">{m.mae}</td>
                <td className="px-5 py-3.5">{m.rmse}</td>
                <td className="px-5 py-3.5 text-emerald-400 font-bold">{m.r2}</td>
                <td className="px-5 py-3.5">{m.smape}%</td>
                <td className="px-5 py-3.5">{(m.accuracy * 100).toFixed(1)}%</td>
                <td className="px-5 py-3.5">{m.f1}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60">
        <h3 className="font-semibold text-white mb-4">R² Score Comparison Across Models</h3>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={models}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="model" stroke="#94a3b8" />
              <YAxis domain={[0, 1]} stroke="#94a3b8" />
              <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#475569', color: '#fff' }} />
              <Bar dataKey="r2" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
