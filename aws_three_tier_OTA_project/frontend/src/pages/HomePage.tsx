import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plane, Hotel, Train, Bus, Search, Star, Shield, Clock } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Card, CardContent } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';

export default function HomePage() {
  const [activeTab, setActiveTab] = useState<'flights' | 'hotels' | 'trains' | 'buses'>('flights');

  const tabs = [
    { id: 'flights', label: 'Flights', icon: Plane, href: '/flights' },
    { id: 'hotels', label: 'Hotels', icon: Hotel, href: '/hotels' },
    { id: 'trains', label: 'Trains', icon: Train, href: '/trains' },
    { id: 'buses', label: 'Buses', icon: Bus, href: '/buses' },
  ] as const;

  const popularDestinations = [
    { city: 'New York', country: 'USA', image: 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=400', price: 199 },
    { city: 'Paris', country: 'France', image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=400', price: 299 },
    { city: 'Tokyo', country: 'Japan', image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=400', price: 499 },
    { city: 'London', country: 'UK', image: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=400', price: 249 },
  ];

  const features = [
    { icon: Star, title: 'Best Prices', description: 'We guarantee the best prices on flights, hotels, and more.' },
    { icon: Shield, title: 'Secure Booking', description: 'Your payment and personal data are always protected.' },
    { icon: Clock, title: '24/7 Support', description: 'Our team is available around the clock to help you.' },
  ];

  return (
    <div className="flex flex-col">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-primary/10 via-background to-background py-20">
        <div className="container">
          <div className="text-center mb-10">
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
              Discover Your Next Adventure
            </h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Book flights, hotels, trains, and buses at the best prices. Your perfect trip is just a few clicks away.
            </p>
          </div>

          {/* Search Card */}
          <Card className="max-w-4xl mx-auto">
            <CardContent className="p-6">
              {/* Tabs */}
              <div className="flex space-x-2 mb-6 border-b">
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`flex items-center space-x-2 px-4 py-2 border-b-2 transition-colors ${
                        activeTab === tab.id
                          ? 'border-primary text-primary'
                          : 'border-transparent text-muted-foreground hover:text-foreground'
                      }`}
                    >
                      <Icon className="h-4 w-4" />
                      <span>{tab.label}</span>
                    </button>
                  );
                })}
              </div>

              {/* Search Form */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div>
                  <label className="text-sm font-medium mb-1 block">From</label>
                  <Input placeholder="City or Airport" />
                </div>
                <div>
                  <label className="text-sm font-medium mb-1 block">To</label>
                  <Input placeholder="City or Airport" />
                </div>
                <div>
                  <label className="text-sm font-medium mb-1 block">Date</label>
                  <Input type="date" />
                </div>
                <div className="flex items-end">
                  <Link to={`/${activeTab}`} className="w-full">
                    <Button className="w-full">
                      <Search className="h-4 w-4 mr-2" />
                      Search
                    </Button>
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-muted/40">
        <div className="container">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {features.map((feature, index) => {
              const Icon = feature.icon;
              return (
                <div key={index} className="flex flex-col items-center text-center p-6">
                  <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                    <Icon className="h-6 w-6 text-primary" />
                  </div>
                  <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
                  <p className="text-muted-foreground">{feature.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Popular Destinations */}
      <section className="py-16">
        <div className="container">
          <h2 className="text-3xl font-bold text-center mb-10">Popular Destinations</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {popularDestinations.map((destination, index) => (
              <Card key={index} className="overflow-hidden group cursor-pointer">
                <div className="relative h-48 overflow-hidden">
                  <img
                    src={destination.image}
                    alt={destination.city}
                    className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                  <div className="absolute bottom-4 left-4 text-white">
                    <h3 className="text-xl font-bold">{destination.city}</h3>
                    <p className="text-sm opacity-90">{destination.country}</p>
                  </div>
                </div>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Starting from</span>
                    <span className="text-lg font-bold text-primary">${destination.price}</span>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-primary text-primary-foreground">
        <div className="container text-center">
          <h2 className="text-3xl font-bold mb-4">Ready to Start Your Journey?</h2>
          <p className="text-xl opacity-90 mb-8 max-w-2xl mx-auto">
            Sign up now and get exclusive deals on your first booking!
          </p>
          <Link to="/register">
            <Button size="lg" variant="secondary">
              Get Started
            </Button>
          </Link>
        </div>
      </section>
    </div>
  );
}
