import {
    PieChart,
    Pie,
    Cell,
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid
} from 'recharts';
import { TrendingUp, TrendingDown, Briefcase } from 'lucide-react';
import './Portfolio.css';

const holdings = [
    { symbol: 'CBA', name: 'Commonwealth Bank', units: 150, avgPrice: 125.50, currentPrice: 134.28, value: 20142.00, gain: 1317.00, gainPercent: 6.99 },
    { symbol: 'BHP', name: 'BHP Group', units: 300, avgPrice: 48.20, currentPrice: 45.67, value: 13701.00, gain: -759.00, gainPercent: -5.25 },
    { symbol: 'CSL', name: 'CSL Limited', units: 40, avgPrice: 275.00, currentPrice: 289.45, value: 11578.00, gain: 578.00, gainPercent: 5.26 },
    { symbol: 'WBC', name: 'Westpac Banking', units: 400, avgPrice: 27.50, currentPrice: 28.94, value: 11576.00, gain: 576.00, gainPercent: 5.24 },
    { symbol: 'NAB', name: 'National Australia Bank', units: 350, avgPrice: 34.00, currentPrice: 35.12, value: 12292.00, gain: 392.00, gainPercent: 3.29 },
    { symbol: 'ANZ', name: 'ANZ Group Holdings', units: 300, avgPrice: 28.50, currentPrice: 29.45, value: 8835.00, gain: 285.00, gainPercent: 3.33 },
    { symbol: 'WES', name: 'Wesfarmers', units: 100, avgPrice: 70.00, currentPrice: 73.82, value: 7382.00, gain: 382.00, gainPercent: 5.46 },
    { symbol: 'MQG', name: 'Macquarie Group', units: 50, avgPrice: 190.00, currentPrice: 198.50, value: 9925.00, gain: 425.00, gainPercent: 4.47 },
];

const sectorAllocation = [
    { name: 'Financials', value: 62771, color: '#ffcc00' },
    { name: 'Materials', value: 13701, color: '#00d26a' },
    { name: 'Healthcare', value: 11578, color: '#3b82f6' },
    { name: 'Consumer', value: 7382, color: '#8b5cf6' },
];

const monthlyReturns = [
    { month: 'Aug', return: -2.5 },
    { month: 'Sep', return: 3.2 },
    { month: 'Oct', return: -1.8 },
    { month: 'Nov', return: 4.5 },
    { month: 'Dec', return: 2.1 },
    { month: 'Jan', return: 1.9 },
];

function Portfolio() {
    const totalValue = holdings.reduce((acc, h) => acc + h.value, 0);
    const totalGain = holdings.reduce((acc, h) => acc + h.gain, 0);
    const totalGainPercent = ((totalGain / (totalValue - totalGain)) * 100).toFixed(2);

    return (
        <div className="portfolio">
            <div className="page-header">
                <h1>Portfolio</h1>
            </div>

            {/* Portfolio Summary */}
            <div className="portfolio-summary">
                <div className="summary-card main">
                    <div className="summary-icon">
                        <Briefcase size={32} />
                    </div>
                    <div className="summary-content">
                        <span className="summary-label">Total Portfolio Value</span>
                        <span className="summary-value">${totalValue.toLocaleString()}</span>
                        <span className={`summary-change ${totalGain >= 0 ? 'positive' : 'negative'}`}>
                            {totalGain >= 0 ? <TrendingUp size={16} /> : <TrendingDown size={16} />}
                            ${Math.abs(totalGain).toLocaleString()} ({totalGainPercent}%)
                        </span>
                    </div>
                </div>

                <div className="summary-card">
                    <span className="summary-label">Cash Available</span>
                    <span className="summary-value">$15,230.00</span>
                    <span className="summary-note">Ready to invest</span>
                </div>

                <div className="summary-card">
                    <span className="summary-label">Holdings</span>
                    <span className="summary-value">{holdings.length}</span>
                    <span className="summary-note">Securities</span>
                </div>

                <div className="summary-card">
                    <span className="summary-label">Best Performer</span>
                    <span className="summary-value positive">CBA +6.99%</span>
                    <span className="summary-note">Last 30 days</span>
                </div>
            </div>

            {/* Charts Section */}
            <div className="portfolio-charts">
                <div className="chart-card allocation-chart">
                    <h3>Sector Allocation</h3>
                    <div className="pie-chart-container">
                        <ResponsiveContainer width="100%" height={250}>
                            <PieChart>
                                <Pie
                                    data={sectorAllocation}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={100}
                                    paddingAngle={2}
                                    dataKey="value"
                                >
                                    {sectorAllocation.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    formatter={(value) => `$${value.toLocaleString()}`}
                                    contentStyle={{
                                        background: '#1e2433',
                                        border: '1px solid #30363d',
                                        borderRadius: '8px'
                                    }}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                        <div className="pie-legend">
                            {sectorAllocation.map((sector) => (
                                <div key={sector.name} className="legend-item">
                                    <span className="legend-color" style={{ background: sector.color }}></span>
                                    <span className="legend-name">{sector.name}</span>
                                    <span className="legend-value">${(sector.value / 1000).toFixed(1)}k</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                <div className="chart-card returns-chart">
                    <h3>Monthly Returns</h3>
                    <ResponsiveContainer width="100%" height={250}>
                        <BarChart data={monthlyReturns}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#30363d" />
                            <XAxis dataKey="month" stroke="#6e7681" />
                            <YAxis stroke="#6e7681" tickFormatter={(v) => `${v}%`} />
                            <Tooltip
                                formatter={(value) => [`${value}%`, 'Return']}
                                contentStyle={{
                                    background: '#1e2433',
                                    border: '1px solid #30363d',
                                    borderRadius: '8px'
                                }}
                            />
                            <Bar
                                dataKey="return"
                                fill="#ffcc00"
                                radius={[4, 4, 0, 0]}
                            />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Holdings Table */}
            <div className="holdings-section">
                <h3>Holdings</h3>
                <div className="holdings-table">
                    <table>
                        <thead>
                            <tr>
                                <th>Security</th>
                                <th className="numeric">Units</th>
                                <th className="numeric">Avg Price</th>
                                <th className="numeric">Current</th>
                                <th className="numeric">Value</th>
                                <th className="numeric">Gain/Loss</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {holdings.map((holding) => (
                                <tr key={holding.symbol}>
                                    <td>
                                        <div className="holding-info">
                                            <span className="holding-symbol">{holding.symbol}</span>
                                            <span className="holding-name">{holding.name}</span>
                                        </div>
                                    </td>
                                    <td className="numeric">{holding.units}</td>
                                    <td className="numeric">${holding.avgPrice.toFixed(2)}</td>
                                    <td className="numeric">${holding.currentPrice.toFixed(2)}</td>
                                    <td className="numeric">${holding.value.toLocaleString()}</td>
                                    <td className={`numeric ${holding.gain >= 0 ? 'positive' : 'negative'}`}>
                                        <div className="gain-cell">
                                            <span>${Math.abs(holding.gain).toLocaleString()}</span>
                                            <span className="gain-percent">
                                                {holding.gain >= 0 ? '+' : '-'}{Math.abs(holding.gainPercent).toFixed(2)}%
                                            </span>
                                        </div>
                                    </td>
                                    <td>
                                        <button className="action-btn">Trade</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

export default Portfolio;
