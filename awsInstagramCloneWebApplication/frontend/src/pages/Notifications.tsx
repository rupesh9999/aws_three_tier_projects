import { useEffect, useState } from 'react';
import { getUserNotifications, type Notification } from '../services/notification.service';
import { Heart, MessageCircle, UserPlus } from 'lucide-react';

const Notifications = () => {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchNotifications = async () => {
            try {
                const data = await getUserNotifications();
                setNotifications(data);
            } catch (err) {
                console.error(err);
                setError('Failed to load notifications');
            } finally {
                setLoading(false);
            }
        };

        fetchNotifications();
    }, []);

    if (loading) return <div className="text-center mt-10">Loading...</div>;
    if (error) return <div className="text-center mt-10 text-red-500">{error}</div>;

    const getIcon = (type: string) => {
        switch (type) {
            case 'LIKE': return <Heart className="text-red-500" />;
            case 'COMMENT': return <MessageCircle className="text-blue-500" />;
            case 'FOLLOW': return <UserPlus className="text-green-500" />;
            default: return <Heart />;
        }
    };

    return (
        <div className="max-w-md mx-auto">
            <h1 className="text-2xl font-bold mb-6 px-4">Notifications</h1>
            <div className="flex flex-col gap-4 px-4">
                {notifications.length === 0 ? (
                    <p className="text-center text-gray-500">No notifications yet.</p>
                ) : (
                    notifications.map((notification) => (
                        <div key={notification.id} className="flex items-center gap-4 bg-gray-900 p-4 rounded-lg">
                            <div className="text-2xl">
                                {getIcon(notification.type)}
                            </div>
                            <div>
                                <p className="text-sm">{notification.message}</p>
                                <p className="text-xs text-gray-500 mt-1">
                                    {new Date(notification.createdAt).toLocaleDateString()}
                                </p>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default Notifications;
