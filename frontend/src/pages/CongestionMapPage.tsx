import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const createCustomIcon = (color: string) => {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `<div style="background-color: ${color}; width: 18px; height: 18px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 10px rgba(0,0,0,0.5);"></div>`,
    iconSize: [18, 18],
    iconAnchor: [9, 9]
  });
};

export const CongestionMapPage: React.FC = () => {
  const junctions = [
    { name: 'Alankar Chowk', lat: 18.5284, lng: 73.8739, congestion: 'HIGH', volume: 142, color: '#EF4444' },
    { name: 'Jehangir Chowk', lat: 18.5312, lng: 73.8765, congestion: 'MODERATE', volume: 98, color: '#F59E0B' },
    { name: 'RTO Chowk', lat: 18.5305, lng: 73.8645, congestion: 'SEVERE', volume: 215, color: '#7C3AED' },
  ];

  const roadSegments = [
    { positions: [[18.5284, 73.8739], [18.5312, 73.8765]], color: '#EF4444', name: 'Station Road Arterial' },
    { positions: [[18.5312, 73.8765], [18.5305, 73.8645]], color: '#7C3AED', name: 'Sassoon Bridge Bypass' },
    { positions: [[18.5305, 73.8645], [18.5284, 73.8739]], color: '#F59E0B', name: 'Collectorate Road' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Interactive Congestion Map</h1>
          <p className="text-sm text-slate-400">Live & predicted spatial congestion visualization across Pune network nodes</p>
        </div>
        <div className="flex items-center gap-3 text-xs bg-slate-800 px-3 py-2 rounded-xl border border-slate-700">
          <span className="flex items-center gap-1"><span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> LOW</span>
          <span className="flex items-center gap-1"><span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span> MODERATE</span>
          <span className="flex items-center gap-1"><span className="w-2.5 h-2.5 rounded-full bg-red-500"></span> HIGH</span>
          <span className="flex items-center gap-1"><span className="w-2.5 h-2.5 rounded-full bg-purple-500"></span> SEVERE</span>
        </div>
      </div>

      <div className="bg-slate-800 p-2 rounded-xl border border-slate-700 shadow-xl h-[550px] relative">
        <MapContainer
          center={[18.5300, 73.8715]}
          zoom={14}
          scrollWheelZoom={true}
          style={{ width: '100%', height: '100%', borderRadius: '0.75rem' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {roadSegments.map((seg, idx) => (
            <Polyline
              key={idx}
              positions={seg.positions as [number, number][]}
              pathOptions={{ color: seg.color, weight: 6, opacity: 0.8 }}
            />
          ))}

          {junctions.map((j, idx) => (
            <Marker
              key={idx}
              position={[j.lat, j.lng]}
              icon={createCustomIcon(j.color)}
            >
              <Popup>
                <div className="p-1 font-sans">
                  <h3 className="font-bold text-slate-900 text-sm">{j.name}</h3>
                  <p className="text-xs text-slate-600">Congestion: <strong>{j.congestion}</strong></p>
                  <p className="text-xs text-slate-600">Predicted Vol: <strong>{j.volume} veh/5min</strong></p>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>
    </div>
  );
};
