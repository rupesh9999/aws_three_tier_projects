import React, { useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
  LinearProgress,
  Divider,
  IconButton,
  Menu,
  MenuItem,
  useTheme,
} from '@mui/material';
import {
  AccountBalance as LoanIcon,
  Home as HomeLoanIcon,
  DirectionsCar as AutoLoanIcon,
  School as EducationIcon,
  Person as PersonalIcon,
  MoreVert as MoreIcon,
  Add as AddIcon,
  TrendingUp,
  CalendarMonth,
  Percent,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

interface Loan {
  id: string;
  type: 'home' | 'auto' | 'personal' | 'education';
  accountNumber: string;
  principalAmount: number;
  outstandingAmount: number;
  interestRate: number;
  tenure: number;
  tenureCompleted: number;
  emiAmount: number;
  nextEmiDate: string;
  status: 'active' | 'closed' | 'overdue';
}

const mockLoans: Loan[] = [
  {
    id: '1',
    type: 'home',
    accountNumber: 'HL****4521',
    principalAmount: 5000000,
    outstandingAmount: 3750000,
    interestRate: 8.5,
    tenure: 240,
    tenureCompleted: 60,
    emiAmount: 43391,
    nextEmiDate: '2025-02-05',
    status: 'active',
  },
  {
    id: '2',
    type: 'auto',
    accountNumber: 'AL****7832',
    principalAmount: 800000,
    outstandingAmount: 450000,
    interestRate: 9.25,
    tenure: 60,
    tenureCompleted: 30,
    emiAmount: 16640,
    nextEmiDate: '2025-02-10',
    status: 'active',
  },
  {
    id: '3',
    type: 'personal',
    accountNumber: 'PL****9012',
    principalAmount: 300000,
    outstandingAmount: 125000,
    interestRate: 12.5,
    tenure: 36,
    tenureCompleted: 24,
    emiAmount: 10030,
    nextEmiDate: '2025-02-15',
    status: 'active',
  },
];

const getLoanIcon = (type: Loan['type']) => {
  switch (type) {
    case 'home':
      return <HomeLoanIcon />;
    case 'auto':
      return <AutoLoanIcon />;
    case 'education':
      return <EducationIcon />;
    case 'personal':
      return <PersonalIcon />;
    default:
      return <LoanIcon />;
  }
};

const getLoanLabel = (type: Loan['type']) => {
  switch (type) {
    case 'home':
      return 'Home Loan';
    case 'auto':
      return 'Auto Loan';
    case 'education':
      return 'Education Loan';
    case 'personal':
      return 'Personal Loan';
    default:
      return 'Loan';
  }
};

const getStatusColor = (status: Loan['status']) => {
  switch (status) {
    case 'active':
      return 'success';
    case 'closed':
      return 'default';
    case 'overdue':
      return 'error';
    default:
      return 'default';
  }
};

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(amount);
};

const LoansPage: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const [selectedLoan, setSelectedLoan] = React.useState<string | null>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, loanId: string) => {
    setAnchorEl(event.currentTarget);
    setSelectedLoan(loanId);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedLoan(null);
  };

  const totalOutstanding = mockLoans.reduce((sum, loan) => sum + loan.outstandingAmount, 0);
  const totalEmi = mockLoans.reduce((sum, loan) => sum + loan.emiAmount, 0);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Loans
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/loans/apply')}
        >
          Apply for Loan
        </Button>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card sx={{ background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`, color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <LoanIcon sx={{ mr: 1 }} />
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Active Loans
                </Typography>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 600 }}>
                {mockLoans.filter(l => l.status === 'active').length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card sx={{ background: `linear-gradient(135deg, ${theme.palette.warning.main} 0%, ${theme.palette.warning.dark} 100%)`, color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <TrendingUp sx={{ mr: 1 }} />
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Total Outstanding
                </Typography>
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                {formatCurrency(totalOutstanding)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card sx={{ background: `linear-gradient(135deg, ${theme.palette.success.main} 0%, ${theme.palette.success.dark} 100%)`, color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <CalendarMonth sx={{ mr: 1 }} />
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Monthly EMI
                </Typography>
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                {formatCurrency(totalEmi)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card sx={{ background: `linear-gradient(135deg, ${theme.palette.info.main} 0%, ${theme.palette.info.dark} 100%)`, color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Percent sx={{ mr: 1 }} />
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Avg Interest Rate
                </Typography>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 600 }}>
                {(mockLoans.reduce((sum, l) => sum + l.interestRate, 0) / mockLoans.length).toFixed(2)}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Loan Cards */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Your Loans
      </Typography>
      <Grid container spacing={3}>
        {mockLoans.map((loan) => (
          <Grid size={{ xs: 12, md: 6 }} key={loan.id}>
            <Card
              sx={{
                cursor: 'pointer',
                transition: 'all 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: theme.shadows[8],
                },
              }}
              onClick={() => navigate(`/loans/${loan.id}`)}
            >
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        borderRadius: 2,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        bgcolor: 'primary.light',
                        color: 'primary.main',
                        mr: 2,
                      }}
                    >
                      {getLoanIcon(loan.type)}
                    </Box>
                    <Box>
                      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                        {getLoanLabel(loan.type)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {loan.accountNumber}
                      </Typography>
                    </Box>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Chip
                      label={loan.status.charAt(0).toUpperCase() + loan.status.slice(1)}
                      size="small"
                      color={getStatusColor(loan.status) as any}
                    />
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleMenuOpen(e, loan.id);
                      }}
                    >
                      <MoreIcon />
                    </IconButton>
                  </Box>
                </Box>

                <Grid container spacing={2} sx={{ mb: 2 }}>
                  <Grid size={{ xs: 6 }}>
                    <Typography variant="body2" color="text.secondary">
                      Principal Amount
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {formatCurrency(loan.principalAmount)}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <Typography variant="body2" color="text.secondary">
                      Outstanding
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'warning.main' }}>
                      {formatCurrency(loan.outstandingAmount)}
                    </Typography>
                  </Grid>
                </Grid>

                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="body2" color="text.secondary">
                      Tenure Progress
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {loan.tenureCompleted}/{loan.tenure} months
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={(loan.tenureCompleted / loan.tenure) * 100}
                    sx={{ height: 8, borderRadius: 4 }}
                  />
                </Box>

                <Divider sx={{ my: 2 }} />

                <Grid container spacing={2}>
                  <Grid size={{ xs: 4 }}>
                    <Typography variant="body2" color="text.secondary">
                      Interest Rate
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {loan.interestRate}%
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 4 }}>
                    <Typography variant="body2" color="text.secondary">
                      EMI Amount
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {formatCurrency(loan.emiAmount)}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 4 }}>
                    <Typography variant="body2" color="text.secondary">
                      Next EMI
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {new Date(loan.nextEmiDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })}
                    </Typography>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleMenuClose}>View Details</MenuItem>
        <MenuItem onClick={handleMenuClose}>Pay EMI</MenuItem>
        <MenuItem onClick={handleMenuClose}>Download Statement</MenuItem>
        <MenuItem onClick={handleMenuClose}>EMI Calculator</MenuItem>
        <MenuItem onClick={handleMenuClose}>Prepayment</MenuItem>
      </Menu>
    </Box>
  );
};

export default LoansPage;
