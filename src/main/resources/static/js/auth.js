// Local Service Finder - Auth Handler

// Dynamically select API URL (localhost vs live production backend)
const API_BASE = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
    ? ""
    : "https://lcf-aryan.onrender.com";

// Check if authenticated
function isLoggedIn() {
    return localStorage.getItem("ls_token") !== null;
}

// Get Token
function getToken() {
    return localStorage.getItem("ls_token");
}

// Get User
function getUser() {
    const userStr = localStorage.getItem("ls_user");
    return userStr ? JSON.parse(userStr) : null;
}

// Set Session
function setSession(token, user) {
    localStorage.setItem("ls_token", token);
    localStorage.setItem("ls_user", JSON.stringify(user));
}

// Logout
function logout() {
    localStorage.removeItem("ls_token");
    localStorage.removeItem("ls_user");
    window.location.href = "index.html";
}

// Generate Auth Headers
function getHeaders() {
    const token = getToken();
    const headers = {
        "Content-Type": "application/json"
    };
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }
    return headers;
}

// Route Guard to verify role access
function guardRoute(requiredRole) {
    if (!isLoggedIn()) {
        window.location.href = "login.html?redirect=" + encodeURIComponent(window.location.pathname);
        return;
    }
    const user = getUser();
    if (!user || (requiredRole && user.role !== requiredRole)) {
        // Dynamic route correction
        if (user.role === "ADMIN") {
            window.location.href = "admin-dashboard.html";
        } else if (user.role === "PROVIDER") {
            window.location.href = "provider-dashboard.html";
        } else {
            window.location.href = "user-dashboard.html";
        }
    }
}

// Set up UI components dynamically depending on user session
document.addEventListener("DOMContentLoaded", function() {
    const navAuthSection = document.getElementById("navAuthSection");
    if (!navAuthSection) return;

    if (isLoggedIn()) {
        const user = getUser();
        let dashboardUrl = "user-dashboard.html";
        if (user.role === "PROVIDER") dashboardUrl = "provider-dashboard.html";
        else if (user.role === "ADMIN") dashboardUrl = "admin-dashboard.html";

        navAuthSection.innerHTML = `
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-person-circle fs-5 me-2 text-primary"></i>
                    <span>${user.name}</span>
                </a>
                <ul class="dropdown-menu dropdown-menu-end border-0 shadow-md" aria-labelledby="navbarDropdown">
                    <li><a class="dropdown-item" href="${dashboardUrl}"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="#" onclick="logout(); return false;"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                </ul>
            </li>
        `;
    } else {
        navAuthSection.innerHTML = `
            <li class="nav-item">
                <a class="btn btn-outline-primary me-2 px-4" href="login.html">Login</a>
            </li>
            <li class="nav-item">
                <a class="btn btn-primary px-4" href="register.html">Sign Up</a>
            </li>
        `;
    }
});
