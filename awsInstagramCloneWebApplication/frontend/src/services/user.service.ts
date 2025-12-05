import axios from 'axios';
import { getCurrentUser } from './auth.service';

const API_URL = 'http://localhost:8080/api/v1/users';

export interface UserProfile {
    userId: number;
    username: string;
    fullName: string;
    bio: string;
    profilePictureUrl: string;
    followersCount: number;
    followingCount: number;
    postsCount: number;
}

export const getUserProfile = async (userId: string) => {
    const response = await axios.get<UserProfile>(`${API_URL}/${userId}`);
    return response.data;
};

export const getMyProfile = async () => {
    const user = getCurrentUser();
    if (!user || !user.userId) {
        throw new Error('User not logged in');
    }
    return getUserProfile(user.userId.toString());
};

export const followUser = async (userId: number) => {
    const currentUser = getCurrentUser();
    if (!currentUser || !currentUser.userId) {
        throw new Error('User not logged in');
    }
    await axios.post(`${API_URL}/${currentUser.userId}/follow/${userId}`);
}

export const unfollowUser = async (userId: number) => {
    const currentUser = getCurrentUser();
    if (!currentUser || !currentUser.userId) {
        throw new Error('User not logged in');
    }
    await axios.post(`${API_URL}/${currentUser.userId}/unfollow/${userId}`);
}
