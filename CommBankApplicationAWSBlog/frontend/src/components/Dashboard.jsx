import { useState, useEffect } from 'react';
import {
    TrendingUp,
    TrendingDown,
    DollarSign,
    Activity,
    ArrowUpRight,
    ArrowDownRight
} from 'lucide-react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    AreaChart,
    Area
} from 'recharts';
import './Dashboard.css';

// Mock data for charts
const portfolioHistory = [
    { date: 'Jan', value: 110000 },
    { date: 'Feb', value: 115200 },
    { date: 'Mar', value: 108500 },
    { date: 'Apr', value: 118900 },
    { date: 'May', value: 122400 },
    { date: 'Jun', value: 127450 },
];

const marketData = [
    { time: '09:00', asx: 8150, volume: 2400 },
    { time: '10:00', asx: 8180, volume: 3200 },
    { time: '11:00', asx: 8165, volume: 2800 },
    { time: '12:00', asx: 8200, volume: 4100 },
    { time: '13:00', asx: 8215, volume: 3600 },
    { time: '14:00', asx: 8234, volume: 3900 },
];

const watchlist = [
    { symbol: 'CBA', name: 'Commonwealth Bank', price: 134.28, change: 2.15, changePercent: 1.63 },
    { symbol: 'BHP', name: 'BHP Group', price: 45.67, change: -0.89, changePercent: -1.91 },
    { symbol: 'CSL', name: 'CSL Limited', price: 289.45, change: 4.32, changePercent: 1.51 },
    { symbol: 'WBC', name: 'Westpac Banking', price: 28.94, change: 0.45, changePercent: 1.58 },
    { symbol: 'NAB', name: 'National Australia Bank', price: 35.12, change: -0.23, changePercent: -0.65 },
];

const recentTrades = [
    { id: 1, symbol: 'CBA', type: 'BUY', quantity: 50, price: 132.50, time: '14:23:45' },
    { id: 2, symbol: 'BHP', type: 'SELL', quantity: 100, price: 46.20, time: '13:45:12' },
    { id: 3, symbol: 'CSL', type: 'BUY', quantity: 20, price: 285.00, time: '11:30:08' },
];

function Dashboard() {
    const [selectedPeriod, setSelectedPeriod] = useState('1M');

    const stats = [
        {
            label: 'Portfolio Value',
            value: '$127,450.00',
            change: '+$2,340.00',
            changePercent: '+1.87%',
            icon: DollarSign,
            positive: true,
        },
        {
            label: "Today's P&L",
            value: '+$892.50',
            change: '+0.71%',
            changePercent: '',
            icon: TrendingUp,
            positive: true,
        },
        {
            label: 'Open Orders',
            value: '3',
            change: '2 pending',
            changePercent: '',
            icon: Activity,
            positive: true,
        },
        {
            label: 'Cash Available',
            value: '$15,230.00',
            change: 'Ready to invest',
            changePercent: '',
            icon: DollarSign,
            positive: true,
        },
    ];

    return (
        <div className="dashboard">
            <div className="page-header">
                <h1>Dashboard</h1>
                <div className="period-selector">
                    {['1D', '1W', '1M', '3M', '1Y', 'ALL'].map((period) => (
                        <button
                            key={period}
                            className={`period-btn ${selectedPeriod === period ? 'active' : ''}`}
                            onClick={() => setSelectedPeriod(period)}
                        >
                            {period}
                        </button>
                    ))}
                </div>
            </div>

            {/* Stats Cards */}
            <div className="stats-grid">
                {stats.map((stat, index) => (
                    <div key={index} className="stat-card">
                        <div className="stat-icon">
                            <stat.icon size={24} />
                        </div>
                        <div className="stat-content">
                            <span className="stat-label">{stat.label}</span>
                            <span className="stat-value">{stat.value}</span>
                            <span className={`stat-change ${stat.positive ? 'positive' : 'negative'}`}>
                                {stat.change} {stat.changePercent}
                            </span>
                        </div>
                    </div>
                ))}
            </div>

            {/* Charts Section */}
            <div className="charts-section">
                <div className="chart-card portfolio-chart">
                    <div className="chart-header">
                        <h3>Portfolio Performance</h3>
                    </div>
                    <div className="chart-body">
                        <ResponsiveContainer width="100%" height={300}>
                            <AreaChart data={portfolioHistory}>
                                <defs>
                                    <linearGradient id="portfolioGradient" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#ffcc00" stopOpacity={0.3} />
                                        <stop offset="95%" stopColor="#ffcc00" stopOpacity={0} />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#30363d" />
                                <XAxis dataKey="date" stroke="#6e7681" />
                                <YAxis stroke="#6e7681" tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} />
                                <Tooltip
                                    contentStyle={{
                                        background: '#1e2433',
                                        border: '1px solid #30363d',
                                        borderRadius: '8px'
                                    }}
                                    formatter={(value) => [`$${value.toLocaleString()}`, 'Value']}
                                />
                                <Area
                                    type="monotone"
                                    dataKey="value"
                                    stroke="#ffcc00"
                                    strokeWidth={2}
                                    fill="url(#portfolioGradient)"
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="chart-card market-chart">
                    <div className="chart-header">
                        <h3>ASX 200 Today</h3>
                        <span className="chart-value positive">8,234.50 (+1.24%)</span>
                    </div>
                    <div className="chart-body">
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={marketData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#30363d" />
                                <XAxis dataKey="time" stroke="#6e7681" />
                                <YAxis stroke="#6e7681" domain={['dataMin - 50', 'dataMax + 50']} />
                                <Tooltip
                                    contentStyle={{
                                        background: '#1e2433',
                                        border: '1px solid #30363d',
                                        borderRadius: '8px'
                                    }}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="asx"
                                    stroke="#00d26a"
                                    strokeWidth={2}
                                    dot={false}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* Watchlist and Recent Trades */}
            <div className="bottom-section">
                <div className="watchlist-card">
                    <div className="card-header">
                        <h3>Watchlist</h3>
                        <button className="text-btn">View All</button>
                    </div>
                    <div className="watchlist-table">
                        <table>
                            <thead>
                                <tr>
                                    <th>Symbol</th>
                                    <th>Price</th>
                                    <th>Change</th>
                                </tr>
                            </thead>
                            <tbody>
                                {watchlist.map((stock) => (
                                    <tr key={stock.symbol}>
                                        <td>
                                            <div className="stock-info">
                                                <span className="stock-symbol">{stock.symbol}</span>
                                                <span className="stock-name">{stock.name}</span>
                                            </div>
                                        </td>
                                        <td className="stock-price">${stock.price.toFixed(2)}</td>
                                        <td className={`stock-change ${stock.change >= 0 ? 'positive' : 'negative'}`}>
                                            {stock.change >= 0 ? <ArrowUpRight size={16} /> : <ArrowDownRight size={16} />}
                                            {stock.changePercent.toFixed(2)}%
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div className="trades-card">
                    <div className="card-header">
                        <h3>Recent Trades</h3>
                        <button className="text-btn">View All</button>
                    </div>
                    <div className="trades-list">
                        {recentTrades.map((trade) => (
                            <div key={trade.id} className="trade-item">
                                <div className="trade-info">
                                    <span className={`trade-type ${trade.type.toLowerCase()}`}>{trade.type}</span>
                                    <span className="trade-symbol">{trade.symbol}</span>
                                </div>
                                <div className="trade-details">
                                    <span className="trade-qty">{trade.quantity} shares</span>
                                    <span className="trade-price">@ ${trade.price.toFixed(2)}</span>
                                </div>
                                <span className="trade-time">{trade.time}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Dashboard;
