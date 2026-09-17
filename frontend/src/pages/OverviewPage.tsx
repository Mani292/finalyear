import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import { TrafficStream } from '../api/types';
import { Activity, Radio, AlertTriangle, TrendingUp, Cpu } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

export const OverviewPage: React.FC = () => {
  const [streams, setStreams] = useState<TrafficStream[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getCurrentTraffic().then((data) => {
      setStreams(data.traffic || []);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const totalVol = streams.reduce((acc, s) => acc + s.total_vehicles_5min, 0);
  const avgVol = streams.length > 0 ? Math.round(totalVol / streams.length) : 0;
  const severeCount = streams.filter(s => s.total_vehicles_5min >= 200).length;

  const locSummary = [
    { name: 'AlankarChowk', vol: streams.filter(s => s.location === 'AlankarChowk').reduce((a, b) => a + b.total_vehicles_5min, 0) },
    { name: 'JehangirChowk', vol: streams.filter(s => s.location === 'JehangirChowk').reduce((a, b) => a + b.total_vehicles_5min, 0) },
    { name: 'RTOChowk', vol: streams.filter(s => s.location === 'RTOChowk').reduce((a, b) => a + b.total_vehicles_5min, 0) },
  ];

  if (loading) {
    return <div className="p-8 text-slate-400">Loading TrafficSense Overview...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">System Overview</h1>
        <p className="text-sm text-slate-400">Real-time traffic stream analytics & hybrid deep-learning prediction status</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60 shadow-lg">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase">Active Streams</span>
            <Radio className="w-5 h-5 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-white">{streams.length}</div>
          <p className="text-xs text-slate-400 mt-1">3 Locations • 8 Cameras</p>
        </div>

        <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60 shadow-lg">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase">Current Total Volume</span>
            <Activity className="w-5 h-5 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-white">{totalVol} <span className="text-xs text-slate-400 font-normal">veh/5min</span></div>
          <p className="text-xs text-slate-400 mt-1">Avg: {avgVol} veh/stream</p>
        </div>

        <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60 shadow-lg">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase">High/Severe Streams</span>
            <AlertTriangle className="w-5 h-5 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-amber-400">{severeCount}</div>
          <p className="text-xs text-slate-400 mt-1">Require rerouting attention</p>
        </div>

        <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60 shadow-lg">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase">Primary Model</span>
            <Cpu className="w-5 h-5 text-purple-400" />
          </div>
          <div className="text-lg font-bold text-purple-300">CNN-BiLSTM-Trans</div>
          <p className="text-xs text-emerald-400 mt-1 flex items-center gap-1">
            <TrendingUp className="w-3 h-3" /> R² Score: 0.8806
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60">
          <h3 className="font-semibold text-white mb-4">Traffic Volume Distribution by Junction</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={locSummary}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="name" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#475569', color: '#fff' }} />
                <Bar dataKey="vol" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60">
          <h3 className="font-semibold text-white mb-4">Monitored Junctions Status</h3>
          <div className="space-y-3">
            {[
              { name: 'AlankarChowk', status: 'HIGH', vol: 142, desc: 'Station arterial road experiencing evening surge' },
              { name: 'JehangirChowk', status: 'MODERATE', vol: 98, desc: 'Sassoon flyover flow steady' },
              { name: 'RTOChowk', status: 'SEVERE', vol: 215, desc: 'Sangam Bridge bottleneck severe congestion' },
            ].map((j) => (
              <div key={j.name} className="flex items-center justify-between p-3 rounded-lg bg-slate-900/60 border border-slate-700/40">
                <div>
                  <h4 className="font-medium text-slate-200">{j.name}</h4>
                  <p className="text-xs text-slate-400">{j.desc}</p>
                </div>
                <div className="text-right">
                  <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                    j.status === 'SEVERE' ? 'bg-purple-500/20 text-purple-300 border border-purple-500/40' :
                    j.status === 'HIGH' ? 'bg-red-500/20 text-red-300 border border-red-500/40' :
                    'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                  }`}>
                    {j.status} ({j.vol} v)
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
