/* ═══════════════════════════════════════════════
   MediBook — Core JavaScript (app.js)
   ═══════════════════════════════════════════════ */

/* ─── Config ──────────────────────────────────── */
const API_BASE = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1" 
    ? "http://localhost:8080" 
    : "https://medibook-api.onrender.com"; // URL chính thức của Backend API trên Render
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
const setNav = (me) => {
  const nav = document.getElementById("nav") || document.getElementById("topNav");
  if (!nav) return;

  // Nếu là trang index.html có style khác, ta sẽ giữ nguyên CSS nhưng thay đổi link
  const navLinks = document.getElementById("navLinks");
  if (navLinks) {
    if (me) {
      navLinks.innerHTML = `
        <a href="doctors.html" class="nav-link-ghost">Bác sĩ</a>
        ${me.role === 'USER' ? '<a href="my-appointments.html" class="nav-link-ghost">Lịch hẹn</a>' : ''}
        ${me.role === 'DOCTOR' ? '<a href="doctor-overview.html" class="nav-link-ghost">Dashboard</a>' : ''}
        ${me.role === 'ADMIN' ? '<a href="admin-dashboard.html" class="nav-link-ghost">Admin</a>' : ''}
        <a href="#" onclick="logout()" class="nav-link-outline" style="border-color:var(--primary);color:var(--primary)">Đăng xuất</a>
      `;
    } else {
      navLinks.innerHTML = `
        <a href="doctors.html" class="nav-link-ghost">Bác sĩ</a>
        <a href="login.html" class="nav-link-outline">Đăng nhập</a>
        <a href="register.html" class="nav-link-solid">Đăng ký miễn phí</a>
      `;
    }
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
        <div style="width:1px; height:20px; background:var(--neutral-200)"></div>
        <div style="display:flex; flex-direction:column; align-items:flex-end">
          <span style="font-size:12px; font-weight:700; color:var(--neutral-900)">${me.fullName}</span>
          <span style="font-size:10px; color:var(--neutral-500)">${me.role}</span>
        </div>
        <button class="btn-secondary btn-sm" onclick="logout()" style="padding:4px 12px; font-size:12px">Đăng xuất</button>
      ` : `
        <a href="login.html" class="btn-ghost btn-sm" style="font-weight:600">Đăng nhập</a>
        <a href="register.html" class="btn-primary btn-sm" style="font-weight:600">Đăng ký</a>
      `}
    </div>
  `;
};

const initNav = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
        setNav(null);
        return;
    }
    try {
        const r = await API("/api/auth/me");
        if (r.ok) {
            const me = await r.json();
            setNav(me);
        } else {
            setNav(null);
        }
    } catch(e) {
        setNav(null);
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
const showToast = (message, type = "success") => {
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
  toast.innerHTML = `
    <span class="material-icons-round" style="font-size:20px">${c.icon}</span>
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

