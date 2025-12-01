import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Drawer,
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Divider,
  Collapse,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import PaymentsIcon from '@mui/icons-material/Payments';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import PeopleIcon from '@mui/icons-material/People';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import QrCodeScannerIcon from '@mui/icons-material/QrCodeScanner';
import ReceiptIcon from '@mui/icons-material/Receipt';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
  width: number;
  variant: 'permanent' | 'temporary';
}

interface NavItem {
  title: string;
  path?: string;
  icon: React.ReactNode;
  children?: { title: string; path: string }[];
}

const navItems: NavItem[] = [
  {
    title: 'Dashboard',
    path: '/dashboard',
    icon: <DashboardIcon />,
  },
  {
    title: 'Accounts',
    path: '/accounts',
    icon: <AccountBalanceIcon />,
  },
  {
    title: 'Transfers',
    icon: <SwapHorizIcon />,
    children: [
      { title: 'Own Account Transfer', path: '/transfers/own-account' },
      { title: 'Beneficiary Transfer', path: '/transfers/beneficiary' },
    ],
  },
  {
    title: 'Beneficiaries',
    path: '/beneficiaries',
    icon: <PeopleIcon />,
  },
  {
    title: 'Payments',
    icon: <PaymentsIcon />,
    children: [
      { title: 'Bill Payments', path: '/payments/bills' },
      { title: 'UPI Payment', path: '/payments/upi' },
      { title: 'QR Payment', path: '/payments/qr' },
    ],
  },
  {
    title: 'Cards',
    path: '/cards',
    icon: <CreditCardIcon />,
  },
  {
    title: 'Loans',
    path: '/loans',
    icon: <AccountBalanceWalletIcon />,
  },
  {
    title: 'Investments',
    path: '/investments',
    icon: <TrendingUpIcon />,
  },
  {
    title: 'Support',
    icon: <SupportAgentIcon />,
    children: [
      { title: 'Help & FAQ', path: '/support/faq' },
      { title: 'Contact Us', path: '/support' },
    ],
  },
];

const Sidebar: React.FC<SidebarProps> = ({ open, onClose, width, variant }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [openMenus, setOpenMenus] = React.useState<Record<string, boolean>>({});

  const handleToggle = (title: string) => {
    setOpenMenus((prev) => ({
      ...prev,
      [title]: !prev[title],
    }));
  };

  const handleNavigation = (path: string) => {
    navigate(path);
    if (variant === 'temporary') {
      onClose();
    }
  };

  const isActive = (path: string) => location.pathname === path;
  const isParentActive = (children?: { path: string }[]) =>
    children?.some((child) => location.pathname === child.path);

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Logo */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          p: 2,
          borderBottom: '1px solid',
          borderColor: 'divider',
        }}
      >
        <AccountBalanceIcon sx={{ fontSize: 36, color: 'primary.main' }} />
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}>
            FinBank
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Mobile Banking
          </Typography>
        </Box>
      </Box>

      {/* Navigation */}
      <List sx={{ flexGrow: 1, py: 1 }}>
        {navItems.map((item) => (
          <React.Fragment key={item.title}>
            <ListItem disablePadding>
              <ListItemButton
                onClick={() => {
                  if (item.children) {
                    handleToggle(item.title);
                  } else if (item.path) {
                    handleNavigation(item.path);
                  }
                }}
                sx={{
                  mx: 1,
                  borderRadius: 2,
                  mb: 0.5,
                  backgroundColor: item.path && isActive(item.path) ? 'primary.light' : 'transparent',
                  color: item.path && isActive(item.path) ? 'primary.contrastText' : 'inherit',
                  '&:hover': {
                    backgroundColor: item.path && isActive(item.path) ? 'primary.light' : 'action.hover',
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    color: (item.path && isActive(item.path)) || isParentActive(item.children)
                      ? 'primary.main'
                      : 'inherit',
                    minWidth: 40,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.title}
                  primaryTypographyProps={{
                    fontWeight: (item.path && isActive(item.path)) || isParentActive(item.children) ? 600 : 400,
                  }}
                />
                {item.children && (openMenus[item.title] ? <ExpandLess /> : <ExpandMore />)}
              </ListItemButton>
            </ListItem>

            {item.children && (
              <Collapse in={openMenus[item.title]} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                  {item.children.map((child) => (
                    <ListItemButton
                      key={child.path}
                      onClick={() => handleNavigation(child.path)}
                      sx={{
                        pl: 6,
                        mx: 1,
                        borderRadius: 2,
                        mb: 0.5,
                        backgroundColor: isActive(child.path) ? 'primary.light' : 'transparent',
                        color: isActive(child.path) ? 'primary.contrastText' : 'inherit',
                        '&:hover': {
                          backgroundColor: isActive(child.path) ? 'primary.light' : 'action.hover',
                        },
                      }}
                    >
                      <ListItemText
                        primary={child.title}
                        primaryTypographyProps={{
                          fontSize: '0.875rem',
                          fontWeight: isActive(child.path) ? 600 : 400,
                        }}
                      />
                    </ListItemButton>
                  ))}
                </List>
              </Collapse>
            )}
          </React.Fragment>
        ))}
      </List>

      {/* Footer */}
      <Box sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary" display="block" textAlign="center">
          © {new Date().getFullYear()} FinBank
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block" textAlign="center">
          Version 1.0.0
        </Typography>
      </Box>
    </Box>
  );

  return (
    <Drawer
      variant={variant}
      open={variant === 'temporary' ? open : true}
      onClose={onClose}
      ModalProps={{
        keepMounted: true, // Better mobile performance
      }}
      sx={{
        width: width,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: width,
          boxSizing: 'border-box',
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
};

export default Sidebar;
