

// Add response interceptor to handle errors and token refresh
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
            if (!originalRequest._retry) {
                originalRequest._retry = true;

                try {
                    // Try to refresh the token
                    let refreshResponse;

                    if (isNgrokEnvironment()) {
                        // For ngrok, use refresh token from localStorage
                        const refreshToken = localStorage.getItem('refreshToken');
