import React from 'react';
import { Heart, MessageCircle, Send, Bookmark } from 'lucide-react';
import type { PostSummary } from '../services/feed.service';

interface PostProps {
    post: PostSummary;
}

const Post: React.FC<PostProps> = ({ post }) => {
    return (
        <div className="bg-black border border-gray-800 rounded mb-6 max-w-md mx-auto">
            {/* Header */}
            <div className="flex items-center p-3">
                <div className="w-8 h-8 rounded-full bg-gray-700 mr-3 overflow-hidden">
                    {post.userProfilePicture ? (
                        <img src={post.userProfilePicture} alt={post.username} className="w-full h-full object-cover" />
                    ) : (
                        <div className="w-full h-full bg-gray-500" />
                    )}
                </div>
                <span className="font-bold text-sm">{post.username}</span>
            </div>

            {/* Image */}
            <div className="w-full aspect-square bg-gray-900">
                <img src={post.imageUrl} alt={post.caption} className="w-full h-full object-cover" />
            </div>

            {/* Actions */}
            <div className="p-3">
                <div className="flex justify-between mb-2">
                    <div className="flex gap-4">
                        <Heart className="cursor-pointer hover:text-gray-400" />
                        <MessageCircle className="cursor-pointer hover:text-gray-400" />
                        <Send className="cursor-pointer hover:text-gray-400" />
                    </div>
                    <Bookmark className="cursor-pointer hover:text-gray-400" />
                </div>
                <div className="font-bold text-sm mb-1">{post.likesCount} likes</div>
                <div>
                    <span className="font-bold text-sm mr-2">{post.username}</span>
                    <span className="text-sm">{post.caption}</span>
                </div>
                <div className="text-gray-500 text-xs mt-1 uppercase">
                    {new Date(post.createdAt).toLocaleDateString()}
                </div>
            </div>
        </div>
    );
};

export default Post;
