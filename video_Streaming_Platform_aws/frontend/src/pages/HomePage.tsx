import { Link } from 'react-router-dom';
import { HiPlay, HiDesktopComputer, HiDownload, HiUserGroup } from 'react-icons/hi';
import { Button } from '@components/common';

const features = [
  {
    icon: HiDesktopComputer,
    title: 'Watch everywhere',
    description:
      'Stream unlimited movies and TV shows on your phone, tablet, laptop, and TV.',
  },
  {
    icon: HiDownload,
    title: 'Download your shows',
    description:
      'Save your favorites easily and always have something to watch offline.',
  },
  {
    icon: HiUserGroup,
    title: 'Create profiles for kids',
    description:
      'Send kids on adventures with their favorite characters in a space made just for them—free with your membership.',
  },
];

const faqs = [
  {
    question: 'What is StreamFlix?',
    answer:
      'StreamFlix is a streaming service that offers a wide variety of award-winning TV shows, movies, anime, documentaries, and more on thousands of internet-connected devices.',
  },
  {
    question: 'How much does StreamFlix cost?',
    answer:
      'Watch StreamFlix on your smartphone, tablet, Smart TV, laptop, or streaming device, all for one fixed monthly fee. Plans range from $9.99 to $19.99 a month.',
  },
  {
    question: 'Where can I watch?',
    answer:
      'Watch anywhere, anytime. Sign in with your StreamFlix account to watch instantly on the web at streamflix.com from your personal computer or on any internet-connected device.',
  },
  {
    question: 'How do I cancel?',
    answer:
      'StreamFlix is flexible. There are no pesky contracts and no commitments. You can easily cancel your account online in two clicks.',
  },
];

export default function HomePage() {
  return (
    <div className="min-h-screen bg-dark-500">
      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center px-4">
        {/* Background */}
        <div
          className="absolute inset-0 bg-cover bg-center"
          style={{
            backgroundImage: `url('https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1920')`,
          }}
        />
        <div className="absolute inset-0 bg-black/70" />
        <div className="absolute inset-0 bg-gradient-to-t from-dark-500 via-transparent to-dark-500/50" />

        {/* Header */}
        <header className="absolute top-0 left-0 right-0 flex items-center justify-between px-4 md:px-12 py-6 z-10">
          <h1 className="text-3xl md:text-4xl font-bold text-primary-500">
            STREAMFLIX
          </h1>
          <Link to="/login">
            <Button size="sm">Sign In</Button>
          </Link>
        </header>

        {/* Content */}
        <div className="relative z-10 max-w-3xl text-center">
          <h2 className="text-4xl md:text-6xl font-bold text-white mb-4">
            Unlimited movies, TV shows, and more
          </h2>
          <p className="text-xl md:text-2xl text-gray-200 mb-6">
            Watch anywhere. Cancel anytime.
          </p>
          <p className="text-lg text-gray-300 mb-8">
            Ready to watch? Enter your email to create or restart your membership.
          </p>

          {/* Email Form */}
          <div className="flex flex-col md:flex-row gap-4 justify-center">
            <input
              type="email"
              placeholder="Email address"
              className="input max-w-md"
            />
            <Link to="/register">
              <Button
                size="lg"
                rightIcon={<HiPlay className="w-6 h-6" />}
                className="whitespace-nowrap"
              >
                Get Started
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4 md:px-12 border-t-8 border-dark-400">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-white text-center mb-12">
            More reasons to join
          </h2>

          <div className="grid md:grid-cols-3 gap-8">
            {features.map((feature) => (
              <div
                key={feature.title}
                className="bg-gradient-to-br from-dark-300 to-dark-400 rounded-lg p-8 text-center"
              >
                <feature.icon className="w-12 h-12 text-primary-500 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-white mb-3">
                  {feature.title}
                </h3>
                <p className="text-gray-400">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="py-20 px-4 md:px-12 border-t-8 border-dark-400">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-white text-center mb-12">
            Frequently Asked Questions
          </h2>

          <div className="space-y-2">
            {faqs.map((faq) => (
              <details
                key={faq.question}
                className="group bg-dark-300 rounded"
              >
                <summary className="flex items-center justify-between p-6 cursor-pointer text-lg font-medium text-white">
                  {faq.question}
                  <span className="text-3xl transition-transform group-open:rotate-45">
                    +
                  </span>
                </summary>
                <div className="px-6 pb-6 text-gray-300">{faq.answer}</div>
              </details>
            ))}
          </div>

          {/* CTA */}
          <div className="text-center mt-12">
            <p className="text-lg text-gray-300 mb-6">
              Ready to watch? Enter your email to create or restart your membership.
            </p>
            <Link to="/register">
              <Button
                size="lg"
                rightIcon={<HiPlay className="w-6 h-6" />}
              >
                Get Started
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 px-4 md:px-12 border-t-8 border-dark-400 text-gray-500">
        <div className="max-w-6xl mx-auto">
          <p className="mb-4">Questions? Call 1-800-STREAMFLIX</p>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <a href="#" className="hover:underline">FAQ</a>
            <a href="#" className="hover:underline">Help Center</a>
            <a href="#" className="hover:underline">Account</a>
            <a href="#" className="hover:underline">Media Center</a>
            <a href="#" className="hover:underline">Investor Relations</a>
            <a href="#" className="hover:underline">Jobs</a>
            <a href="#" className="hover:underline">Ways to Watch</a>
            <a href="#" className="hover:underline">Terms of Use</a>
            <a href="#" className="hover:underline">Privacy</a>
            <a href="#" className="hover:underline">Cookie Preferences</a>
            <a href="#" className="hover:underline">Corporate Information</a>
            <a href="#" className="hover:underline">Contact Us</a>
          </div>
          <p className="mt-8 text-sm">© 2024 StreamFlix, Inc.</p>
        </div>
      </footer>
    </div>
  );
}
