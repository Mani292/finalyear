import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Activity,
  LineChart,
  MapPin,
  Navigation,
  GitCompare,
  BarChart2,
  Info,
  Layers,
  CheckCircle2
} from 'lucide-react';

interface SidebarProps {
  isDemoMode: boolean;
  setIsDemoMode: (val: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isDemoMode, setIsDemoMode }) => {
  const navItems = [
    { to: '/', label: 'Overview', icon: LayoutDashboard },
    { to: '/live', label: 'Live Traffic', icon: Activity },
    { to: '/forecast', label: 'Forecast', icon: LineChart },
    { to: '/map', label: 'Congestion Map', icon: MapPin },
    { to: '/routes', label: 'Future Routing', icon: Navigation },
    { to: '/models', label: 'Model Benchmarks', icon: GitCompare },
    { to: '/analytics', label: 'Analytics', icon: BarChart2 },
    { to: '/system', label: 'System Info', icon: Info },
  ];

  return (
    <aside className="w-64 bg-slate-800 border-r border-slate-700 flex flex-col justify-between min-h-screen p-4">
      <div>
        <div className="flex items-center gap-3 px-2 py-3 mb-6">
          <div className="p-2 bg-emerald-500/20 text-emerald-400 rounded-lg border border-emerald-500/30">
            <Layers className="w-6 h-6" />
          </div>
          <div>
            <h1 className="font-bold text-lg text-white leading-tight">TrafficSense AI</h1>
            <p className="text-xs text-slate-400">Intelligent ITS Forecaster</p>
          </div>
        </div>

        <nav className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-600 text-white shadow-md shadow-emerald-900/30'
                      : 'text-slate-300 hover:bg-slate-700/60 hover:text-white'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                {item.label}
              </NavLink>
            );
          })}
        </nav>
      </div>

      <div className="space-y-4 pt-4 border-t border-slate-700">
        <div className="bg-slate-900/60 p-3 rounded-xl border border-slate-700/50">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-slate-300">DEMO MODE</span>
            <button
              onClick={() => setIsDemoMode(!isDemoMode)}
              className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                isDemoMode ? 'bg-emerald-500' : 'bg-slate-600'
              }`}
            >
              <span
                className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white transition-transform ${
                  isDemoMode ? 'translate-x-4.5' : 'translate-x-1'
                }`}
              />
            </button>
          </div>
          <p className="text-[11px] text-slate-400 leading-tight">
            {isDemoMode
              ? 'Historical Dataset / ML Demonstration Mode Active'
              : 'Real-time API Stream Active'}
          </p>
        </div>

        <div className="flex items-center gap-2 px-2 text-xs text-slate-400">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>Hybrid Model Ready (R²: 0.88)</span>
        </div>
      </div>
    </aside>
  );
};
