import { useEffect, useState } from 'react';
import { getUserFeed, type PostSummary } from '../services/feed.service';
import Post from '../components/Post';

const Feed = () => {
    const [posts, setPosts] = useState<PostSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchFeed = async () => {
            try {
                const data = await getUserFeed();
                setPosts(data.posts);
            } catch (err) {
                console.error(err);
                setError('Failed to load feed');
            } finally {
                setLoading(false);
            }
        };

        fetchFeed();
    }, []);

    if (loading) return <div className="text-center mt-10">Loading...</div>;
    if (error) return <div className="text-center mt-10 text-red-500">{error}</div>;

    return (
        <div className="pb-10">
            {posts.map((post) => (
                <Post key={post.id} post={post} />
            ))}
        </div>
    );
};

export default Feed;
