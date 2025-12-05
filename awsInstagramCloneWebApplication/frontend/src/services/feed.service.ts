import axios from 'axios';
import { getCurrentUser } from './auth.service';

const API_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/feed`;

export interface PostSummary {
    id: number;
    userId: number;
    username: string;
    userProfilePicture: string;
    imageUrl: string;
    caption: string;
    likesCount: number;
    commentsCount: number;
    createdAt: string;
}

export interface FeedResponse {
    posts: PostSummary[];
}

export const getUserFeed = async () => {
    const user = getCurrentUser();
    if (!user || !user.userId) {
        throw new Error('User not logged in');
    }
    const response = await axios.get<FeedResponse>(`${API_URL}/${user.userId}`);
    return response.data;
};
