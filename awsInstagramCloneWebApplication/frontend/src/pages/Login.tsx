import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../services/auth.service';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await login(username, password);
            navigate('/');
        } catch (err) {
            setError('Invalid username or password');
            console.error(err);
        }
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-black text-white">
            <div className="w-full max-w-xs border border-gray-800 p-8 rounded-lg">
                <h1 className="text-3xl font-bold mb-8 text-center">Instagram</h1>
                <form onSubmit={handleLogin} className="flex flex-col gap-4">
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
                        Log In
                    </button>
                </form>
                <div className="mt-6 text-center text-sm">
                    <p className="text-gray-400">
                        Don't have an account?{' '}
                        <Link to="/signup" className="text-blue-500 font-bold">
                            Sign up
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;
