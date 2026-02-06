import { useState } from 'react';
import {
    Search,
    ArrowUpRight,
    ArrowDownRight,
    Clock,
    CheckCircle,
    XCircle,
    AlertCircle
} from 'lucide-react';
import './Trading.css';

const orderTypes = ['Market', 'Limit', 'Stop', 'Stop Limit'];

const openOrders = [
    { id: 1, symbol: 'CBA', type: 'LIMIT BUY', quantity: 100, price: 130.00, status: 'pending', time: '09:45:00' },
    { id: 2, symbol: 'WBC', type: 'LIMIT SELL', quantity: 200, price: 30.00, status: 'pending', time: '10:15:00' },
    { id: 3, symbol: 'BHP', type: 'STOP', quantity: 50, price: 44.00, status: 'triggered', time: '11:30:00' },
];

const orderHistory = [
    { id: 101, symbol: 'CSL', type: 'MARKET BUY', quantity: 20, price: 285.00, status: 'filled', time: '11:30:08' },
    { id: 102, symbol: 'BHP', type: 'MARKET SELL', quantity: 100, price: 46.20, status: 'filled', time: '13:45:12' },
    { id: 103, symbol: 'CBA', type: 'LIMIT BUY', quantity: 50, price: 132.50, status: 'filled', time: '14:23:45' },
    { id: 104, symbol: 'ANZ', type: 'LIMIT BUY', quantity: 150, price: 28.00, status: 'cancelled', time: '09:20:00' },
];

const stockSearch = [
    { symbol: 'CBA', name: 'Commonwealth Bank of Australia', price: 134.28, change: 1.63 },
    { symbol: 'BHP', name: 'BHP Group Ltd', price: 45.67, change: -1.91 },
    { symbol: 'CSL', name: 'CSL Limited', price: 289.45, change: 1.51 },
    { symbol: 'WBC', name: 'Westpac Banking Corporation', price: 28.94, change: 1.58 },
    { symbol: 'NAB', name: 'National Australia Bank', price: 35.12, change: -0.65 },
    { symbol: 'ANZ', name: 'ANZ Group Holdings Ltd', price: 29.45, change: 0.85 },
    { symbol: 'WES', name: 'Wesfarmers Ltd', price: 73.82, change: 2.15 },
    { symbol: 'MQG', name: 'Macquarie Group Ltd', price: 198.50, change: -0.42 },
];

function Trading() {
    const [orderSide, setOrderSide] = useState('buy');
    const [orderType, setOrderType] = useState('Market');
    const [selectedStock, setSelectedStock] = useState(stockSearch[0]);
    const [quantity, setQuantity] = useState('');
    const [limitPrice, setLimitPrice] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [activeTab, setActiveTab] = useState('open');

    const filteredStocks = stockSearch.filter(
        stock =>
            stock.symbol.toLowerCase().includes(searchTerm.toLowerCase()) ||
            stock.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleSubmit = (e) => {
        e.preventDefault();
        alert(`Order submitted: ${orderSide.toUpperCase()} ${quantity} ${selectedStock.symbol} @ ${orderType === 'Market' ? 'Market' : '$' + limitPrice}`);
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'filled': return <CheckCircle size={16} className="status-icon filled" />;
            case 'cancelled': return <XCircle size={16} className="status-icon cancelled" />;
            case 'triggered': return <AlertCircle size={16} className="status-icon triggered" />;
            default: return <Clock size={16} className="status-icon pending" />;
        }
    };

    return (
        <div className="trading">
            <div className="page-header">
                <h1>Trading</h1>
            </div>

            <div className="trading-layout">
                {/* Stock Search & Selection */}
                <div className="stock-panel">
                    <div className="search-box">
                        <Search size={20} />
                        <input
                            type="text"
                            placeholder="Search stocks..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>

                    <div className="stock-list">
                        {filteredStocks.map((stock) => (
                            <div
                                key={stock.symbol}
                                className={`stock-item ${selectedStock.symbol === stock.symbol ? 'selected' : ''}`}
                                onClick={() => setSelectedStock(stock)}
                            >
                                <div className="stock-main">
                                    <span className="symbol">{stock.symbol}</span>
                                    <span className="name">{stock.name}</span>
                                </div>
                                <div className="stock-price-info">
                                    <span className="price">${stock.price.toFixed(2)}</span>
                                    <span className={`change ${stock.change >= 0 ? 'positive' : 'negative'}`}>
                                        {stock.change >= 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
                                        {Math.abs(stock.change).toFixed(2)}%
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Order Entry */}
                <div className="order-panel">
                    <div className="order-card">
                        <div className="order-header">
                            <div className="selected-stock">
                                <span className="symbol">{selectedStock.symbol}</span>
                                <span className="price">${selectedStock.price.toFixed(2)}</span>
                                <span className={`change ${selectedStock.change >= 0 ? 'positive' : 'negative'}`}>
                                    {selectedStock.change >= 0 ? '+' : ''}{selectedStock.change.toFixed(2)}%
                                </span>
                            </div>
                        </div>

                        <form onSubmit={handleSubmit} className="order-form">
                            {/* Buy/Sell Toggle */}
                            <div className="side-toggle">
                                <button
                                    type="button"
                                    className={`side-btn buy ${orderSide === 'buy' ? 'active' : ''}`}
                                    onClick={() => setOrderSide('buy')}
                                >
                                    Buy
                                </button>
                                <button
                                    type="button"
                                    className={`side-btn sell ${orderSide === 'sell' ? 'active' : ''}`}
                                    onClick={() => setOrderSide('sell')}
                                >
                                    Sell
                                </button>
                            </div>

                            {/* Order Type */}
                            <div className="form-group">
                                <label>Order Type</label>
                                <div className="order-type-grid">
                                    {orderTypes.map((type) => (
                                        <button
                                            key={type}
                                            type="button"
                                            className={`order-type-btn ${orderType === type ? 'active' : ''}`}
                                            onClick={() => setOrderType(type)}
                                        >
                                            {type}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            {/* Quantity */}
                            <div className="form-group">
                                <label>Quantity</label>
                                <input
                                    type="number"
                                    min="1"
                                    placeholder="Enter quantity"
                                    value={quantity}
                                    onChange={(e) => setQuantity(e.target.value)}
                                    required
                                />
                            </div>

                            {/* Limit Price */}
                            {orderType !== 'Market' && (
                                <div className="form-group">
                                    <label>{orderType.includes('Stop') ? 'Stop Price' : 'Limit Price'}</label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        placeholder="Enter price"
                                        value={limitPrice}
                                        onChange={(e) => setLimitPrice(e.target.value)}
                                        required
                                    />
                                </div>
                            )}

                            {/* Order Summary */}
                            <div className="order-summary">
                                <div className="summary-row">
                                    <span>Estimated Total</span>
                                    <span className="total">
                                        ${(parseFloat(quantity || 0) * (orderType === 'Market' ? selectedStock.price : parseFloat(limitPrice || 0))).toFixed(2)}
                                    </span>
                                </div>
                                <div className="summary-row">
                                    <span>Brokerage</span>
                                    <span>$9.95</span>
                                </div>
                            </div>

                            {/* Submit Button */}
                            <button
                                type="submit"
                                className={`submit-btn ${orderSide}`}
                                disabled={!quantity}
                            >
                                {orderSide === 'buy' ? 'Buy' : 'Sell'} {selectedStock.symbol}
                            </button>
                        </form>
                    </div>
                </div>

                {/* Orders Panel */}
                <div className="orders-panel">
                    <div className="orders-tabs">
                        <button
                            className={`tab ${activeTab === 'open' ? 'active' : ''}`}
                            onClick={() => setActiveTab('open')}
                        >
                            Open Orders ({openOrders.length})
                        </button>
                        <button
                            className={`tab ${activeTab === 'history' ? 'active' : ''}`}
                            onClick={() => setActiveTab('history')}
                        >
                            Order History
                        </button>
                    </div>

                    <div className="orders-list">
                        {(activeTab === 'open' ? openOrders : orderHistory).map((order) => (
                            <div key={order.id} className="order-item">
                                <div className="order-main">
                                    {getStatusIcon(order.status)}
                                    <div className="order-info">
                                        <span className="order-symbol">{order.symbol}</span>
                                        <span className="order-type">{order.type}</span>
                                    </div>
                                </div>
                                <div className="order-details">
                                    <span className="order-qty">{order.quantity} @ ${order.price.toFixed(2)}</span>
                                    <span className="order-time">{order.time}</span>
                                </div>
                                {activeTab === 'open' && order.status === 'pending' && (
                                    <button className="cancel-btn">Cancel</button>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Trading;
