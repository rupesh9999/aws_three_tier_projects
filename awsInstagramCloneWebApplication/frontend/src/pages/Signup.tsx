import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../services/auth.service';

const Signup = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [fullName, setFullName] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSignup = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await register(username, email, password, fullName);
            navigate('/login');
        } catch (err) {
            setError('Registration failed. Please try again.');
            console.error(err);
        }
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-black text-white">
            <div className="w-full max-w-xs border border-gray-800 p-8 rounded-lg">
                <h1 className="text-3xl font-bold mb-4 text-center">Instagram</h1>
                <p className="text-gray-400 text-center font-bold mb-6">Sign up to see photos and videos from your friends.</p>
                <form onSubmit={handleSignup} className="flex flex-col gap-4">
                    <input
                        type="text"
                        placeholder="Mobile Number or Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="bg-gray-900 border border-gray-700 rounded p-2 text-white focus:outline-none focus:border-gray-500"
                    />
                    <input
                        type="text"
                        placeholder="Full Name"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        className="bg-gray-900 border border-gray-700 rounded p-2 text-white focus:outline-none focus:border-gray-500"
                    />
                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="bg-gray-900 border border-gray-700 rounded p-2 text-white focus:outline-none focus:border-gray-500"
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="bg-gray-900 border border-gray-700 rounded p-2 text-white focus:outline-none focus:border-gray-500"
                    />
                    {error && <p className="text-red-500 text-sm text-center">{error}</p>}
                    <button type="submit" className="bg-blue-500 text-white font-bold py-2 rounded hover:bg-blue-600 transition">
                        Sign Up
                    </button>
                </form>
                <div className="mt-6 text-center text-sm">
                    <p className="text-gray-400">
                        Have an account?{' '}
                        <Link to="/login" className="text-blue-500 font-bold">
                            Log in
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Signup;
