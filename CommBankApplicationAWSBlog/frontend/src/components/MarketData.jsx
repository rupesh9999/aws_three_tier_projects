import { useState, useEffect } from 'react';
import {
    TrendingUp,
    TrendingDown,
    ArrowUpRight,
    ArrowDownRight,
    Activity,
    Clock,
    RefreshCw
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import './MarketData.css';

// Mock real-time data generator
const generateMockPrice = (basePrice) => {
    const change = (Math.random() - 0.5) * 0.5;
    return (basePrice + change).toFixed(2);
};

const indices = [
    { name: 'S&P/ASX 200', value: 8234.50, change: 1.24, trend: [8150, 8180, 8165, 8200, 8215, 8234] },
    { name: 'All Ordinaries', value: 8489.20, change: 0.98, trend: [8420, 8445, 8430, 8465, 8480, 8489] },
    { name: 'S&P 500', value: 5987.45, change: 0.52, trend: [5960, 5970, 5965, 5980, 5985, 5987] },
    { name: 'NASDAQ', value: 19890.30, change: 0.78, trend: [19800, 19840, 19820, 19870, 19880, 19890] },
];

const marketMovers = {
    gainers: [
        { symbol: 'LYC', name: 'Lynas Rare Earths', price: 7.85, change: 8.45 },
        { symbol: 'MIN', name: 'Mineral Resources', price: 45.20, change: 6.23 },
        { symbol: 'PLS', name: 'Pilbara Minerals', price: 3.42, change: 5.56 },
        { symbol: 'LTR', name: 'Liontown Resources', price: 1.28, change: 4.92 },
        { symbol: 'IGO', name: 'IGO Limited', price: 5.67, change: 4.41 },
    ],
    losers: [
        { symbol: 'ZIP', name: 'Zip Co', price: 2.15, change: -7.33 },
        { symbol: 'SQ2', name: 'Block Inc', price: 98.50, change: -5.21 },
        { symbol: 'CXO', name: 'Core Lithium', price: 0.45, change: -4.26 },
        { symbol: 'APX', name: 'Appen Limited', price: 1.82, change: -3.70 },
        { symbol: 'TYR', name: 'Tyro Payments', price: 1.45, change: -3.33 },
    ],
};

const sectors = [
    { name: 'Financials', change: 1.45, volume: '2.4B' },
    { name: 'Materials', change: 2.15, volume: '1.8B' },
    { name: 'Healthcare', change: -0.32, volume: '890M' },
    { name: 'Energy', change: 1.82, volume: '1.2B' },
    { name: 'Consumer Discretionary', change: 0.65, volume: '650M' },
    { name: 'Information Technology', change: -0.85, volume: '980M' },
    { name: 'Industrials', change: 0.92, volume: '540M' },
    { name: 'Utilities', change: 0.28, volume: '320M' },
];

function MarketData() {
    const [lastUpdate, setLastUpdate] = useState(new Date());
    const [isRefreshing, setIsRefreshing] = useState(false);

    const handleRefresh = () => {
        setIsRefreshing(true);
        setTimeout(() => {
            setLastUpdate(new Date());
            setIsRefreshing(false);
        }, 1000);
    };

    // Simulate real-time updates
    useEffect(() => {
        const interval = setInterval(() => {
            setLastUpdate(new Date());
        }, 30000); // Update every 30 seconds
        return () => clearInterval(interval);
    }, []);

    return (
        <div className="market-data">
            <div className="page-header">
                <div className="header-left">
                    <h1>Market Data</h1>
                    <span className="market-status open">
                        <Activity size={14} />
                        Market Open
                    </span>
                </div>
                <div className="header-right">
                    <span className="last-update">
                        <Clock size={14} />
                        Last update: {lastUpdate.toLocaleTimeString()}
                    </span>
                    <button
                        className={`refresh-btn ${isRefreshing ? 'spinning' : ''}`}
                        onClick={handleRefresh}
                    >
                        <RefreshCw size={16} />
                    </button>
                </div>
            </div>

            {/* Indices Cards */}
            <div className="indices-grid">
                {indices.map((index) => (
                    <div key={index.name} className="index-card">
                        <div className="index-header">
                            <span className="index-name">{index.name}</span>
                            <span className={`index-change ${index.change >= 0 ? 'positive' : 'negative'}`}>
                                {index.change >= 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
                                {Math.abs(index.change).toFixed(2)}%
                            </span>
                        </div>
                        <span className="index-value">{index.value.toLocaleString()}</span>
                        <div className="index-chart">
                            <ResponsiveContainer width="100%" height={50}>
                                <LineChart data={index.trend.map((v, i) => ({ value: v }))}>
                                    <Line
                                        type="monotone"
                                        dataKey="value"
                                        stroke={index.change >= 0 ? '#00d26a' : '#ff4d4d'}
                                        strokeWidth={2}
                                        dot={false}
                                    />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                ))}
            </div>

            {/* Market Movers */}
            <div className="movers-section">
                <div className="movers-card gainers">
                    <div className="movers-header">
                        <TrendingUp className="movers-icon positive" size={20} />
                        <h3>Top Gainers</h3>
                    </div>
                    <div className="movers-list">
                        {marketMovers.gainers.map((stock) => (
                            <div key={stock.symbol} className="mover-item">
                                <div className="mover-info">
                                    <span className="mover-symbol">{stock.symbol}</span>
                                    <span className="mover-name">{stock.name}</span>
                                </div>
                                <div className="mover-data">
                                    <span className="mover-price">${stock.price.toFixed(2)}</span>
                                    <span className="mover-change positive">+{stock.change.toFixed(2)}%</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="movers-card losers">
                    <div className="movers-header">
                        <TrendingDown className="movers-icon negative" size={20} />
                        <h3>Top Losers</h3>
                    </div>
                    <div className="movers-list">
                        {marketMovers.losers.map((stock) => (
                            <div key={stock.symbol} className="mover-item">
                                <div className="mover-info">
                                    <span className="mover-symbol">{stock.symbol}</span>
                                    <span className="mover-name">{stock.name}</span>
                                </div>
                                <div className="mover-data">
                                    <span className="mover-price">${stock.price.toFixed(2)}</span>
                                    <span className="mover-change negative">{stock.change.toFixed(2)}%</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Sector Performance */}
            <div className="sectors-section">
                <h3>Sector Performance</h3>
                <div className="sectors-grid">
                    {sectors.map((sector) => (
                        <div key={sector.name} className="sector-card">
                            <span className="sector-name">{sector.name}</span>
                            <div className="sector-data">
                                <span className={`sector-change ${sector.change >= 0 ? 'positive' : 'negative'}`}>
                                    {sector.change >= 0 ? '+' : ''}{sector.change.toFixed(2)}%
                                </span>
                                <span className="sector-volume">Vol: {sector.volume}</span>
                            </div>
                            <div className="sector-bar">
                                <div
                                    className={`sector-bar-fill ${sector.change >= 0 ? 'positive' : 'negative'}`}
                                    style={{ width: `${Math.min(Math.abs(sector.change) * 20, 100)}%` }}
                                />
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default MarketData;
