import axios from 'axios';
import { getCurrentUser } from './auth.service';

const API_URL = 'http://localhost:8080/api/v1/posts';

export const createPost = async (caption: string, imageFile: File) => {
    const user = getCurrentUser();
    if (!user || !user.userId) {
        throw new Error('User not logged in');
    }

    const formData = new FormData();
    formData.append('userId', user.userId.toString());
    formData.append('caption', caption);
    formData.append('image', imageFile);

    const response = await axios.post(API_URL, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
};

export const getUserPosts = async (userId: number) => {
    const response = await axios.get(`${API_URL}/user/${userId}`);
    return response.data;
};
