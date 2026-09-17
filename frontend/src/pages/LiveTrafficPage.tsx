import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import { TrafficStream } from '../api/types';
import { Car, Bike, Bus, Truck, Filter } from 'lucide-react';

export const LiveTrafficPage: React.FC = () => {
  const [streams, setStreams] = useState<TrafficStream[]>([]);
  const [selectedLoc, setSelectedLoc] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getCurrentTraffic().then((data) => {
      setStreams(data.traffic || []);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const filtered = selectedLoc === 'ALL'
    ? streams
    : streams.filter(s => s.location === selectedLoc);

  if (loading) {
    return <div className="p-8 text-slate-400">Loading live streams...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Live Traffic Streams</h1>
          <p className="text-sm text-slate-400">Heterogeneous traffic breakdown across Pune cameras & directions</p>
        </div>

        <div className="flex items-center gap-2 bg-slate-800 px-3 py-2 rounded-xl border border-slate-700">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={selectedLoc}
            onChange={(e) => setSelectedLoc(e.target.value)}
            className="bg-transparent text-sm text-white focus:outline-none cursor-pointer"
          >
            <option value="ALL" className="bg-slate-800">All Locations</option>
            <option value="AlankarChowk" className="bg-slate-800">AlankarChowk</option>
            <option value="JehangirChowk" className="bg-slate-800">JehangirChowk</option>
            <option value="RTOChowk" className="bg-slate-800">RTOChowk</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map((stream, idx) => (
          <div key={idx} className="bg-slate-800/80 p-5 rounded-xl border border-slate-700/60 shadow-md hover:border-slate-600 transition-all">
            <div className="flex items-center justify-between mb-3 border-b border-slate-700/60 pb-2">
              <div>
                <h3 className="font-bold text-white">{stream.location}</h3>
                <p className="text-xs text-slate-400">Cam: <span className="text-emerald-400 font-semibold">{stream.camera}</span> | Dir: <span className="text-blue-400 font-semibold">{stream.direction}</span></p>
              </div>
              <div className="text-right">
                <span className="text-xl font-bold text-white">{stream.total_vehicles_5min}</span>
                <span className="text-[10px] text-slate-400 block">veh/5min</span>
              </div>
            </div>

            <div className="grid grid-cols-4 gap-2 text-center text-xs">
              <div className="bg-slate-900/60 p-2 rounded-lg border border-slate-700/40">
                <Car className="w-4 h-4 mx-auto text-blue-400 mb-1" />
                <span className="text-slate-400 block text-[10px]">Cars</span>
                <span className="font-semibold text-white">{stream.car_5min}</span>
              </div>
              <div className="bg-slate-900/60 p-2 rounded-lg border border-slate-700/40">
                <Bike className="w-4 h-4 mx-auto text-emerald-400 mb-1" />
                <span className="text-slate-400 block text-[10px]">Bikes</span>
                <span className="font-semibold text-white">{stream.motorbike_5min}</span>
              </div>
              <div className="bg-slate-900/60 p-2 rounded-lg border border-slate-700/40">
                <Bus className="w-4 h-4 mx-auto text-amber-400 mb-1" />
                <span className="text-slate-400 block text-[10px]">Buses</span>
                <span className="font-semibold text-white">{stream.bus_5min}</span>
              </div>
              <div className="bg-slate-900/60 p-2 rounded-lg border border-slate-700/40">
                <Truck className="w-4 h-4 mx-auto text-purple-400 mb-1" />
                <span className="text-slate-400 block text-[10px]">Trucks</span>
                <span className="font-semibold text-white">{stream.truck_5min}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
