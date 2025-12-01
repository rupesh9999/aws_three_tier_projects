import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
  Divider,
  useTheme,
  LinearProgress,
  Avatar,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  AccountBalance as FDIcon,
  Savings as RDIcon,
  ShowChart as MFIcon,
  CurrencyExchange as SIPIcon,
  Add as AddIcon,
  PieChart,
  Timeline,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

interface Investment {
  id: string;
  type: 'fd' | 'rd' | 'mf' | 'sip';
  name: string;
  investedAmount: number;
  currentValue: number;
  returns: number;
  returnPercentage: number;
  maturityDate?: string;
  status: 'active' | 'matured';
}

const mockInvestments: Investment[] = [
  {
    id: '1',
    type: 'fd',
    name: 'Fixed Deposit',
    investedAmount: 500000,
    currentValue: 537500,
    returns: 37500,
    returnPercentage: 7.5,
    maturityDate: '2026-06-15',
    status: 'active',
  },
  {
    id: '2',
    type: 'mf',
    name: 'Equity Growth Fund',
    investedAmount: 200000,
    currentValue: 245000,
    returns: 45000,
    returnPercentage: 22.5,
    status: 'active',
  },
  {
    id: '3',
    type: 'sip',
    name: 'Balanced Advantage SIP',
    investedAmount: 120000,
    currentValue: 135600,
    returns: 15600,
    returnPercentage: 13.0,
    status: 'active',
  },
  {
    id: '4',
    type: 'rd',
    name: 'Recurring Deposit',
    investedAmount: 60000,
    currentValue: 63000,
    returns: 3000,
    returnPercentage: 5.0,
    maturityDate: '2025-12-01',
    status: 'active',
  },
];

const getInvestmentIcon = (type: Investment['type']) => {
  switch (type) {
    case 'fd':
      return <FDIcon />;
    case 'rd':
      return <RDIcon />;
    case 'mf':
      return <MFIcon />;
    case 'sip':
      return <SIPIcon />;
    default:
      return <TrendingUp />;
  }
};

const getInvestmentColor = (type: Investment['type']) => {
  switch (type) {
    case 'fd':
      return '#1976d2';
    case 'rd':
      return '#9c27b0';
    case 'mf':
      return '#2e7d32';
    case 'sip':
      return '#ed6c02';
    default:
      return '#757575';
  }
};

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(amount);
};

const InvestmentsPage: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();

  const totalInvested = mockInvestments.reduce((sum, inv) => sum + inv.investedAmount, 0);
  const totalCurrentValue = mockInvestments.reduce((sum, inv) => sum + inv.currentValue, 0);
  const totalReturns = totalCurrentValue - totalInvested;
  const overallReturnPercentage = ((totalReturns / totalInvested) * 100).toFixed(2);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Investments
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/investments/new')}
        >
          New Investment
        </Button>
      </Box>

      {/* Portfolio Summary */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 3, fontWeight: 600 }}>
                Portfolio Overview
              </Typography>
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Total Invested
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 600 }}>
                      {formatCurrency(totalInvested)}
                    </Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Current Value
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 600, color: 'success.main' }}>
                      {formatCurrency(totalCurrentValue)}
                    </Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Total Returns
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Typography variant="h5" sx={{ fontWeight: 600, color: totalReturns >= 0 ? 'success.main' : 'error.main' }}>
                        {formatCurrency(totalReturns)}
                      </Typography>
                      <Chip
                        icon={totalReturns >= 0 ? <TrendingUp /> : <TrendingDown />}
                        label={`${overallReturnPercentage}%`}
                        size="small"
                        color={totalReturns >= 0 ? 'success' : 'error'}
                        sx={{ ml: 1 }}
                      />
                    </Box>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ height: '100%', background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`, color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <PieChart sx={{ mr: 1 }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Asset Allocation
                </Typography>
              </Box>
              <Box sx={{ mt: 2 }}>
                {['fd', 'mf', 'sip', 'rd'].map((type) => {
                  const typeInvestments = mockInvestments.filter(inv => inv.type === type);
                  const typeTotal = typeInvestments.reduce((sum, inv) => sum + inv.currentValue, 0);
                  const percentage = ((typeTotal / totalCurrentValue) * 100).toFixed(1);
                  return (
                    <Box key={type} sx={{ mb: 1 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                        <Typography variant="body2" sx={{ textTransform: 'uppercase' }}>
                          {type}
                        </Typography>
                        <Typography variant="body2">
                          {percentage}%
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={parseFloat(percentage)}
                        sx={{
                          height: 6,
                          borderRadius: 3,
                          bgcolor: 'rgba(255,255,255,0.2)',
                          '& .MuiLinearProgress-bar': {
                            bgcolor: 'white',
                          },
                        }}
                      />
                    </Box>
                  );
                })}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Investment Categories */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Your Investments
      </Typography>
      <Grid container spacing={3}>
        {mockInvestments.map((investment) => (
          <Grid size={{ xs: 12, sm: 6, lg: 3 }} key={investment.id}>
            <Card
              sx={{
                height: '100%',
                cursor: 'pointer',
                transition: 'all 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: theme.shadows[8],
                },
              }}
              onClick={() => navigate(`/investments/${investment.id}`)}
            >
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Avatar
                    sx={{
                      bgcolor: `${getInvestmentColor(investment.type)}20`,
                      color: getInvestmentColor(investment.type),
                      mr: 2,
                    }}
                  >
                    {getInvestmentIcon(investment.type)}
                  </Avatar>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                      {investment.name}
                    </Typography>
                    <Chip
                      label={investment.type.toUpperCase()}
                      size="small"
                      sx={{
                        bgcolor: `${getInvestmentColor(investment.type)}20`,
                        color: getInvestmentColor(investment.type),
                        fontSize: '0.65rem',
                        height: 20,
                      }}
                    />
                  </Box>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Current Value
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    {formatCurrency(investment.currentValue)}
                  </Typography>
                </Box>

                <Divider sx={{ my: 1.5 }} />

                <Grid container spacing={1}>
                  <Grid size={{ xs: 6 }}>
                    <Typography variant="caption" color="text.secondary">
                      Invested
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {formatCurrency(investment.investedAmount)}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <Typography variant="caption" color="text.secondary">
                      Returns
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      {investment.returns >= 0 ? (
                        <TrendingUp sx={{ fontSize: 16, color: 'success.main', mr: 0.5 }} />
                      ) : (
                        <TrendingDown sx={{ fontSize: 16, color: 'error.main', mr: 0.5 }} />
                      )}
                      <Typography
                        variant="body2"
                        sx={{
                          fontWeight: 500,
                          color: investment.returns >= 0 ? 'success.main' : 'error.main',
                        }}
                      >
                        {investment.returnPercentage}%
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>

                {investment.maturityDate && (
                  <Box sx={{ mt: 2, pt: 1.5, borderTop: 1, borderColor: 'divider' }}>
                    <Typography variant="caption" color="text.secondary">
                      Maturity Date
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {new Date(investment.maturityDate).toLocaleDateString('en-IN', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric',
                      })}
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Quick Actions */}
      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
          Quick Actions
        </Typography>
        <Grid container spacing={2}>
          {[
            { label: 'Open Fixed Deposit', icon: <FDIcon />, path: '/investments/fd/new' },
            { label: 'Start SIP', icon: <SIPIcon />, path: '/investments/sip/new' },
            { label: 'Explore Mutual Funds', icon: <MFIcon />, path: '/investments/mutual-funds' },
            { label: 'View Performance', icon: <Timeline />, path: '/investments/performance' },
          ].map((action) => (
            <Grid size={{ xs: 12, sm: 6, md: 3 }} key={action.label}>
              <Card
                sx={{
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                  '&:hover': {
                    bgcolor: 'primary.light',
                    '& .MuiTypography-root': { color: 'primary.main' },
                    '& .MuiSvgIcon-root': { color: 'primary.main' },
                  },
                }}
                onClick={() => navigate(action.path)}
              >
                <CardContent sx={{ display: 'flex', alignItems: 'center', py: 2 }}>
                  {action.icon}
                  <Typography variant="body1" sx={{ ml: 1.5, fontWeight: 500 }}>
                    {action.label}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Box>
  );
};

export default InvestmentsPage;
