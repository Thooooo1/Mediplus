/* ═══════════════════════════════════════════════
   MediBook — Core JavaScript (app.js)
   ═══════════════════════════════════════════════ */

/* ─── Config ──────────────────────────────────── */
const API_BASE = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1" 
    ? "http://localhost:8080" 
    : "https://medibook-api-yd85.onrender.com"; // URL chính thức của Backend API trên Render
const APP_TIMEZONE = "Asia/Ho_Chi_Minh";
const APP_TIMEZONE_LABEL = "Giờ Việt Nam (GMT+7)";

/* ─── API Helper ──────────────────────────────── */
const API = async (path, opt = {}) => {
  const token = localStorage.getItem("token");
  const headers = Object.assign(
    { "Content-Type": "application/json" },
    opt.headers || {},
    token ? { Authorization: "Bearer " + token } : {},
  );
  try {
    const res = await fetch(API_BASE + path, { ...opt, headers });
    return res;
  } catch (err) {
    console.error("API Error:", err);
    throw err;
  }
};

/* ─── Date/Time Formatters ────────────────────── */
const fmt = (iso) =>
  new Date(iso).toLocaleString("vi-VN", { timeZone: APP_TIMEZONE });

const fmtTime = (iso) =>
  new Date(iso).toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    timeZone: APP_TIMEZONE,
  });

const fmtDate = (iso) =>
  new Date(iso).toLocaleDateString("vi-VN", { timeZone: APP_TIMEZONE });

/* ─── Auth Helpers ────────────────────────────── */
const requireAuth = async (expectedRole) => {
  const r = await API("/api/auth/me");
  if (!r.ok) {
    location.href = "login.html";
    return null;
  }
  const me = await r.json();
  if (expectedRole && me.role !== expectedRole) {
    showToast("Bạn không có quyền truy cập trang này.", "warning");
    setTimeout(() => { location.href = "index.html"; }, 1500);
    return null;
  }
  return me;
};

const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  location.href = "login.html";
};

/* ─── Navbar ──────────────────────────────────── */
const getNotifHtml = () => `
  <div class="nav-notif-container">
    <button class="notif-bell-btn" onclick="toggleNotifDropdown(event)">
      <span class="material-icons-round">notifications</span>
      <span id="notifBadge" class="notif-badge">0</span>
    </button>
    <div id="notifDropdown" class="notif-dropdown">
      <div class="notif-header">
        <h4>Thông báo</h4>
        <span class="material-icons-round" style="font-size:18px;color:var(--neutral-400)">notifications_active</span>
      </div>
      <div id="notifList" class="notif-list"></div>
    </div>
  </div>
`;

const renderNotifBell = (containerId) => {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = getNotifHtml();
        initNotifications();
    }
};

const setNav = (me) => {
  const nav = document.getElementById("nav") || document.getElementById("topNav");
  if (!nav) return;

  const notifHtml = me ? getNotifHtml() : '';

  // Nếu là trang index.html có style khác, ta sẽ giữ nguyên CSS nhưng thay đổi link
  const navLinks = document.getElementById("navLinks");
  if (navLinks) {
    if (me) {
      navLinks.innerHTML = `
        <a href="doctors.html" class="nav-link-ghost">Bác sĩ</a>
        ${me.role === 'USER' ? '<a href="my-appointments.html" class="nav-link-ghost">Lịch hẹn</a>' : ''}
        ${me.role === 'DOCTOR' ? '<a href="doctor-overview.html" class="nav-link-ghost">Dashboard</a>' : ''}
        ${me.role === 'ADMIN' ? '<a href="admin-dashboard.html" class="nav-link-ghost">Admin</a>' : ''}
        ${notifHtml}
        <a href="#" onclick="logout()" class="nav-link-outline" style="border-color:var(--primary);color:var(--primary)">Đăng xuất</a>
      `;
    } else {
      navLinks.innerHTML = `
        <a href="doctors.html" class="nav-link-ghost">Bác sĩ</a>
        <a href="login.html" class="nav-link-outline">Đăng nhập</a>
        <a href="register.html" class="nav-link-solid">Đăng ký miễn phí</a>
      `;
    }
    if (me) initNotifications();
    return;
  }

  // Cho các trang khác dùng ID nav chuẩn
  nav.innerHTML = `
    <div style="flex:1">
      <a href="index.html" style="text-decoration:none; color:inherit; display:flex; align-items:center; gap:8px">
        <span class="material-icons-round" style="color:var(--primary)">medical_services</span>
        <b style="font-size:20px">MediBook</b>
      </a>
    </div>
    <div style="display:flex; gap:16px; align-items:center">
      <a href="doctors.html" style="text-decoration:none; color:var(--neutral-600); font-weight:600; font-size:14px">Bác sĩ</a>
      ${me ? `
        ${me.role === "USER" ? `<a href="my-appointments.html" style="text-decoration:none; color:var(--neutral-600); font-weight:600; font-size:14px">Lịch hẹn</a>` : ""}
        ${me.role === "DOCTOR" ? `<a href="doctor-overview.html" style="text-decoration:none; color:var(--neutral-600); font-weight:600; font-size:14px">Dashboard</a>` : ""}
        ${me.role === "ADMIN" ? `<a href="admin-dashboard.html" style="text-decoration:none; color:var(--neutral-600); font-weight:600; font-size:14px">Admin</a>` : ""}
        ${notifHtml}
        <div style="width:1px; height:20px; background:var(--neutral-200)"></div>
        <div style="display:flex; flex-direction:column; align-items:flex-end; margin-right:8px">
          <span style="font-size:13px; font-weight:700; color:var(--neutral-900)">${me.fullName}</span>
        </div>
        <button class="btn-secondary btn-sm" onclick="logout()" style="padding:4px 12px; font-size:12px">Đăng xuất</button>
      ` : `
        <a href="login.html" class="btn-ghost btn-sm" style="font-weight:600">Đăng nhập</a>
        <a href="register.html" class="btn-primary btn-sm" style="font-weight:600">Đăng ký</a>
      `}
    </div>
  `;
  if (me) initNotifications();
};

/* --- Notifications --- */
let notifInterval = null;

const updateNotificationBadge = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;
    try {
        const r = await API("/api/notifications/unread-count");
        if (r.ok) {
            const count = await r.json();
            const badge = document.getElementById("notifBadge");
            if (badge) {
                const prevCount = parseInt(badge.getAttribute("data-count") || "0");
                badge.setAttribute("data-count", count);
                badge.textContent = count > 99 ? "99+" : count;
                const isShowing = count > 0;
                badge.classList.toggle("show", isShowing);
                if (!isShowing) badge.style.display = 'none';
                else badge.style.display = 'flex';
                
                // Alert if count increased (disabled as per user request)
                // if (count > prevCount && prevCount >= 0) {
                //     showToast("Bạn có thông báo mới!", "info");
                // }
            }
        }
    } catch(e) {}
};

const renderNotifications = async () => {
    const list = document.getElementById("notifList");
    if (!list) return;
    list.innerHTML = '<div class="p-6 text-center"><div class="loader-spinner" style="width:24px;height:24px;border-width:2px"></div></div>';
    
    try {
        const r = await API("/api/notifications?size=10");
        if (r.ok) {
            const data = await r.json();
                const unreadOnly = data.content.filter(n => !n.read);
                if (unreadOnly.length === 0) {
                    list.innerHTML = '<div class="notif-empty"><span class="material-icons-round">notifications_off</span><span>Không có thông báo mới</span></div>';
                } else {
                    list.innerHTML = unreadOnly.map(n => `
                        <div class="notif-item unread" onclick="markNotifAsRead('${n.id}', '${n.type}', '${n.relatedId}')">
                        <div class="notif-title">${n.title}</div>
                        <div class="notif-msg">${n.message}</div>
                        <div class="notif-time">${fmt(n.createdAt)}</div>
                    </div>
                `).join('');
            }
        }
    } catch(e) {
        list.innerHTML = '<div class="notif-empty">Lỗi tải thông báo</div>';
    }
};

const markNotifAsRead = async (id, type, relatedId) => {
    try {
        await API(`/api/notifications/${id}/read`, { method: 'POST' });
        updateNotificationBadge();
        // Redirect based on type if needed
        if (type === 'APPOINTMENT_BOOKED' || type === 'APPOINTMENT_CANCELLED' || type === 'APPOINTMENT_CONFIRMED') {
            const role = localStorage.getItem("role");
            if (role === 'DOCTOR') location.href = 'doctor-dashboard.html';
            else if (role === 'ADMIN') location.href = 'admin-appointments.html';
            else if (role === 'USER') location.href = 'my-appointments.html';
            else renderNotifications();
        } else {
            renderNotifications();
        }
    } catch(e) {}
};

const toggleNotifDropdown = (e) => {
    if (e) e.stopPropagation();
    const dropdown = document.getElementById("notifDropdown");
    if (dropdown) {
        const isShowing = dropdown.classList.toggle("show");
        if (isShowing) renderNotifications();
    }
};

const initNotifications = () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    // Close on outside click
    if (!window.notifInitialized) {
        document.addEventListener('click', (e) => {
            const dropdown = document.getElementById("notifDropdown");
            if (dropdown && !dropdown.contains(e.target) && !e.target.closest('.notif-bell-btn')) {
                dropdown.classList.remove("show");
            }
        });
        window.notifInitialized = true;
    }

    updateNotificationBadge();
    if (notifInterval) clearInterval(notifInterval);
    notifInterval = setInterval(updateNotificationBadge, 10000); // 10s (Faster response)
};

const initNav = async () => {
    const token = localStorage.getItem("token");
    
    const updateCta = (me) => {
        const ctaTitle = document.getElementById('ctaTitle');
        const ctaDesc = document.getElementById('ctaDesc');
        const ctaBtn = document.getElementById('ctaBtn');
        if (ctaTitle && ctaDesc && ctaBtn) {
            if (me) {
                ctaTitle.textContent = 'Đặt lịch khám chuyên khoa ngay';
                ctaDesc.innerHTML = 'Tìm bác sĩ và đặt lịch khám nhanh chóng.<br>Hệ thống xác nhận lịch tự động.';
                ctaBtn.innerHTML = '<span class="material-icons-round" style="font-size:20px">event_available</span> Đặt lịch ngay';
                ctaBtn.href = 'doctors.html';
            } else {
                ctaTitle.textContent = 'Sẵn sàng chăm sóc sức khỏe?';
                ctaDesc.innerHTML = 'Đăng ký miễn phí và đặt lịch với bác sĩ chuyên khoa<br>ngay hôm nay. Chỉ mất 30 giây.';
                ctaBtn.innerHTML = '<span class="material-icons-round" style="font-size:20px">rocket_launch</span> Bắt đầu ngay — Miễn phí';
                ctaBtn.href = 'register.html';
            }
        }
    };

    if (!token) {
        setNav(null);
        updateCta(null);
        return;
    }
    try {
        const r = await API("/api/auth/me");
        if (r.ok) {
            const me = await r.json();
            setNav(me);
            updateCta(me);
        } else {
            setNav(null);
            updateCta(null);
        }
    } catch(e) {
        setNav(null);
        updateCta(null);
    }
};

// Tự động chạy khi load file script
addEventListener('DOMContentLoaded', initNav);

/* ─── Status Badge Helper ─────────────────────── */
const statusBadge = (status) => {
  const map = {
    BOOKED:    { bg: 'var(--warning-light)',  color: 'var(--warning)',  label: 'Đã đặt' },
    CONFIRMED: { bg: 'var(--primary-light)',  color: 'var(--primary)',  label: 'Đã xác nhận' },
    COMPLETED: { bg: 'var(--success-light)',  color: 'var(--success)',  label: 'Hoàn thành' },
    CANCELLED: { bg: 'var(--danger-light)',   color: 'var(--danger)',   label: 'Đã hủy' },
    PENDING:   { bg: 'var(--warning-light)',  color: 'var(--warning)',  label: 'Chờ xử lý' },
    AVAILABLE: { bg: 'var(--success-light)',  color: 'var(--success)',  label: 'Trống' },
  };
  const s = map[status] || { bg: '#f1f5f9', color: '#64748b', label: status };
  return `<span class="status-badge" style="background:${s.bg};color:${s.color}">${s.label}</span>`;
};

/* ─── Loading Skeleton ────────────────────────── */
const showSkeleton = (container, count = 3) => {
  if (typeof container === 'string') container = document.getElementById(container);
  if (!container) return;
  let html = '';
  for (let i = 0; i < count; i++) {
    html += `<div class="skeleton-card"><div class="skeleton-line w-60"></div><div class="skeleton-line w-40"></div><div class="skeleton-line w-80"></div></div>`;
  }
  container.innerHTML = html;
};

/* ─── Empty State ─────────────────────────────── */
const showEmptyState = (container, icon, title, desc, ctaHtml = '') => {
  if (typeof container === 'string') container = document.getElementById(container);
  if (!container) return;
  container.innerHTML = `
    <div class="empty-state">
      <span class="material-icons-round" style="font-size:56px;color:var(--neutral-300)">${icon}</span>
      <h3 style="margin:12px 0 4px;color:var(--neutral-700)">${title}</h3>
      <p style="color:var(--neutral-500);margin-bottom:16px">${desc}</p>
      ${ctaHtml}
    </div>
  `;
};

/* ─── Error State ─────────────────────────────── */
const showErrorState = (container, message, retryFn) => {
  if (typeof container === 'string') container = document.getElementById(container);
  if (!container) return;
  container.innerHTML = `
    <div class="empty-state">
      <span class="material-icons-round" style="font-size:56px;color:var(--danger)">error_outline</span>
      <h3 style="margin:12px 0 4px;color:var(--neutral-700)">Đã xảy ra lỗi</h3>
      <p style="color:var(--neutral-500);margin-bottom:16px">${message}</p>
      ${retryFn ? `<button class="btn-primary" onclick="(${retryFn.toString()})()">Thử lại</button>` : ''}
    </div>
  `;
};

/* ─── Page Loader ─────────────────────────────── */
const showPageLoader = () => {
  if (document.getElementById("page-loader")) return;
  const loader = document.createElement("div");
  loader.id = "page-loader";
  loader.className = "page-loader";
  loader.innerHTML = `
    <div class="loader-content">
      <div class="loader-spinner"></div>
      <p>Đang tải dữ liệu...</p>
    </div>
  `;
  document.body.appendChild(loader);
};

const hidePageLoader = () => {
  const loader = document.getElementById("page-loader");
  if (loader) {
    loader.style.opacity = '0';
    setTimeout(() => loader.remove(), 300);
  }
};

/* ─── Toast Notifications ─────────────────────── */
const showToast = (message, type = "success", options = {}) => {
  const colorMap = {
    success: { bg: '#dcfce7', border: '#bbf7d0', color: '#166534', icon: 'check_circle' },
    error:   { bg: '#fee2e2', border: '#fecaca', color: '#991b1b', icon: 'error' },
    warning: { bg: '#fef3c7', border: '#fde68a', color: '#92400e', icon: 'warning' },
    info:    { bg: '#dbeafe', border: '#bfdbfe', color: '#1e40af', icon: 'info' },
  };
  const c = colorMap[type] || colorMap.info;

  const toast = document.createElement("div");
  toast.className = "toast-notification";
  toast.style.cssText = `background:${c.bg};border:1px solid ${c.border};color:${c.color}`;
  
  const iconHtml = options.noIcon ? '' : `<span class="material-icons-round" style="font-size:20px">${c.icon}</span>`;
  
  toast.innerHTML = `
    ${iconHtml}
    <p style="flex:1;margin:0;font-size:14px;font-weight:500">${message}</p>
    <button onclick="this.parentElement.remove()" style="background:none;border:none;cursor:pointer;opacity:0.6;color:inherit">
      <span class="material-icons-round" style="font-size:18px">close</span>
    </button>
  `;

  document.body.appendChild(toast);
  requestAnimationFrame(() => { toast.classList.add("show"); });
  setTimeout(() => {
    if (toast.parentElement) {
      toast.classList.add("hide");
      setTimeout(() => toast.remove(), 300);
    }
  }, 4000);
};

/* ─── Confirm Modal ───────────────────────────── */
const showConfirm = (message, { title = "Xác nhận", confirmText = "Đồng ý", cancelText = "Hủy", danger = false } = {}) => {
  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    const btnClass = danger ? "btn-danger" : "btn-primary";
    overlay.innerHTML = `
      <div class="modal-content">
        <h3 style="margin:0 0 8px;font-size:18px;font-weight:700;color:var(--neutral-900)">${title}</h3>
        <p style="color:var(--neutral-500);font-size:14px;line-height:1.6;margin:0 0 24px">${message}</p>
        <div style="display:flex;justify-content:flex-end;gap:12px">
          <button class="btn-secondary" id="confirm-cancel">${cancelText}</button>
          <button class="${btnClass}" id="confirm-ok">${confirmText}</button>
        </div>
      </div>
    `;
    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("show"));
    const close = (result) => {
      overlay.classList.remove("show");
      setTimeout(() => { overlay.remove(); resolve(result); }, 200);
    };
    overlay.querySelector("#confirm-cancel").onclick = () => close(false);
    overlay.querySelector("#confirm-ok").onclick = () => close(true);
    overlay.addEventListener("click", (e) => { if (e.target === overlay) close(false); });
  });
};

/* ─── Pagination Helper ───────────────────────── */
const renderPagination = (container, data, loadFn) => {
  if (!container) return;
  container.innerHTML = '';
  if (!data || data.totalPages <= 1) return;
  
  const div = document.createElement('div');
  div.className = 'pagination';
  
  const createBtn = (text, page, disabled, active) => {
    const b = document.createElement('button');
    b.innerHTML = text;
    if (disabled) b.disabled = true;
    if (active) b.className = 'active';
    if (!disabled && !active) b.onclick = () => loadFn(page);
    return b;
  };
  
  div.appendChild(createBtn('<span class="material-icons-round" style="font-size:18px">chevron_left</span>', data.number - 1, data.first, false));
  for (let i = 0; i < data.totalPages; i++) {
    const isEdge = i === 0 || i === data.totalPages - 1;
    const isNear = Math.abs(i - data.number) <= 2;
    if (isEdge || isNear) {
      div.appendChild(createBtn(i + 1, i, false, i === data.number));
    } else if (i === 1 || i === data.totalPages - 2) {
      const span = document.createElement('span');
      span.textContent = '...';
      span.style.padding = '0 8px';
      span.style.color = 'var(--neutral-400)';
      span.style.alignSelf = 'center';
      div.appendChild(span);
    }
  }
  div.appendChild(createBtn('<span class="material-icons-round" style="font-size:18px">chevron_right</span>', data.number + 1, data.last, false));
  
  container.appendChild(div);
};

