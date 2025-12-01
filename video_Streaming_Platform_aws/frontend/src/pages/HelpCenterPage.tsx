import { useState } from 'react';
import { HiSearch, HiChevronDown, HiMail, HiPhone, HiChat } from 'react-icons/hi';
import { Button } from '@components/common';

const faqCategories = [
  {
    name: 'Getting Started',
    questions: [
      {
        q: 'How do I sign up for StreamFlix?',
        a: 'Visit our website and click "Get Started". Enter your email address and follow the prompts to create your account and choose a subscription plan.',
      },
      {
        q: 'What devices can I use to watch StreamFlix?',
        a: 'StreamFlix is available on smart TVs, smartphones, tablets, computers, and streaming devices like Roku, Fire TV, Apple TV, and gaming consoles.',
      },
      {
        q: 'How many screens can I watch on at once?',
        a: 'The number of screens depends on your plan. Basic: 1 screen, Standard: 2 screens, Premium: 4 screens.',
      },
    ],
  },
  {
    name: 'Billing & Payments',
    questions: [
      {
        q: 'How do I change my payment method?',
        a: 'Go to Account Settings > Payment & Billing > Manage Payment Methods to add, remove, or update your payment information.',
      },
      {
        q: 'Can I cancel my subscription anytime?',
        a: 'Yes, you can cancel anytime. You will continue to have access until the end of your current billing period.',
      },
    ],
  },
  {
    name: 'Playback Issues',
    questions: [
      {
        q: 'Why is my video buffering?',
        a: 'Buffering is usually caused by slow internet connection. Try: restarting your router, closing other apps, moving closer to your WiFi router, or reducing video quality.',
      },
      {
        q: 'How do I change video quality?',
        a: 'While watching, click the settings icon and select your preferred quality. You can also set a default quality in your profile settings.',
      },
    ],
  },
  {
    name: 'Account & Profiles',
    questions: [
      {
        q: 'How many profiles can I create?',
        a: 'You can create up to 5 profiles per account, allowing different family members to have personalized recommendations.',
      },
      {
        q: 'How do I set up a Kids profile?',
        a: 'When creating a profile, toggle the "Kid Profile" option. This restricts content to age-appropriate titles only.',
      },
    ],
  },
];

export default function HelpCenterPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedQuestion, setExpandedQuestion] = useState<string | null>(null);

  const filteredFaqs = faqCategories
    .map((category) => ({
      ...category,
      questions: category.questions.filter(
        (q) =>
          q.q.toLowerCase().includes(searchQuery.toLowerCase()) ||
          q.a.toLowerCase().includes(searchQuery.toLowerCase())
      ),
    }))
    .filter((category) => category.questions.length > 0);

  return (
    <div className="min-h-screen pt-20 px-4 md:px-12">
      <div className="max-w-4xl mx-auto">
        {/* Hero */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-white mb-4">Help Center</h1>
          <p className="text-gray-400 mb-6">
            Find answers to common questions or contact our support team
          </p>

          {/* Search */}
          <div className="relative max-w-xl mx-auto">
            <HiSearch className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search for help..."
              className="input pl-12"
            />
          </div>
        </div>

        {/* Contact Options */}
        <div className="grid md:grid-cols-3 gap-4 mb-12">
          <div className="bg-dark-400 rounded-lg p-6 text-center">
            <HiChat className="w-10 h-10 text-primary-500 mx-auto mb-3" />
            <h3 className="font-semibold text-white mb-2">Live Chat</h3>
            <p className="text-sm text-gray-400 mb-4">
              Chat with our support team
            </p>
            <Button variant="secondary" size="sm">
              Start Chat
            </Button>
          </div>

          <div className="bg-dark-400 rounded-lg p-6 text-center">
            <HiPhone className="w-10 h-10 text-primary-500 mx-auto mb-3" />
            <h3 className="font-semibold text-white mb-2">Phone Support</h3>
            <p className="text-sm text-gray-400 mb-4">
              1-800-STREAMFLIX
            </p>
            <Button variant="secondary" size="sm">
              Call Now
            </Button>
          </div>

          <div className="bg-dark-400 rounded-lg p-6 text-center">
            <HiMail className="w-10 h-10 text-primary-500 mx-auto mb-3" />
            <h3 className="font-semibold text-white mb-2">Email Support</h3>
            <p className="text-sm text-gray-400 mb-4">
              Get help via email
            </p>
            <Button variant="secondary" size="sm">
              Send Email
            </Button>
          </div>
        </div>

        {/* FAQs */}
        <div className="space-y-8">
          {filteredFaqs.map((category) => (
            <div key={category.name}>
              <h2 className="text-xl font-semibold text-white mb-4">
                {category.name}
              </h2>
              <div className="space-y-2">
                {category.questions.map((faq) => (
                  <div
                    key={faq.q}
                    className="bg-dark-400 rounded-lg overflow-hidden"
                  >
                    <button
                      onClick={() =>
                        setExpandedQuestion(
                          expandedQuestion === faq.q ? null : faq.q
                        )
                      }
                      className="w-full flex items-center justify-between p-4 text-left"
                    >
                      <span className="font-medium text-white">{faq.q}</span>
                      <HiChevronDown
                        className={`w-5 h-5 text-gray-400 transition-transform ${
                          expandedQuestion === faq.q ? 'rotate-180' : ''
                        }`}
                      />
                    </button>
                    {expandedQuestion === faq.q && (
                      <div className="px-4 pb-4 text-gray-300">{faq.a}</div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* No Results */}
        {searchQuery && filteredFaqs.length === 0 && (
          <div className="text-center py-12">
            <p className="text-xl text-gray-400">
              No results found for "{searchQuery}"
            </p>
            <p className="text-gray-500 mt-2">
              Try different keywords or contact our support team
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
