import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router';

/**
 * Home page - Simple landing page
 */
export default function Home() {
  const { authenticated } = useAuth();
  const navigate = useNavigate();

  const handleGetStarted = () => {
    if (authenticated) {
      navigate('/dashboard');
    } else {
      navigate('/signin');
    }
  };

  return (
    <div className="bg-[var(--bg)] text-[var(--text)] flex items-center justify-center" style={{ minHeight: 'calc(100vh - 4rem)' }}>
      <div className="text-center space-y-8 px-4">
        <h1 className="text-6xl font-bold bg-gradient-to-r from-[var(--text)] to-[var(--accent)] bg-clip-text text-transparent">
          Git Gud
        </h1>
        <p className="text-xl text-[var(--text-muted)] max-w-md mx-auto">
          Level up your Java Spring skills through gamified learning
        </p>
        <button
          onClick={handleGetStarted}
          className="px-8 h-12 rounded-md bg-transparent text-[var(--accent)]
                   border-2 border-[var(--accent)] hover:bg-[var(--accent)]
                   hover:text-[var(--bg)] font-medium transition-colors text-lg"
        >
          {authenticated ? 'Go to Dashboard' : 'Get Started'}
        </button>
      </div>
    </div>
  );
}
