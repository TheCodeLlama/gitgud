package com.syntaxllama.gitgud.backend.model;

/**
 * Enum representing preset avatar options for users.
 * Each avatar has an identifier that maps to an avatar image.
 */
public enum Avatar {
    CODER_1("/avatars/coder-1.png", "Classic Coder"),
    CODER_2("/avatars/coder-2.png", "Coffee Coder"),
    NINJA_1("/avatars/ninja-1.png", "Code Ninja"),
    NINJA_2("/avatars/ninja-2.png", "Stealth Ninja"),
    ROBOT_1("/avatars/robot-1.png", "Friendly Robot"),
    ROBOT_2("/avatars/robot-2.png", "Tech Robot"),
    WIZARD("/avatars/wizard.png", "Code Wizard"),
    WARRIOR("/avatars/warrior.png", "Code Warrior"),
    EXPLORER("/avatars/explorer.png", "Code Explorer"),
    MASTER("/avatars/master.png", "Code Master");

    private final String url;
    private final String displayName;

    Avatar(String url, String displayName) {
        this.url = url;
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get an Avatar by its URL.
     *
     * @param url The avatar URL
     * @return The Avatar enum, or null if not found
     */
    public static Avatar fromUrl(String url) {
        if (url == null) {
            return null;
        }
        for (Avatar avatar : values()) {
            if (avatar.url.equals(url)) {
                return avatar;
            }
        }
        return null;
    }

    /**
     * Check if a given URL is a valid preset avatar.
     *
     * @param url The URL to check
     * @return true if it's a valid preset avatar
     */
    public static boolean isValidAvatar(String url) {
        return fromUrl(url) != null;
    }
}
