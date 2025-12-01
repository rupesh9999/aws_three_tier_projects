import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Chip,
  Button,
  Avatar,
  Divider,
  Badge,
  useTheme,
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  Payment,
  CreditCard,
  AccountBalance,
  Security,
  Campaign,
  Delete,
  MarkEmailRead,
  CheckCircle,
  Error,
  Info,
  Warning,
  FilterList,
  DoneAll,
} from '@mui/icons-material';

interface Notification {
  id: string;
  type: 'transaction' | 'security' | 'promotion' | 'alert' | 'info';
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}

const mockNotifications: Notification[] = [
  {
    id: '1',
    type: 'transaction',
    title: 'Payment Successful',
    message: 'Your payment of ₹5,000 to Rajesh Kumar was successful.',
    timestamp: '2025-01-15T10:30:00',
    read: false,
  },
  {
    id: '2',
    type: 'security',
    title: 'New Login Detected',
    message: 'A new login was detected from iPhone 14 Pro in Bangalore.',
    timestamp: '2025-01-15T09:15:00',
    read: false,
  },
  {
    id: '3',
    type: 'alert',
    title: 'Low Balance Alert',
    message: 'Your Savings Account balance is below ₹10,000.',
    timestamp: '2025-01-14T18:00:00',
    read: true,
  },
  {
    id: '4',
    type: 'promotion',
    title: 'Exclusive Offer',
    message: 'Get 5% cashback on your next credit card transaction!',
    timestamp: '2025-01-14T12:00:00',
    read: true,
  },
  {
    id: '5',
    type: 'transaction',
    title: 'EMI Deducted',
    message: 'Your home loan EMI of ₹43,391 has been deducted.',
    timestamp: '2025-01-05T08:00:00',
    read: true,
  },
  {
    id: '6',
    type: 'info',
    title: 'Account Statement Ready',
    message: 'Your December 2024 account statement is now available.',
    timestamp: '2025-01-01T09:00:00',
    read: true,
  },
  {
    id: '7',
    type: 'security',
    title: 'Password Changed',
    message: 'Your account password was changed successfully.',
    timestamp: '2024-12-28T14:30:00',
    read: true,
  },
];

const getNotificationIcon = (type: Notification['type']) => {
  switch (type) {
    case 'transaction':
      return <Payment />;
    case 'security':
      return <Security />;
    case 'promotion':
      return <Campaign />;
    case 'alert':
      return <Warning />;
    case 'info':
      return <Info />;
    default:
      return <NotificationsIcon />;
  }
};

const getNotificationColor = (type: Notification['type']) => {
  switch (type) {
    case 'transaction':
      return 'success';
    case 'security':
      return 'error';
    case 'promotion':
      return 'primary';
    case 'alert':
      return 'warning';
    case 'info':
      return 'info';
    default:
      return 'default';
  }
};

const formatTimestamp = (timestamp: string): string => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
};

const NotificationsPage: React.FC = () => {
  const theme = useTheme();
  const [tabValue, setTabValue] = useState(0);
  const [notifications, setNotifications] = useState(mockNotifications);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleMarkAsRead = (id: string) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
  };

  const handleDelete = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const handleMarkAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const filteredNotifications = notifications.filter((n) => {
    if (tabValue === 0) return true;
    if (tabValue === 1) return n.type === 'transaction';
    if (tabValue === 2) return n.type === 'security';
    if (tabValue === 3) return n.type === 'promotion' || n.type === 'info';
    if (tabValue === 4) return n.type === 'alert';
    return true;
  });

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Typography variant="h4" sx={{ fontWeight: 600 }}>
            Notifications
          </Typography>
          {unreadCount > 0 && (
            <Chip
              label={`${unreadCount} unread`}
              size="small"
              color="primary"
              sx={{ ml: 2 }}
            />
          )}
        </Box>
        <Button
          variant="outlined"
          startIcon={<DoneAll />}
          onClick={handleMarkAllAsRead}
          disabled={unreadCount === 0}
        >
          Mark All as Read
        </Button>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[
          { label: 'All', count: notifications.length, icon: <NotificationsIcon /> },
          { label: 'Transactions', count: notifications.filter((n) => n.type === 'transaction').length, icon: <Payment /> },
          { label: 'Security', count: notifications.filter((n) => n.type === 'security').length, icon: <Security /> },
          { label: 'Alerts', count: notifications.filter((n) => n.type === 'alert').length, icon: <Warning /> },
        ].map((item, index) => (
          <Grid size={{ xs: 6, sm: 3 }} key={item.label}>
            <Card
              sx={{
                cursor: 'pointer',
                border: tabValue === index ? 2 : 0,
                borderColor: 'primary.main',
              }}
              onClick={() => setTabValue(index)}
            >
              <CardContent sx={{ py: 2, textAlign: 'center' }}>
                <Avatar
                  sx={{
                    bgcolor: tabValue === index ? 'primary.main' : 'grey.100',
                    color: tabValue === index ? 'white' : 'grey.600',
                    width: 40,
                    height: 40,
                    mx: 'auto',
                    mb: 1,
                  }}
                >
                  {item.icon}
                </Avatar>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  {item.count}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {item.label}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Notifications List */}
      <Card>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
        >
          <Tab label="All" />
          <Tab label="Transactions" />
          <Tab label="Security" />
          <Tab label="Updates" />
          <Tab label="Alerts" />
        </Tabs>

        <List disablePadding>
          {filteredNotifications.length === 0 ? (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <NotificationsIcon sx={{ fontSize: 64, color: 'grey.300', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                No notifications
              </Typography>
              <Typography variant="body2" color="text.secondary">
                You're all caught up!
              </Typography>
            </Box>
          ) : (
            filteredNotifications.map((notification, index) => (
              <React.Fragment key={notification.id}>
                {index > 0 && <Divider />}
                <ListItem
                  sx={{
                    bgcolor: notification.read ? 'transparent' : 'action.hover',
                    '&:hover': { bgcolor: 'action.selected' },
                  }}
                >
                  <ListItemIcon>
                    <Badge
                      color={getNotificationColor(notification.type) as any}
                      variant="dot"
                      invisible={notification.read}
                    >
                      <Avatar
                        sx={{
                          bgcolor: `${getNotificationColor(notification.type)}.light`,
                          color: `${getNotificationColor(notification.type)}.main`,
                        }}
                      >
                        {getNotificationIcon(notification.type)}
                      </Avatar>
                    </Badge>
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Typography
                        variant="subtitle2"
                        sx={{ fontWeight: notification.read ? 400 : 600 }}
                      >
                        {notification.title}
                      </Typography>
                    }
                    secondary={
                      <Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                          {notification.message}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {formatTimestamp(notification.timestamp)}
                        </Typography>
                      </Box>
                    }
                  />
                  <ListItemSecondaryAction>
                    {!notification.read && (
                      <IconButton
                        edge="end"
                        size="small"
                        onClick={() => handleMarkAsRead(notification.id)}
                        sx={{ mr: 1 }}
                        title="Mark as read"
                      >
                        <MarkEmailRead />
                      </IconButton>
                    )}
                    <IconButton
                      edge="end"
                      size="small"
                      onClick={() => handleDelete(notification.id)}
                      title="Delete"
                    >
                      <Delete />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              </React.Fragment>
            ))
          )}
        </List>
      </Card>
    </Box>
  );
};

export default NotificationsPage;
