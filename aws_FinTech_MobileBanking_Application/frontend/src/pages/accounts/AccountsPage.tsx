import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Skeleton,
  Avatar,
  Chip,
  IconButton,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  AccountBalance as AccountBalanceIcon,
  MoreVert as MoreVertIcon,
  ArrowForward as ArrowForwardIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
} from '@mui/icons-material';
import { AppDispatch, RootState } from '@/store';
import { fetchAccounts } from '@/store/slices/accountSlice';
import { formatCurrency, maskAccountNumber } from '@/utils/formatters';

const AccountsPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { accounts, isLoading } = useSelector((state: RootState) => state.accounts);

  const [showBalances, setShowBalances] = React.useState(true);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const [selectedAccountId, setSelectedAccountId] = React.useState<string | null>(null);

  useEffect(() => {
    dispatch(fetchAccounts());
  }, [dispatch]);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, accountId: string) => {
    setAnchorEl(event.currentTarget);
    setSelectedAccountId(accountId);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedAccountId(null);
  };

  const getAccountTypeColor = (type: string) => {
    switch (type) {
      case 'SAVINGS':
        return '#1976d2';
      case 'CURRENT':
        return '#2e7d32';
      case 'FIXED_DEPOSIT':
        return '#ed6c02';
      case 'RECURRING_DEPOSIT':
        return '#9c27b0';
      default:
        return '#757575';
    }
  };

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          My Accounts
        </Typography>
        <IconButton onClick={() => setShowBalances(!showBalances)}>
          {showBalances ? <VisibilityOffIcon /> : <VisibilityIcon />}
        </IconButton>
      </Box>

      {/* Total Balance Summary */}
      <Card sx={{ mb: 4, bgcolor: 'primary.main', color: 'white' }}>
        <CardContent>
          <Typography variant="body2" sx={{ opacity: 0.9 }}>
            Total Balance
          </Typography>
          <Typography variant="h3" sx={{ fontWeight: 700 }}>
            {isLoading ? (
              <Skeleton width={200} sx={{ bgcolor: 'rgba(255,255,255,0.3)' }} />
            ) : showBalances ? (
              formatCurrency(totalBalance)
            ) : (
              '••••••'
            )}
          </Typography>
        </CardContent>
      </Card>

      {/* Accounts Grid */}
      {isLoading ? (
        <Grid container spacing={3}>
          {[1, 2, 3].map((i) => (
            <Grid item xs={12} md={6} lg={4} key={i}>
              <Skeleton variant="rounded" height={200} />
            </Grid>
          ))}
        </Grid>
      ) : accounts.length === 0 ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 6 }}>
            <AccountBalanceIcon sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
            <Typography variant="h6" color="text.secondary">
              No accounts found
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {accounts.map((account) => (
            <Grid item xs={12} md={6} lg={4} key={account.id}>
              <Card
                sx={{
                  height: '100%',
                  cursor: 'pointer',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
                onClick={() => navigate(`/accounts/${account.id}`)}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Avatar
                      sx={{
                        bgcolor: `${getAccountTypeColor(account.accountType)}15`,
                        color: getAccountTypeColor(account.accountType),
                      }}
                    >
                      <AccountBalanceIcon />
                    </Avatar>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Chip
                        label={account.status}
                        size="small"
                        color={account.status === 'ACTIVE' ? 'success' : 'default'}
                      />
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleMenuOpen(e, account.id);
                        }}
                      >
                        <MoreVertIcon />
                      </IconButton>
                    </Box>
                  </Box>

                  <Typography variant="body2" color="text.secondary">
                    {account.accountType.replace('_', ' ')}
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 500, mb: 2 }}>
                    {maskAccountNumber(account.accountNumber)}
                  </Typography>

                  <Typography variant="body2" color="text.secondary">
                    Available Balance
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700 }}>
                    {showBalances ? formatCurrency(account.availableBalance) : '••••••'}
                  </Typography>

                  <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption" color="text.secondary">
                      IFSC: {account.ifscCode}
                    </Typography>
                    <ArrowForwardIcon fontSize="small" color="action" />
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Context Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => {
          navigate(`/accounts/${selectedAccountId}`);
          handleMenuClose();
        }}>
          View Details
        </MenuItem>
        <MenuItem onClick={() => {
          navigate('/transfers/own-account');
          handleMenuClose();
        }}>
          Transfer Funds
        </MenuItem>
        <MenuItem onClick={handleMenuClose}>Download Statement</MenuItem>
      </Menu>
    </Box>
  );
};

export default AccountsPage;
