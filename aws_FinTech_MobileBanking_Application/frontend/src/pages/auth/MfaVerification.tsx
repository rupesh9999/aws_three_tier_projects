import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { AppDispatch, RootState } from '@/store';
import { verifyMfa, clearError } from '@/store/slices/authSlice';
import { authService } from '@/services/api/authService';

interface MfaVerificationProps {
  mfaToken: string;
  onSuccess: () => void;
}

const MfaVerification: React.FC<MfaVerificationProps> = ({ mfaToken, onSuccess }) => {
  const dispatch = useDispatch<AppDispatch>();
  const { isLoading, error } = useSelector((state: RootState) => state.auth);

  const [otp, setOtp] = useState('');
  const [resendLoading, setResendLoading] = useState(false);
  const [resendMessage, setResendMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    dispatch(clearError());
    
    const result = await dispatch(verifyMfa({ otp, mfaToken }));
    
    if (verifyMfa.fulfilled.match(result)) {
      onSuccess();
    }
  };

  const handleResendOtp = async () => {
    setResendLoading(true);
    setResendMessage('');
    
    try {
      const response = await authService.resendOtp(mfaToken);
      setResendMessage(response.message || 'OTP sent successfully');
    } catch (err) {
      setResendMessage('Failed to resend OTP. Please try again.');
    } finally {
      setResendLoading(false);
    }
  };

  const handleOtpChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 6);
    setOtp(value);
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Typography variant="h5" component="h2" sx={{ mb: 1, fontWeight: 600 }}>
        Verify Your Identity
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        We've sent a verification code to your registered mobile number. Please enter the code below.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
          {error}
        </Alert>
      )}

      {resendMessage && (
        <Alert 
          severity={resendMessage.includes('Failed') ? 'error' : 'success'} 
          sx={{ mb: 2 }}
          onClose={() => setResendMessage('')}
        >
          {resendMessage}
        </Alert>
      )}

      <TextField
        label="Enter OTP"
        value={otp}
        onChange={handleOtpChange}
        fullWidth
        margin="normal"
        placeholder="000000"
        inputProps={{
          maxLength: 6,
          style: { letterSpacing: '0.5em', textAlign: 'center', fontSize: '1.5rem' },
        }}
        autoFocus
      />

      <Button
        type="submit"
        fullWidth
        variant="contained"
        size="large"
        sx={{ mt: 3, mb: 2 }}
        disabled={isLoading || otp.length !== 6}
      >
        {isLoading ? <CircularProgress size={24} /> : 'Verify'}
      </Button>

      <Box sx={{ textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          Didn't receive the code?{' '}
          <Button
            variant="text"
            size="small"
            onClick={handleResendOtp}
            disabled={resendLoading}
          >
            {resendLoading ? 'Sending...' : 'Resend OTP'}
          </Button>
        </Typography>
      </Box>
    </Box>
  );
};

export default MfaVerification;
