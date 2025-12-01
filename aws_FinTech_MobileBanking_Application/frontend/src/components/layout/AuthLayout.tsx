import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box, Container, Paper, Typography } from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';

const AuthLayout: React.FC = () => {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)',
        padding: 2,
      }}
    >
      <Container maxWidth="sm">
        <Paper
          elevation={8}
          sx={{
            p: 4,
            borderRadius: 3,
          }}
        >
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              mb: 3,
            }}
          >
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                mb: 1,
              }}
            >
              <AccountBalanceIcon sx={{ fontSize: 40, color: 'primary.main' }} />
              <Typography
                variant="h4"
                component="h1"
                sx={{ fontWeight: 700, color: 'primary.main' }}
              >
                FinBank
              </Typography>
            </Box>
            <Typography variant="body2" color="text.secondary">
              Secure Mobile Banking
            </Typography>
          </Box>

          <Outlet />
        </Paper>

        <Typography
          variant="caption"
          sx={{
            display: 'block',
            textAlign: 'center',
            mt: 3,
            color: 'white',
            opacity: 0.8,
          }}
        >
          © {new Date().getFullYear()} FinBank. All rights reserved.
        </Typography>
      </Container>
    </Box>
  );
};

export default AuthLayout;
