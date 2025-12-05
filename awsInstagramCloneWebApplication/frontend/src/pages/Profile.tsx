import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getUserProfile, type UserProfile } from '../services/user.service';
import { getUserPosts } from '../services/post.service';
import type { PostSummary } from '../services/feed.service';

const Profile = () => {
    const { userId } = useParams();
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [posts, setPosts] = useState<PostSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                if (userId) {
                    const profileData = userId === 'me'
                        ? await import('../services/user.service').then(m => m.getMyProfile())
                        : await getUserProfile(userId);

                    setProfile(profileData);

                    // Fetch user posts
                    const postsData = await getUserPosts(profileData.userId);
                    setPosts(postsData);
                }
            } catch (err) {
                console.error(err);
                setError('Failed to load profile');
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, [userId]);

    if (loading) return <div className="text-center mt-10">Loading...</div>;
    if (error) return <div className="text-center mt-10 text-red-500">{error}</div>;
    if (!profile) return <div className="text-center mt-10">User not found</div>;

    return (
        <div className="max-w-4xl mx-auto">
            {/* Profile Header */}
            <div className="flex flex-col md:flex-row items-center gap-8 mb-12 px-4">
                <div className="w-32 h-32 md:w-40 md:h-40 rounded-full bg-gray-700 overflow-hidden flex-shrink-0">
                    {profile.profilePictureUrl ? (
                        <img src={profile.profilePictureUrl} alt={profile.username} className="w-full h-full object-cover" />
                    ) : (
                        <div className="w-full h-full bg-gray-600" />
                    )}
                </div>

                <div className="flex-grow text-center md:text-left">
                    <div className="flex flex-col md:flex-row items-center gap-4 mb-4">
                        <h1 className="text-2xl font-light">{profile.username}</h1>
                        {userId === 'me' && (
                            <button className="bg-gray-800 px-4 py-1.5 rounded font-bold text-sm hover:bg-gray-700">
                                Edit Profile
                            </button>
                        )}
                    </div>

                    <div className="flex justify-center md:justify-start gap-8 mb-4">
                        <span><span className="font-bold">{profile.postsCount}</span> posts</span>
                        <span><span className="font-bold">{profile.followersCount}</span> followers</span>
                        <span><span className="font-bold">{profile.followingCount}</span> following</span>
                    </div>

                    <div>
                        <div className="font-bold">{profile.fullName}</div>
                        <div className="whitespace-pre-wrap">{profile.bio}</div>
                    </div>
                </div>
            </div>

            {/* Posts Grid */}
            <div className="grid grid-cols-3 gap-1 md:gap-8">
                {posts.map((post) => (
                    <div key={post.id} className="aspect-square bg-gray-900 relative group cursor-pointer">
                        <img src={post.imageUrl} alt={post.caption} className="w-full h-full object-cover" />
                        <div className="absolute inset-0 bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-6 text-white font-bold">
                            <span>❤️ {post.likesCount}</span>
                            <span>💬 {post.commentsCount}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Profile;
