import axios from 'axios';
import { getCurrentUser } from './auth.service';

const API_URL = 'http://localhost:8080/api/v1/notifications';

export interface Notification {
    id: number;
    message: string;
    type: string;
    relatedEntityId: number;
    isRead: boolean;
    createdAt: string;
}

export const getUserNotifications = async () => {
    const user = getCurrentUser();
    if (!user || !user.userId) {
        throw new Error('User not logged in');
    }
    const response = await axios.get<Notification[]>(`${API_URL}/${user.userId}`);
    return response.data;
};
