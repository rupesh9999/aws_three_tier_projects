import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost } from '../services/post.service';
import { Upload } from 'lucide-react';

const CreatePost = () => {
    const [caption, setCaption] = useState('');
    const [image, setImage] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setImage(file);
            setPreview(URL.createObjectURL(file));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!image) {
            setError('Please select an image');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await createPost(caption, image);
            navigate('/');
        } catch (err) {
            console.error(err);
            setError('Failed to create post');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto bg-black text-white p-4">
            <h1 className="text-2xl font-bold mb-6">Create New Post</h1>

            <form onSubmit={handleSubmit} className="flex flex-col gap-6">
                {/* Image Upload Area */}
                <div className="w-full aspect-square bg-gray-900 border-2 border-dashed border-gray-700 rounded-lg flex items-center justify-center overflow-hidden relative">
                    {preview ? (
                        <img src={preview} alt="Preview" className="w-full h-full object-cover" />
                    ) : (
                        <div className="text-center p-4">
                            <Upload className="mx-auto h-12 w-12 text-gray-500 mb-2" />
                            <p className="text-gray-400">Click to upload photo</p>
                        </div>
                    )}
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageChange}
                        className="absolute inset-0 opacity-0 cursor-pointer"
                    />
                </div>

                {/* Caption Input */}
                <div>
                    <textarea
                        placeholder="Write a caption..."
                        value={caption}
                        onChange={(e) => setCaption(e.target.value)}
                        className="w-full bg-gray-900 border border-gray-700 rounded p-3 text-white focus:outline-none focus:border-gray-500 min-h-[100px]"
                    />
                </div>

                {error && <p className="text-red-500 text-sm">{error}</p>}

                <button
                    type="submit"
                    disabled={loading || !image}
                    className={`bg-blue-500 text-white font-bold py-3 rounded hover:bg-blue-600 transition ${(loading || !image) ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                >
                    {loading ? 'Sharing...' : 'Share'}
                </button>
            </form>
        </div>
    );
};

export default CreatePost;
