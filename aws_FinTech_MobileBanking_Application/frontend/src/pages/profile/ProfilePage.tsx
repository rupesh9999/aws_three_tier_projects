import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Avatar,
  Button,
  Tabs,
  Tab,
  TextField,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Switch,
  IconButton,
  Chip,
  useTheme,
} from '@mui/material';
import {
  Person,
  Email,
  Phone,
  Home,
  Work,
  Edit,
  Save,
  Cancel,
  Security,
  Notifications,
  Fingerprint,
  PhoneAndroid,
  Lock,
  Verified,
  CameraAlt,
  Badge,
  CalendarMonth,
} from '@mui/icons-material';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <div role="tabpanel" hidden={value !== index}>
    {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
  </div>
);

const ProfilePage: React.FC = () => {
  const theme = useTheme();
  const [tabValue, setTabValue] = useState(0);
  const [isEditing, setIsEditing] = useState(false);
  const [profileData, setProfileData] = useState({
    firstName: 'Rajesh',
    lastName: 'Kumar',
    email: 'rajesh.kumar@email.com',
    phone: '+91 98765 43210',
    dateOfBirth: '1990-05-15',
    pan: 'ABCPK1234A',
    aadhaar: 'XXXX-XXXX-5678',
    address: '123, MG Road, Bangalore',
    city: 'Bangalore',
    state: 'Karnataka',
    pincode: '560001',
    occupation: 'Software Engineer',
    employer: 'Tech Solutions Pvt Ltd',
  });

  const [securitySettings, setSecuritySettings] = useState({
    biometric: true,
    twoFactor: true,
    loginAlerts: true,
    transactionAlerts: true,
    marketingEmails: false,
    smsAlerts: true,
    pushNotifications: true,
  });

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleSecurityToggle = (setting: keyof typeof securitySettings) => {
    setSecuritySettings((prev) => ({
      ...prev,
      [setting]: !prev[setting],
    }));
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 600, mb: 3 }}>
        Profile
      </Typography>

      {/* Profile Header */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Box sx={{ position: 'relative' }}>
              <Avatar
                sx={{
                  width: 100,
                  height: 100,
                  bgcolor: theme.palette.primary.main,
                  fontSize: 36,
                }}
              >
                {profileData.firstName[0]}{profileData.lastName[0]}
              </Avatar>
              <IconButton
                sx={{
                  position: 'absolute',
                  bottom: 0,
                  right: 0,
                  bgcolor: 'background.paper',
                  boxShadow: 1,
                  '&:hover': { bgcolor: 'grey.100' },
                }}
                size="small"
              >
                <CameraAlt fontSize="small" />
              </IconButton>
            </Box>
            <Box sx={{ ml: 3, flex: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Typography variant="h5" sx={{ fontWeight: 600 }}>
                  {profileData.firstName} {profileData.lastName}
                </Typography>
                <Chip
                  icon={<Verified />}
                  label="Verified"
                  size="small"
                  color="success"
                  sx={{ ml: 2 }}
                />
              </Box>
              <Typography variant="body1" color="text.secondary">
                {profileData.email}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Customer ID: CUST0012345
              </Typography>
            </Box>
            <Button
              variant={isEditing ? 'contained' : 'outlined'}
              startIcon={isEditing ? <Save /> : <Edit />}
              onClick={() => setIsEditing(!isEditing)}
            >
              {isEditing ? 'Save Changes' : 'Edit Profile'}
            </Button>
            {isEditing && (
              <Button
                variant="outlined"
                color="error"
                startIcon={<Cancel />}
                sx={{ ml: 1 }}
                onClick={() => setIsEditing(false)}
              >
                Cancel
              </Button>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Card>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
        >
          <Tab icon={<Person />} label="Personal Info" iconPosition="start" />
          <Tab icon={<Security />} label="Security" iconPosition="start" />
          <Tab icon={<Notifications />} label="Notifications" iconPosition="start" />
        </Tabs>

        <CardContent>
          {/* Personal Information Tab */}
          <TabPanel value={tabValue} index={0}>
            <Grid container spacing={4}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                  <Person sx={{ mr: 1 }} /> Basic Information
                </Typography>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      label="First Name"
                      value={profileData.firstName}
                      disabled={!isEditing}
                      onChange={(e) => setProfileData({ ...profileData, firstName: e.target.value })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      label="Last Name"
                      value={profileData.lastName}
                      disabled={!isEditing}
                      onChange={(e) => setProfileData({ ...profileData, lastName: e.target.value })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Email"
                      value={profileData.email}
                      disabled={!isEditing}
                      InputProps={{ startAdornment: <Email sx={{ mr: 1, color: 'text.secondary' }} /> }}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Phone"
                      value={profileData.phone}
                      disabled={!isEditing}
                      InputProps={{ startAdornment: <Phone sx={{ mr: 1, color: 'text.secondary' }} /> }}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Date of Birth"
                      type="date"
                      value={profileData.dateOfBirth}
                      disabled={!isEditing}
                      InputProps={{ startAdornment: <CalendarMonth sx={{ mr: 1, color: 'text.secondary' }} /> }}
                      InputLabelProps={{ shrink: true }}
                    />
                  </Grid>
                </Grid>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                  <Badge sx={{ mr: 1 }} /> KYC Details
                </Typography>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      label="PAN Number"
                      value={profileData.pan}
                      disabled
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      label="Aadhaar Number"
                      value={profileData.aadhaar}
                      disabled
                    />
                  </Grid>
                </Grid>

                <Typography variant="subtitle1" sx={{ fontWeight: 600, mt: 3, mb: 2, display: 'flex', alignItems: 'center' }}>
                  <Home sx={{ mr: 1 }} /> Address
                </Typography>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Address"
                      value={profileData.address}
                      disabled={!isEditing}
                      multiline
                      rows={2}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      fullWidth
                      label="City"
                      value={profileData.city}
                      disabled={!isEditing}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      fullWidth
                      label="State"
                      value={profileData.state}
                      disabled={!isEditing}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      fullWidth
                      label="Pincode"
                      value={profileData.pincode}
                      disabled={!isEditing}
                    />
                  </Grid>
                </Grid>

                <Typography variant="subtitle1" sx={{ fontWeight: 600, mt: 3, mb: 2, display: 'flex', alignItems: 'center' }}>
                  <Work sx={{ mr: 1 }} /> Employment
                </Typography>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      label="Occupation"
                      value={profileData.occupation}
                      disabled={!isEditing}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      label="Employer"
                      value={profileData.employer}
                      disabled={!isEditing}
                    />
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </TabPanel>

          {/* Security Tab */}
          <TabPanel value={tabValue} index={1}>
            <Grid container spacing={4}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                  Authentication
                </Typography>
                <Card variant="outlined">
                  <List disablePadding>
                    <ListItem>
                      <ListItemIcon>
                        <Fingerprint />
                      </ListItemIcon>
                      <ListItemText
                        primary="Biometric Login"
                        secondary="Use fingerprint or face recognition"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.biometric}
                          onChange={() => handleSecurityToggle('biometric')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemIcon>
                        <PhoneAndroid />
                      </ListItemIcon>
                      <ListItemText
                        primary="Two-Factor Authentication"
                        secondary="Require OTP for login"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.twoFactor}
                          onChange={() => handleSecurityToggle('twoFactor')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemIcon>
                        <Lock />
                      </ListItemIcon>
                      <ListItemText
                        primary="Change Password"
                        secondary="Last changed 30 days ago"
                      />
                      <ListItemSecondaryAction>
                        <Button size="small" variant="outlined">
                          Change
                        </Button>
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemIcon>
                        <Security />
                      </ListItemIcon>
                      <ListItemText
                        primary="Transaction PIN"
                        secondary="Required for all transactions"
                      />
                      <ListItemSecondaryAction>
                        <Button size="small" variant="outlined">
                          Reset
                        </Button>
                      </ListItemSecondaryAction>
                    </ListItem>
                  </List>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                  Login Activity
                </Typography>
                <Card variant="outlined">
                  <List disablePadding>
                    {[
                      { device: 'iPhone 14 Pro', location: 'Bangalore, IN', time: '2 hours ago', current: true },
                      { device: 'MacBook Pro', location: 'Bangalore, IN', time: 'Yesterday', current: false },
                      { device: 'Chrome on Windows', location: 'Mumbai, IN', time: '3 days ago', current: false },
                    ].map((session, index) => (
                      <React.Fragment key={index}>
                        {index > 0 && <Divider />}
                        <ListItem>
                          <ListItemIcon>
                            <PhoneAndroid />
                          </ListItemIcon>
                          <ListItemText
                            primary={
                              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                {session.device}
                                {session.current && (
                                  <Chip label="Current" size="small" color="success" sx={{ ml: 1 }} />
                                )}
                              </Box>
                            }
                            secondary={`${session.location} • ${session.time}`}
                          />
                          {!session.current && (
                            <ListItemSecondaryAction>
                              <Button size="small" color="error">
                                Logout
                              </Button>
                            </ListItemSecondaryAction>
                          )}
                        </ListItem>
                      </React.Fragment>
                    ))}
                  </List>
                </Card>
              </Grid>
            </Grid>
          </TabPanel>

          {/* Notifications Tab */}
          <TabPanel value={tabValue} index={2}>
            <Grid container spacing={4}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                  Alert Preferences
                </Typography>
                <Card variant="outlined">
                  <List disablePadding>
                    <ListItem>
                      <ListItemText
                        primary="Login Alerts"
                        secondary="Get notified on new login"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.loginAlerts}
                          onChange={() => handleSecurityToggle('loginAlerts')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText
                        primary="Transaction Alerts"
                        secondary="Notifications for all transactions"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.transactionAlerts}
                          onChange={() => handleSecurityToggle('transactionAlerts')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText
                        primary="SMS Alerts"
                        secondary="Receive SMS for important updates"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.smsAlerts}
                          onChange={() => handleSecurityToggle('smsAlerts')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText
                        primary="Push Notifications"
                        secondary="In-app and mobile notifications"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.pushNotifications}
                          onChange={() => handleSecurityToggle('pushNotifications')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText
                        primary="Marketing Emails"
                        secondary="Offers, promotions and newsletters"
                      />
                      <ListItemSecondaryAction>
                        <Switch
                          checked={securitySettings.marketingEmails}
                          onChange={() => handleSecurityToggle('marketingEmails')}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                  </List>
                </Card>
              </Grid>
            </Grid>
          </TabPanel>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ProfilePage;
