import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Sidebar } from './components/Sidebar';
import { OverviewPage } from './pages/OverviewPage';
import { LiveTrafficPage } from './pages/LiveTrafficPage';
import { ForecastPage } from './pages/ForecastPage';
import { CongestionMapPage } from './pages/CongestionMapPage';
import { RouteRecommendationPage } from './pages/RouteRecommendationPage';
import { ModelComparisonPage } from './pages/ModelComparisonPage';
import { AnalyticsPage, SystemInfoPage } from './pages/AnalyticsPages';

export const App: React.FC = () => {
  const [isDemoMode, setIsDemoMode] = useState(true);

  return (
    <Router>
      <div className="flex min-h-screen bg-slate-900 text-slate-100 font-sans">
        <Sidebar isDemoMode={isDemoMode} setIsDemoMode={setIsDemoMode} />
        <main className="flex-1 p-8 overflow-y-auto max-w-7xl">
          <Routes>
            <Route path="/" element={<OverviewPage />} />
            <Route path="/live" element={<LiveTrafficPage />} />
            <Route path="/forecast" element={<ForecastPage />} />
            <Route path="/map" element={<CongestionMapPage />} />
            <Route path="/routes" element={<RouteRecommendationPage />} />
            <Route path="/models" element={<ModelComparisonPage />} />
            <Route path="/analytics" element={<AnalyticsPage />} />
            <Route path="/system" element={<SystemInfoPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
};

export default App;
