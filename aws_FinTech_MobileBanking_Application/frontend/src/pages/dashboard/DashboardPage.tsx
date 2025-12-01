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
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Chip,
  IconButton,
} from '@mui/material';
import {
  AccountBalance as AccountBalanceIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  ArrowForward as ArrowForwardIcon,
  CreditCard as CreditCardIcon,
  SwapHoriz as SwapHorizIcon,
  Payment as PaymentIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
} from '@mui/icons-material';
import { AppDispatch, RootState } from '@/store';
import { fetchAccounts } from '@/store/slices/accountSlice';
import { fetchCards } from '@/store/slices/cardSlice';
import { formatCurrency, formatDate, maskAccountNumber } from '@/utils/formatters';

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  
  const { user } = useSelector((state: RootState) => state.auth);
  const { accounts, isLoading: accountsLoading } = useSelector((state: RootState) => state.accounts);
  const { cards, isLoading: cardsLoading } = useSelector((state: RootState) => state.cards);

  const [showBalances, setShowBalances] = React.useState(true);

  useEffect(() => {
    dispatch(fetchAccounts());
    dispatch(fetchCards());
  }, [dispatch]);

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

  const quickActions = [
    { title: 'Transfer', icon: <SwapHorizIcon />, path: '/transfers', color: '#1976d2' },
    { title: 'Pay Bills', icon: <PaymentIcon />, path: '/payments/bills', color: '#2e7d32' },
    { title: 'Cards', icon: <CreditCardIcon />, path: '/cards', color: '#ed6c02' },
    { title: 'Accounts', icon: <AccountBalanceIcon />, path: '/accounts', color: '#9c27b0' },
  ];

  // Mock recent transactions for display
  const recentTransactions = [
    { id: '1', description: 'Salary Credit', amount: 5000, type: 'CREDIT', date: '2024-01-15' },
    { id: '2', description: 'Electricity Bill', amount: -150, type: 'DEBIT', date: '2024-01-14' },
    { id: '3', description: 'Online Shopping', amount: -89.99, type: 'DEBIT', date: '2024-01-13' },
    { id: '4', description: 'Transfer from John', amount: 200, type: 'CREDIT', date: '2024-01-12' },
  ];

  return (
    <Box>
      {/* Welcome Section */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Welcome back, {user?.firstName}!
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Here's an overview of your finances
        </Typography>
      </Box>

      {/* Total Balance Card */}
      <Card
        sx={{
          mb: 4,
          background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)',
          color: 'white',
        }}
      >
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <Box>
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                Total Balance
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="h3" sx={{ fontWeight: 700 }}>
                  {accountsLoading ? (
                    <Skeleton width={200} sx={{ bgcolor: 'rgba(255,255,255,0.3)' }} />
                  ) : showBalances ? (
                    formatCurrency(totalBalance)
                  ) : (
                    '••••••'
                  )}
                </Typography>
                <IconButton
                  size="small"
                  onClick={() => setShowBalances(!showBalances)}
                  sx={{ color: 'white' }}
                >
                  {showBalances ? <VisibilityOffIcon /> : <VisibilityIcon />}
                </IconButton>
              </Box>
              <Typography variant="body2" sx={{ mt: 1, opacity: 0.9 }}>
                Across {accounts.length} account{accounts.length !== 1 ? 's' : ''}
              </Typography>
            </Box>
            <AccountBalanceIcon sx={{ fontSize: 48, opacity: 0.3 }} />
          </Box>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Quick Actions
      </Typography>
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {quickActions.map((action) => (
          <Grid item xs={6} sm={3} key={action.title}>
            <Card
              sx={{
                cursor: 'pointer',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
              }}
              onClick={() => navigate(action.path)}
            >
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <Avatar
                  sx={{
                    width: 56,
                    height: 56,
                    bgcolor: `${action.color}15`,
                    color: action.color,
                    mx: 'auto',
                    mb: 1,
                  }}
                >
                  {action.icon}
                </Avatar>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>
                  {action.title}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Accounts Summary */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  My Accounts
                </Typography>
                <Button
                  size="small"
                  endIcon={<ArrowForwardIcon />}
                  onClick={() => navigate('/accounts')}
                >
                  View All
                </Button>
              </Box>

              {accountsLoading ? (
                <Box>
                  {[1, 2].map((i) => (
                    <Skeleton key={i} height={60} sx={{ mb: 1 }} />
                  ))}
                </Box>
              ) : accounts.length === 0 ? (
                <Typography color="text.secondary" textAlign="center" py={3}>
                  No accounts found
                </Typography>
              ) : (
                <List disablePadding>
                  {accounts.slice(0, 3).map((account) => (
                    <ListItem
                      key={account.id}
                      sx={{
                        px: 0,
                        borderBottom: '1px solid',
                        borderColor: 'divider',
                        '&:last-child': { borderBottom: 'none' },
                      }}
                    >
                      <ListItemAvatar>
                        <Avatar sx={{ bgcolor: 'primary.light' }}>
                          <AccountBalanceIcon />
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={account.accountType.replace('_', ' ')}
                        secondary={maskAccountNumber(account.accountNumber)}
                      />
                      <Typography variant="body1" sx={{ fontWeight: 600 }}>
                        {showBalances ? formatCurrency(account.balance) : '••••••'}
                      </Typography>
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Transactions */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Recent Transactions
                </Typography>
                <Button
                  size="small"
                  endIcon={<ArrowForwardIcon />}
                  onClick={() => navigate('/accounts')}
                >
                  View All
                </Button>
              </Box>

              <List disablePadding>
                {recentTransactions.map((transaction) => (
                  <ListItem
                    key={transaction.id}
                    sx={{
                      px: 0,
                      borderBottom: '1px solid',
                      borderColor: 'divider',
                      '&:last-child': { borderBottom: 'none' },
                    }}
                  >
                    <ListItemAvatar>
                      <Avatar
                        sx={{
                          bgcolor: transaction.type === 'CREDIT' ? 'success.light' : 'error.light',
                        }}
                      >
                        {transaction.type === 'CREDIT' ? <TrendingUpIcon /> : <TrendingDownIcon />}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={transaction.description}
                      secondary={formatDate(transaction.date)}
                    />
                    <Typography
                      variant="body1"
                      sx={{
                        fontWeight: 600,
                        color: transaction.type === 'CREDIT' ? 'success.main' : 'error.main',
                      }}
                    >
                      {transaction.type === 'CREDIT' ? '+' : ''}
                      {formatCurrency(transaction.amount)}
                    </Typography>
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* Cards Summary */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  My Cards
                </Typography>
                <Button
                  size="small"
                  endIcon={<ArrowForwardIcon />}
                  onClick={() => navigate('/cards')}
                >
                  View All
                </Button>
              </Box>

              {cardsLoading ? (
                <Grid container spacing={2}>
                  {[1, 2].map((i) => (
                    <Grid item xs={12} sm={6} key={i}>
                      <Skeleton height={100} variant="rounded" />
                    </Grid>
                  ))}
                </Grid>
              ) : cards.length === 0 ? (
                <Typography color="text.secondary" textAlign="center" py={3}>
                  No cards found
                </Typography>
              ) : (
                <Grid container spacing={2}>
                  {cards.slice(0, 2).map((card) => (
                    <Grid item xs={12} sm={6} key={card.id}>
                      <Box
                        sx={{
                          p: 2,
                          borderRadius: 2,
                          background: card.cardType === 'CREDIT'
                            ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                            : 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
                          color: 'white',
                        }}
                      >
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                          <Typography variant="body2" sx={{ opacity: 0.9 }}>
                            {card.cardType} Card
                          </Typography>
                          <Chip
                            label={card.isLocked ? 'Locked' : 'Active'}
                            size="small"
                            sx={{
                              bgcolor: 'rgba(255,255,255,0.2)',
                              color: 'white',
                            }}
                          />
                        </Box>
                        <Typography variant="h6" sx={{ letterSpacing: 2 }}>
                          {card.cardNumber}
                        </Typography>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
                          <Typography variant="body2">{card.cardHolderName}</Typography>
                          <Typography variant="body2">{card.expiryDate}</Typography>
                        </Box>
                      </Box>
                    </Grid>
                  ))}
                </Grid>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;
