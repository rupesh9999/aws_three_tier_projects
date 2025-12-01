import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Card, CardContent, Avatar } from '@mui/material';
import { SwapHoriz, AccountBalance, People } from '@mui/icons-material';

const TransfersPage: React.FC = () => {
  const navigate = useNavigate();

  const transferOptions = [
    {
      title: 'Own Account Transfer',
      description: 'Transfer between your accounts',
      icon: <AccountBalance />,
      path: '/transfers/own-account',
      color: '#1976d2',
    },
    {
      title: 'Beneficiary Transfer',
      description: 'Transfer to saved beneficiaries',
      icon: <People />,
      path: '/transfers/beneficiary',
      color: '#2e7d32',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 600, mb: 3 }}>
        Fund Transfer
      </Typography>

      <Grid container spacing={3}>
        {transferOptions.map((option) => (
          <Grid item xs={12} sm={6} key={option.title}>
            <Card
              sx={{
                cursor: 'pointer',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': { transform: 'translateY(-4px)', boxShadow: 4 },
              }}
              onClick={() => navigate(option.path)}
            >
              <CardContent sx={{ p: 3 }}>
                <Avatar sx={{ bgcolor: `${option.color}15`, color: option.color, mb: 2 }}>
                  {option.icon}
                </Avatar>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  {option.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {option.description}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default TransfersPage;
