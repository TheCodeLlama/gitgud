const Avatar = ({ user }) => {
  return (
    <>
      {user.avatarUrl ? (
         <img src={user.avatarUrl} alt={user.username} className="w-24 h-24 rounded-full object-cover" />
       ) : (
         <span className="text-3xl font-bold">
          {user.username?.charAt(0) || 'U'}
        </span>
      )}
    </>
  );
};

export default Avatar;