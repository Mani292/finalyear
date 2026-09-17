import React from 'react';
import { Database, Server, CheckCircle2 } from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Historical Traffic Analytics</h1>
        <p className="text-sm text-slate-400">Pune heterogeneous traffic observation analysis & vehicle composition</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-slate-800 p-5 rounded-xl border border-slate-700">
          <h3 className="text-slate-400 text-xs font-bold uppercase mb-2">Dataset Scope</h3>
          <p className="text-2xl font-bold text-white">1,208 Records</p>
          <p className="text-xs text-slate-400 mt-1">Cleaned unique stream 5-min intervals</p>
        </div>
        <div className="bg-slate-800 p-5 rounded-xl border border-slate-700">
          <h3 className="text-slate-400 text-xs font-bold uppercase mb-2">Monitored Streams</h3>
          <p className="text-2xl font-bold text-white">32 Streams</p>
          <p className="text-xs text-slate-400 mt-1">3 Locations • 8 Cameras • 4 Directions</p>
        </div>
        <div className="bg-slate-800 p-5 rounded-xl border border-slate-700">
          <h3 className="text-slate-400 text-xs font-bold uppercase mb-2">Primary Vehicle Class</h3>
          <p className="text-2xl font-bold text-emerald-400">Two-Wheelers (62%)</p>
          <p className="text-xs text-slate-400 mt-1">Typical Pune urban heterogeneous mix</p>
        </div>
      </div>
    </div>
  );
};

export const SystemInfoPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">System Status & Architecture</h1>
        <p className="text-sm text-slate-400">TrafficSense AI Intelligent Transportation System diagnostic details</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 space-y-4">
          <h3 className="font-bold text-white flex items-center gap-2">
            <Server className="w-5 h-5 text-emerald-400" /> Backend API Server
          </h3>
          <div className="space-y-2 text-sm text-slate-300">
            <div className="flex justify-between border-b border-slate-700 pb-1">
              <span>Framework:</span> <span className="font-semibold text-white">FastAPI v2.0.0</span>
            </div>
            <div className="flex justify-between border-b border-slate-700 pb-1">
              <span>Status:</span> <span className="font-semibold text-emerald-400 flex items-center gap-1"><CheckCircle2 className="w-3.5 h-3.5" /> ONLINE</span>
            </div>
            <div className="flex justify-between border-b border-slate-700 pb-1">
              <span>Port:</span> <span className="font-semibold text-white">8000</span>
            </div>
          </div>
        </div>

        <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 space-y-4">
          <h3 className="font-bold text-white flex items-center gap-2">
            <Database className="w-5 h-5 text-blue-400" /> Database Engine
          </h3>
          <div className="space-y-2 text-sm text-slate-300">
            <div className="flex justify-between border-b border-slate-700 pb-1">
              <span>Primary DB:</span> <span className="font-semibold text-white">Supabase PostgreSQL</span>
            </div>
            <div className="flex justify-between border-b border-slate-700 pb-1">
              <span>Fallback DB:</span> <span className="font-semibold text-white">SQLite (backend/trafficsense.db)</span>
            </div>
            <div className="flex justify-between border-b border-slate-700 pb-1">
              <span>Status:</span> <span className="font-semibold text-emerald-400 flex items-center gap-1"><CheckCircle2 className="w-3.5 h-3.5" /> CONNECTED</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
