import { useState, useEffect, useCallback } from 'react';
import { paymentApi } from '../services/api';

const SERVICES = [
    { name: 'Payment Initiation', port: 8081, key: 'initiation' },
    { name: 'Payment Execution', port: 8082, key: 'execution' },
    { name: 'Payment Tracking', port: 8083, key: 'tracking' },
    { name: 'Payment Reconciliation', port: 8084, key: 'reconciliation' },
    { name: 'Payment Billing', port: 8085, key: 'billing' },
    { name: 'Payment Risk', port: 8086, key: 'risk' },
];

const PAYMENT_TYPES = ['REAL_TIME', 'DIGITAL_DISBURSEMENT', 'CREDIT_TRANSFER', 'PEER_TO_PEER', 'AUTOMATED_CLEARING', 'CROSS_BORDER', 'WIRE_TRANSFER'];
const CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CAD', 'AUD'];

function Dashboard() {
    const [payments, setPayments] = useState([]);
    const [stats, setStats] = useState({ total: 0, successful: 0, pending: 0, failed: 0 });
    const [serviceHealth, setServiceHealth] = useState({});
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        tenantId: 'TENANT-001',
        debtorAccount: 'ACC-1234567890',
        creditorAccount: 'ACC-0987654321',
        debtorName: 'John Doe',
        creditorName: 'Jane Smith',
        amount: '1250.00',
        currency: 'USD',
        paymentType: 'REAL_TIME',
    });

    const loadPayments = useCallback(async () => {
        try {
            const res = await paymentApi.getPaymentsByTenant(formData.tenantId);
            const data = res.data.content || [];
            setPayments(data);
            setStats({
                total: data.length,
                successful: data.filter(p => ['EXECUTED', 'SETTLED', 'COMPLETED', 'RECONCILED', 'BILLED'].includes(p.status)).length,
                pending: data.filter(p => ['INITIATED', 'RISK_SCREENING', 'RISK_APPROVED', 'EXECUTING'].includes(p.status)).length,
                failed: data.filter(p => ['FAILED', 'RISK_REJECTED', 'REVERSED'].includes(p.status)).length,
            });
        } catch {
            // API not available
        }
    }, [formData.tenantId]);

    const checkServiceHealth = useCallback(async () => {
        const healthMap = {};
        for (const svc of SERVICES) {
            try {
                const res = await fetch(`/api/v1/payments/health`, { signal: AbortSignal.timeout(3000) });
                healthMap[svc.key] = res.ok;
            } catch {
                healthMap[svc.key] = false;
            }
        }
        setServiceHealth(healthMap);
    }, []);

    useEffect(() => {
        loadPayments();
        checkServiceHealth();
        const interval = setInterval(() => { loadPayments(); checkServiceHealth(); }, 15000);
        return () => clearInterval(interval);
    }, [loadPayments, checkServiceHealth]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await paymentApi.initiatePayment({
                ...formData,
                amount: parseFloat(formData.amount),
                idempotencyKey: crypto.randomUUID(),
            });
            await loadPayments();
        } catch (err) {
            console.error('Payment initiation failed:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
    };

    return (
        <>
            {/* Stats */}
            <div className="stats-grid">
                <div className="card">
                    <span className="card-title">Total Payments</span>
                    <div className="stat-value gradient-blue">{stats.total.toLocaleString()}</div>
                    <div className="stat-trend up">▲ Live Tracking</div>
                </div>
                <div className="card">
                    <span className="card-title">Successful</span>
                    <div className="stat-value gradient-green">{stats.successful.toLocaleString()}</div>
                    <div className="stat-trend up">▲ {stats.total > 0 ? ((stats.successful / stats.total) * 100).toFixed(1) : 0}% rate</div>
                </div>
                <div className="card">
                    <span className="card-title">Pending</span>
                    <div className="stat-value gradient-amber">{stats.pending.toLocaleString()}</div>
                    <div className="stat-trend">◆ In Processing</div>
                </div>
                <div className="card">
                    <span className="card-title">Failed / Rejected</span>
                    <div className="stat-value gradient-red">{stats.failed.toLocaleString()}</div>
                    <div className="stat-trend down">▼ Needs Review</div>
                </div>
            </div>

            <div className="dashboard-grid">
                {/* Recent Payments Table */}
                <div className="card">
                    <div className="card-header">
                        <span className="card-title">Recent Payments</span>
                        <button className="btn btn-primary" onClick={loadPayments} style={{ padding: '6px 12px', fontSize: '0.75rem' }}>
                            ↻ Refresh
                        </button>
                    </div>
                    <div className="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Transaction ID</th>
                                    <th>Amount</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>Created</th>
                                </tr>
                            </thead>
                            <tbody>
                                {payments.length === 0 ? (
                                    <tr><td colSpan="5" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                                        No payments yet — submit one below
                                    </td></tr>
                                ) : payments.slice(0, 10).map(p => (
                                    <tr key={p.transactionId}>
                                        <td style={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>
                                            {p.transactionId?.substring(0, 8)}...
                                        </td>
                                        <td style={{ fontWeight: 600 }}>
                                            {p.currency} {parseFloat(p.amount).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                                        </td>
                                        <td>{p.paymentType?.replace('_', ' ')}</td>
                                        <td><span className={`status-pill ${p.status?.toLowerCase()}`}>{p.status}</span></td>
                                        <td style={{ fontSize: '0.75rem' }}>{p.createdAt ? new Date(p.createdAt).toLocaleString() : '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Service Health */}
                <div className="card">
                    <div className="card-header">
                        <span className="card-title">Service Health</span>
                    </div>
                    <div className="service-list">
                        {SERVICES.map(svc => (
                            <div className="service-item" key={svc.key}>
                                <div>
                                    <div className="service-name">{svc.name}</div>
                                    <div className="service-port">:{svc.port}</div>
                                </div>
                                <div className={`service-status ${serviceHealth[svc.key] ? 'healthy' : 'unhealthy'}`} />
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Payment Form */}
            <div className="card">
                <div className="card-header">
                    <span className="card-title">Initiate New Payment</span>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <div className="form-group">
                            <label className="form-label">Tenant ID</label>
                            <input className="form-input" name="tenantId" value={formData.tenantId} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Payment Type</label>
                            <select className="form-select" name="paymentType" value={formData.paymentType} onChange={handleChange}>
                                {PAYMENT_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Debtor Name</label>
                            <input className="form-input" name="debtorName" value={formData.debtorName} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Creditor Name</label>
                            <input className="form-input" name="creditorName" value={formData.creditorName} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Debtor Account</label>
                            <input className="form-input" name="debtorAccount" value={formData.debtorAccount} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Creditor Account</label>
                            <input className="form-input" name="creditorAccount" value={formData.creditorAccount} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Amount</label>
                            <input className="form-input" type="number" step="0.01" name="amount" value={formData.amount} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Currency</label>
                            <select className="form-select" name="currency" value={formData.currency} onChange={handleChange}>
                                {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
                            </select>
                        </div>
                        <div className="form-group full-width" style={{ marginTop: '0.5rem' }}>
                            <button type="submit" className="btn btn-primary" disabled={loading}>
                                {loading ? '⏳ Processing...' : '💳 Submit Payment'}
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </>
    );
}

export default Dashboard;
