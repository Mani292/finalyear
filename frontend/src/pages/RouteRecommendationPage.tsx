import React, { useState } from 'react';
import { api } from '../api/client';
import { RouteRecommendationResponse } from '../api/types';
import { Navigation, ShieldCheck, Clock, Sparkles } from 'lucide-react';

export const RouteRecommendationPage: React.FC = () => {
  const [source, setSource] = useState('AlankarChowk');
  const [destination, setDestination] = useState('RTOChowk');
  const [modelName, setModelName] = useState('Hybrid');
  const [routeRes, setRouteRes] = useState<RouteRecommendationResponse | null>(null);
  const [loading, setLoading] = useState(false);

  const handleRecommend = () => {
    setLoading(true);
    api.recommendRoute(source, destination, 'Immediate', modelName)
      .then((data) => {
        setRouteRes(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Future-Aware Route Recommendation</h1>
        <p className="text-sm text-slate-400">Routes evaluated using predicted future traffic at arrival times (T+5m, T+15m, T+30m)</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 bg-slate-800 p-4 rounded-xl border border-slate-700">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">SOURCE JUNCTION</label>
          <select
            value={source}
            onChange={(e) => setSource(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="AlankarChowk">AlankarChowk</option>
            <option value="JehangirChowk">JehangirChowk</option>
            <option value="RTOChowk">RTOChowk</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">DESTINATION JUNCTION</label>
          <select
            value={destination}
            onChange={(e) => setDestination(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="RTOChowk">RTOChowk</option>
            <option value="JehangirChowk">JehangirChowk</option>
            <option value="AlankarChowk">AlankarChowk</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">PREDICTION MODEL</label>
          <select
            value={modelName}
            onChange={(e) => setModelName(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg p-2 text-sm focus:outline-none"
          >
            <option value="Hybrid">Hybrid (CNN-BiLSTM-Trans)</option>
            <option value="Transformer">Transformer</option>
            <option value="BiLSTM">BiLSTM</option>
            <option value="CNN">CNN 1D</option>
            <option value="RandomForest">Random Forest Baseline</option>
          </select>
        </div>

        <div className="flex items-end">
          <button
            onClick={handleRecommend}
            disabled={loading}
            className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold p-2 rounded-lg transition-colors flex items-center justify-center gap-2 shadow-md shadow-emerald-900/40 cursor-pointer disabled:opacity-50"
          >
            <Navigation className="w-4 h-4" />
            {loading ? 'Evaluating...' : 'Find Best Future Route'}
          </button>
        </div>
      </div>

      {routeRes && (
        <div className="space-y-6">
          <div className="bg-emerald-950/40 p-5 rounded-xl border border-emerald-500/40 text-emerald-200 flex items-start gap-3">
            <Sparkles className="w-6 h-6 text-emerald-400 shrink-0 mt-0.5" />
            <div>
              <h3 className="font-bold text-emerald-300 mb-1">Why this route is recommended?</h3>
              <p className="text-sm text-emerald-200/90 leading-relaxed">{routeRes.explanation}</p>
            </div>
          </div>

          <div className="bg-slate-800 p-6 rounded-xl border-2 border-emerald-500/80 shadow-xl relative overflow-hidden">
            <div className="absolute top-0 right-0 bg-emerald-500 text-slate-950 px-4 py-1 rounded-bl-xl font-extrabold text-xs tracking-wider flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5" /> RECOMMENDED ROUTE
            </div>

            <div className="mb-4">
              <h2 className="text-xl font-bold text-white flex items-center gap-2">
                {routeRes.recommended_route.name}
              </h2>
              <p className="text-xs text-slate-400 mt-1">
                Distance: {routeRes.recommended_route.distance_km} km • Estimated Future Volume: {routeRes.recommended_route.predicted_future_volume} veh/5m
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 bg-slate-900/60 p-4 rounded-lg border border-slate-700/50">
              <div>
                <span className="text-[10px] text-slate-400 uppercase font-semibold block">Expected Travel Time</span>
                <span className="text-xl font-extrabold text-white flex items-center gap-1">
                  <Clock className="w-4 h-4 text-emerald-400" /> {routeRes.recommended_route.expected_travel_time_min} mins
                </span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase font-semibold block">Expected Delay</span>
                <span className="text-xl font-extrabold text-amber-400">
                  +{routeRes.recommended_route.expected_delay_min} mins
                </span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase font-semibold block">Predicted Congestion</span>
                <span className="text-xl font-extrabold text-emerald-400">
                  {routeRes.recommended_route.predicted_congestion}
                </span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase font-semibold block">Future Route Score</span>
                <span className="text-xl font-extrabold text-purple-300">
                  {routeRes.recommended_route.route_score}
                </span>
              </div>
            </div>
          </div>

          {routeRes.alternative_routes.length > 0 && (
            <div className="space-y-3">
              <h3 className="text-lg font-bold text-white">Alternative Routes Evaluated</h3>
              {routeRes.alternative_routes.map((alt, idx) => (
                <div key={idx} className="bg-slate-800/80 p-4 rounded-xl border border-slate-700/60 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                  <div>
                    <h4 className="font-semibold text-white flex items-center gap-2">
                      {alt.name} <span className="text-xs text-slate-400">({alt.distance_km} km)</span>
                    </h4>
                    <p className="text-xs text-slate-400 mt-1">
                      Predicted Volume: {alt.predicted_future_volume} veh/5m | Expected Delay: +{alt.expected_delay_min} mins
                    </p>
                  </div>
                  <div className="flex items-center gap-4 text-sm">
                    <span className="text-slate-300 font-semibold">{alt.expected_travel_time_min} mins</span>
                    <span className="px-2.5 py-0.5 rounded-md text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                      {alt.predicted_congestion}
                    </span>
                    <span className="text-xs text-slate-400">Score: {alt.route_score}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
