import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import Dashboard from './components/Dashboard';

function App() {
    return (
        <BrowserRouter>
            <div className="app">
                <header className="header">
                    <div className="header-inner">
                        <div className="logo">
                            <div className="logo-icon">⚡</div>
                            <span className="logo-text">PayOrchestrate</span>
                        </div>
                        <nav>
                            <ul className="nav-links">
                                <li><NavLink to="/" className={({ isActive }) => isActive ? 'active' : ''}>Dashboard</NavLink></li>
                                <li><NavLink to="/payments" className={({ isActive }) => isActive ? 'active' : ''}>Payments</NavLink></li>
                                <li><NavLink to="/services" className={({ isActive }) => isActive ? 'active' : ''}>Services</NavLink></li>
                            </ul>
                        </nav>
                        <div className="status-badge online">
                            <span className="status-dot" />
                            System Online
                        </div>
                    </div>
                </header>

                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<Dashboard />} />
                        <Route path="/payments" element={<Dashboard />} />
                        <Route path="/services" element={<Dashboard />} />
                    </Routes>
                </main>
            </div>
        </BrowserRouter>
    );
}

export default App;
