import { Link } from 'react-router-dom';
import { HelpCircle, Phone, Mail, MessageCircle, ChevronDown } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';

export default function HelpPage() {
  const faqs = [
    {
      question: 'How do I cancel my booking?',
      answer: 'You can cancel your booking by going to "My Bookings", selecting the booking you want to cancel, and clicking the "Cancel Booking" button. Refunds are processed according to our cancellation policy.',
    },
    {
      question: 'What payment methods do you accept?',
      answer: 'We accept credit/debit cards (Visa, Mastercard, American Express), UPI, net banking, and major digital wallets.',
    },
    {
      question: 'How do I get my e-ticket?',
      answer: 'After successful payment, your e-ticket will be emailed to you. You can also download it from the booking details page in "My Bookings".',
    },
    {
      question: 'Can I modify my booking?',
      answer: 'Modifications depend on the type of booking and fare rules. Please contact our support team for assistance with modifications.',
    },
    {
      question: 'How long does a refund take?',
      answer: 'Refunds are typically processed within 5-7 business days. The time to reflect in your account depends on your bank or payment provider.',
    },
    {
      question: 'Is my payment information secure?',
      answer: 'Yes, we use industry-standard encryption and are PCI-DSS compliant. Your payment information is never stored on our servers.',
    },
  ];

  return (
    <div className="container py-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <HelpCircle className="h-16 w-16 mx-auto text-primary mb-4" />
          <h1 className="text-3xl font-bold mb-4">How can we help you?</h1>
          <div className="max-w-md mx-auto">
            <Input placeholder="Search for help topics..." className="text-center" />
          </div>
        </div>

        {/* Contact Options */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <Card className="text-center hover:shadow-md transition-shadow">
            <CardContent className="py-8">
              <Phone className="h-10 w-10 mx-auto text-primary mb-4" />
              <h3 className="font-semibold mb-2">Call Us</h3>
              <p className="text-muted-foreground mb-4">24/7 Customer Support</p>
              <p className="font-medium text-primary">1-800-TRAVEL-EASE</p>
            </CardContent>
          </Card>
          <Card className="text-center hover:shadow-md transition-shadow">
            <CardContent className="py-8">
              <Mail className="h-10 w-10 mx-auto text-primary mb-4" />
              <h3 className="font-semibold mb-2">Email Us</h3>
              <p className="text-muted-foreground mb-4">Get a response within 24 hours</p>
              <p className="font-medium text-primary">support@travelease.com</p>
            </CardContent>
          </Card>
          <Card className="text-center hover:shadow-md transition-shadow">
            <CardContent className="py-8">
              <MessageCircle className="h-10 w-10 mx-auto text-primary mb-4" />
              <h3 className="font-semibold mb-2">Live Chat</h3>
              <p className="text-muted-foreground mb-4">Chat with our support team</p>
              <Button>Start Chat</Button>
            </CardContent>
          </Card>
        </div>

        {/* FAQs */}
        <Card id="faq">
          <CardHeader>
            <CardTitle>Frequently Asked Questions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {faqs.map((faq, index) => (
                <details key={index} className="group border rounded-lg">
                  <summary className="flex items-center justify-between p-4 cursor-pointer list-none">
                    <span className="font-medium">{faq.question}</span>
                    <ChevronDown className="h-5 w-5 text-muted-foreground group-open:rotate-180 transition-transform" />
                  </summary>
                  <div className="px-4 pb-4 text-muted-foreground">
                    {faq.answer}
                  </div>
                </details>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Quick Links */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-12">
          <Link to="/help#refunds" className="p-4 border rounded-lg text-center hover:border-primary transition-colors">
            <p className="font-medium">Refund Policy</p>
          </Link>
          <Link to="/help#terms" className="p-4 border rounded-lg text-center hover:border-primary transition-colors">
            <p className="font-medium">Terms & Conditions</p>
          </Link>
          <Link to="/help#privacy" className="p-4 border rounded-lg text-center hover:border-primary transition-colors">
            <p className="font-medium">Privacy Policy</p>
          </Link>
          <Link to="/help#contact" className="p-4 border rounded-lg text-center hover:border-primary transition-colors">
            <p className="font-medium">Contact Us</p>
          </Link>
        </div>
      </div>
    </div>
  );
}
