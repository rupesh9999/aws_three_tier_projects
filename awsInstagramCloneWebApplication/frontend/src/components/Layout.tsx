import { Outlet, Link } from 'react-router-dom';
import { Home, PlusSquare, User, Bell } from 'lucide-react';

const Layout = () => {
    return (
        <div className="flex flex-col min-h-screen bg-black text-white">
            <nav className="fixed top-0 w-full border-b border-gray-800 bg-black z-50 p-4 flex justify-between items-center">
                <h1 className="text-xl font-bold">Instagram Clone</h1>
                <div className="flex gap-4">
                    <Link to="/"><Home /></Link>
                    <Link to="/create"><PlusSquare /></Link>
                    <Link to="/notifications"><Bell /></Link>
                    {/* TODO: Replace with dynamic user ID */}
                    <Link to="/profile/me"><User /></Link>
                </div>
            </nav>
            <main className="flex-grow pt-16 pb-20 px-4">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
