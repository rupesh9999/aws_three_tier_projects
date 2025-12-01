import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActionArea,
  TextField,
  Button,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  Chip,
  Divider,
  useTheme,
} from '@mui/material';
import {
  Help,
  Phone,
  Email,
  Chat,
  ExpandMore,
  CreditCard,
  AccountBalance,
  Payment,
  Security,
  Send,
  Search,
  Article,
  ContactSupport,
  Headset,
  QuestionAnswer,
} from '@mui/icons-material';

interface FAQ {
  question: string;
  answer: string;
  category: string;
}

const faqs: FAQ[] = [
  {
    question: 'How do I reset my password?',
    answer: 'Go to Profile > Security > Change Password. You will receive an OTP on your registered mobile number to verify the change.',
    category: 'Security',
  },
  {
    question: 'How do I add a new beneficiary?',
    answer: 'Navigate to Transfers > Manage Beneficiaries > Add New. Enter the account details and verify with OTP. The beneficiary will be active after 24 hours.',
    category: 'Transfers',
  },
  {
    question: 'What is the transaction limit for IMPS?',
    answer: 'The default IMPS limit is ₹2,00,000 per transaction and ₹5,00,000 per day. You can request a limit increase from Settings.',
    category: 'Transfers',
  },
  {
    question: 'How do I block my debit/credit card?',
    answer: 'Go to Cards > Select Card > Block Card. This is instant and irreversible. For temporary block, use the "Freeze Card" option.',
    category: 'Cards',
  },
  {
    question: 'How do I update my mobile number?',
    answer: 'Visit your nearest branch with valid ID proof to update your registered mobile number. This cannot be done online for security reasons.',
    category: 'Account',
  },
  {
    question: 'How do I download my account statement?',
    answer: 'Go to Accounts > Select Account > Statement > Choose date range and format (PDF/Excel) > Download.',
    category: 'Account',
  },
  {
    question: 'What should I do if I suspect fraud?',
    answer: 'Immediately block your cards, change your password, and call our 24/7 helpline at 1800-XXX-XXXX. File a report through the app under Support > Report Fraud.',
    category: 'Security',
  },
  {
    question: 'How do I set up UPI?',
    answer: 'Go to Payments > UPI > Setup UPI. Link your bank account, create a UPI PIN, and set your preferred UPI ID.',
    category: 'Payments',
  },
];

const SupportPage: React.FC = () => {
  const theme = useTheme();
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedFaq, setExpandedFaq] = useState<string | false>(false);
  const [message, setMessage] = useState('');

  const filteredFaqs = faqs.filter(
    (faq) =>
      faq.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
      faq.answer.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleFaqChange = (panel: string) => (_event: React.SyntheticEvent, isExpanded: boolean) => {
    setExpandedFaq(isExpanded ? panel : false);
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 600, mb: 3 }}>
        Help & Support
      </Typography>

      {/* Quick Contact Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card
            sx={{
              background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
              color: 'white',
              height: '100%',
            }}
          >
            <CardActionArea sx={{ height: '100%', p: 2 }}>
              <CardContent sx={{ textAlign: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                  <Phone sx={{ fontSize: 28 }} />
                </Avatar>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                  Call Us
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  24/7 Helpline
                </Typography>
                <Typography variant="h6" sx={{ fontWeight: 600, mt: 1 }}>
                  1800-XXX-XXXX
                </Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card
            sx={{
              background: `linear-gradient(135deg, ${theme.palette.success.main} 0%, ${theme.palette.success.dark} 100%)`,
              color: 'white',
              height: '100%',
            }}
          >
            <CardActionArea sx={{ height: '100%', p: 2 }}>
              <CardContent sx={{ textAlign: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                  <Chat sx={{ fontSize: 28 }} />
                </Avatar>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                  Live Chat
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Chat with our team
                </Typography>
                <Chip label="Online" size="small" sx={{ bgcolor: 'rgba(255,255,255,0.3)', color: 'white', mt: 1 }} />
              </CardContent>
            </CardActionArea>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card
            sx={{
              background: `linear-gradient(135deg, ${theme.palette.warning.main} 0%, ${theme.palette.warning.dark} 100%)`,
              color: 'white',
              height: '100%',
            }}
          >
            <CardActionArea sx={{ height: '100%', p: 2 }}>
              <CardContent sx={{ textAlign: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                  <Email sx={{ fontSize: 28 }} />
                </Avatar>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                  Email Us
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Get response in 24hrs
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 600, mt: 1 }}>
                  support@bank.com
                </Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card
            sx={{
              background: `linear-gradient(135deg, ${theme.palette.error.main} 0%, ${theme.palette.error.dark} 100%)`,
              color: 'white',
              height: '100%',
            }}
          >
            <CardActionArea sx={{ height: '100%', p: 2 }}>
              <CardContent sx={{ textAlign: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                  <Security sx={{ fontSize: 28 }} />
                </Avatar>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                  Report Fraud
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Immediate action
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 600, mt: 1 }}>
                  Block & Report
                </Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* FAQs */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <QuestionAnswer sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Frequently Asked Questions
                </Typography>
              </Box>

              <TextField
                fullWidth
                placeholder="Search FAQs..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
                }}
                sx={{ mb: 2 }}
              />

              {filteredFaqs.length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Help sx={{ fontSize: 48, color: 'grey.300', mb: 2 }} />
                  <Typography color="text.secondary">
                    No FAQs found. Try a different search term.
                  </Typography>
                </Box>
              ) : (
                filteredFaqs.map((faq, index) => (
                  <Accordion
                    key={index}
                    expanded={expandedFaq === `faq-${index}`}
                    onChange={handleFaqChange(`faq-${index}`)}
                    sx={{ mb: 1 }}
                  >
                    <AccordionSummary expandIcon={<ExpandMore />}>
                      <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                        <Typography sx={{ fontWeight: 500, flex: 1 }}>
                          {faq.question}
                        </Typography>
                        <Chip label={faq.category} size="small" sx={{ mr: 2 }} />
                      </Box>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Typography color="text.secondary">
                        {faq.answer}
                      </Typography>
                    </AccordionDetails>
                  </Accordion>
                ))
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Links & Contact Form */}
        <Grid size={{ xs: 12, md: 5 }}>
          {/* Quick Links */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Article sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Quick Links
                </Typography>
              </Box>
              <List disablePadding>
                {[
                  { icon: <CreditCard />, text: 'Card Services', desc: 'Block, replace, or request cards' },
                  { icon: <AccountBalance />, text: 'Account Services', desc: 'Statements, details, and more' },
                  { icon: <Payment />, text: 'Transaction Disputes', desc: 'Raise and track disputes' },
                  { icon: <Security />, text: 'Security Center', desc: 'Manage security settings' },
                ].map((item, index) => (
                  <React.Fragment key={index}>
                    {index > 0 && <Divider />}
                    <ListItem
                      component="div"
                      sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                    >
                      <ListItemIcon>
                        <Avatar sx={{ bgcolor: 'primary.light', color: 'primary.main' }}>
                          {item.icon}
                        </Avatar>
                      </ListItemIcon>
                      <ListItemText
                        primary={item.text}
                        secondary={item.desc}
                      />
                    </ListItem>
                  </React.Fragment>
                ))}
              </List>
            </CardContent>
          </Card>

          {/* Contact Form */}
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <ContactSupport sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Send us a Message
                </Typography>
              </Box>
              <TextField
                fullWidth
                label="Subject"
                placeholder="Brief description of your query"
                sx={{ mb: 2 }}
              />
              <TextField
                fullWidth
                label="Message"
                placeholder="Describe your issue in detail..."
                multiline
                rows={4}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                sx={{ mb: 2 }}
              />
              <Button
                fullWidth
                variant="contained"
                startIcon={<Send />}
                disabled={!message.trim()}
              >
                Submit
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SupportPage;
