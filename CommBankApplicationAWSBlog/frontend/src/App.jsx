import { Routes, Route, Link, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import {
    TrendingUp,
    Briefcase,
    BarChart3,
    Bell,
    Menu,
    X,
    User
} from 'lucide-react';
import Dashboard from './components/Dashboard';
import Portfolio from './components/Portfolio';
import MarketData from './components/MarketData';
import Trading from './components/Trading';
import './styles/App.css';

function App() {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [notifications, setNotifications] = useState(3);
    const location = useLocation();

    const navItems = [
        { path: '/', icon: BarChart3, label: 'Dashboard' },
        { path: '/trading', icon: TrendingUp, label: 'Trading' },
        { path: '/portfolio', icon: Briefcase, label: 'Portfolio' },
        { path: '/market', icon: BarChart3, label: 'Market Data' },
    ];

    useEffect(() => {
        setSidebarOpen(false);
    }, [location]);

    return (
        <div className="app">
            {/* Header */}
            <header className="header">
                <div className="header-left">
                    <button
                        className="menu-button"
                        onClick={() => setSidebarOpen(!sidebarOpen)}
                        aria-label="Toggle menu"
                    >
                        {sidebarOpen ? <X size={24} /> : <Menu size={24} />}
                    </button>
                    <div className="logo">
                        <TrendingUp className="logo-icon" />
                        <span className="logo-text">CommSec</span>
                    </div>
                </div>

                <div className="header-center">
                    <div className="market-summary">
                        <span className="market-item">
                            <span className="market-label">ASX 200</span>
                            <span className="market-value positive">8,234.50</span>
                            <span className="market-change positive">+1.24%</span>
                        </span>
                        <span className="market-item">
                            <span className="market-label">ALL ORDS</span>
                            <span className="market-value positive">8,489.20</span>
                            <span className="market-change positive">+0.98%</span>
                        </span>
                    </div>
                </div>

                <div className="header-right">
                    <button className="notification-button">
                        <Bell size={20} />
                        {notifications > 0 && (
                            <span className="notification-badge">{notifications}</span>
                        )}
                    </button>
                    <div className="user-menu">
                        <User size={20} />
                        <span>John Doe</span>
                    </div>
                </div>
            </header>

            <div className="main-container">
                {/* Sidebar */}
                <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
                    <nav className="nav">
                        {navItems.map(({ path, icon: Icon, label }) => (
                            <Link
                                key={path}
                                to={path}
                                className={`nav-item ${location.pathname === path ? 'active' : ''}`}
                            >
                                <Icon size={20} />
                                <span>{label}</span>
                            </Link>
                        ))}
                    </nav>

                    <div className="sidebar-footer">
                        <div className="account-summary">
                            <div className="account-label">Portfolio Value</div>
                            <div className="account-value">$127,450.00</div>
                            <div className="account-change positive">+$2,340.00 (1.87%)</div>
                        </div>
                    </div>
                </aside>

                {/* Main Content */}
                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<Dashboard />} />
                        <Route path="/trading" element={<Trading />} />
                        <Route path="/portfolio" element={<Portfolio />} />
                        <Route path="/market" element={<MarketData />} />
                    </Routes>
                </main>
            </div>

            {/* Overlay for mobile */}
            {sidebarOpen && (
                <div
                    className="sidebar-overlay"
                    onClick={() => setSidebarOpen(false)}
                />
            )}
        </div>
    );
}

export default App;
