<!-- Render: GitHub / trình xem Markdown hỗ trợ HTML & SVG -->

<p align="center">
  <img src="travel_frontend/design/vietnam_animated_logo.svg" alt="VIETNAM Travel" width="320"/>
</p>

<p align="center">
  <strong>Vietnam Travel Itinerary</strong><br/>
  Nền tảng lập lịch trình &amp; cộng đồng du lịch Việt Nam
</p>

<p align="center">
  <a href="https://drive.google.com/drive/folders/1SnC808VUBcOjSkkttOQDJcJngClAQJvu?usp=sharing">
    <strong>⬇ Tải APK — VietNamTravel.apk</strong>
  </a>
  &nbsp;·&nbsp;
  <a href="https://www.figma.com/design/Ao46NmxaB21je4dKqXuSU5/travel-VietNam?node-id=0-1&t=razabF79oDnBfNEK-1">
    <strong>🎨 Figma UI Design</strong>
  </a>
</p>

---

<!-- Non-render: trình xem chỉ hiển thị text thuần (không HTML/SVG) -->

# VIETNAM TRAVEL

**Vietnam Travel Itinerary** — Nền tảng lập lịch trình & cộng đồng du lịch Việt Nam

**Logo động:** `travel_frontend/design/vietnam_animated_logo.svg`  
**Logo tĩnh:** `travel_frontend/design/vietnam_logo_static.svg`  
**App icon:** `travel_frontend/app/src/main/res/drawable/ic_launcher_foreground.xml`

**Tải APK:** https://drive.google.com/drive/folders/1SnC808VUBcOjSkkttOQDJcJngClAQJvu?usp=sharing

**Figma UI Design:** https://www.figma.com/design/Ao46NmxaB21je4dKqXuSU5/travel-VietNam?node-id=0-1&t=razabF79oDnBfNEK-1

---

## Giới thiệu

**Vietnam Travel Itinerary** là nền tảng công nghệ kết hợp **công cụ lập kế hoạch du lịch cá nhân** và **mạng xã hội thu nhỏ** dành cho cộng đồng yêu du lịch trong nước. Ứng dụng giúp người dùng tìm địa điểm, xem thời tiết & sự kiện, xây dựng lịch trình nhiều ngày và chia sẻ hành trình khắp **63 tỉnh thành Việt Nam**.

### Vấn đề giải quyết

Khi tự lên kế hoạch du lịch, người dùng thường phải tổng hợp thông tin từ nhiều nguồn rời rạc: địa điểm, giờ mở cửa, thời tiết, sự kiện địa phương. Việc sắp xếp điểm đến theo ngày, theo dõi khoảng cách và nhận cảnh báo cũng khó nếu chỉ dùng ghi chú thông thường. Bên cạnh đó, nhu cầu chia sẻ trải nghiệm và lên kế hoạch cùng bạn bè/nhóm vẫn thiếu công cụ gắn kết trực tiếp với dữ liệu địa điểm và lịch trình.

### Giải pháp

Vietnam Travel Itinerary số hóa toàn bộ quy trình khám phá và lập kế hoạch:

- Tìm kiếm địa điểm theo **tỉnh/thành**, **loại hình** và **trending**
- Xem **dự báo thời tiết** và **sự kiện/lễ hội** gắn với địa điểm
- Lập **lịch trình nhiều ngày**, sắp xếp thứ tự điểm đến và nhận **cảnh báo tự động**
- **Mời cộng tác** vào lịch trình qua email
- **Đăng bài**, tương tác (reaction, bình luận, repost), **theo dõi** người dùng
- **Đề xuất địa điểm mới** từ cộng đồng, Admin duyệt trước khi hiển thị

### Nhóm người dùng

| Vai trò | Mô tả |
|---|---|
| **Traveler** | Tìm địa điểm, lập lịch trình, đánh giá, chia sẻ trải nghiệm |
| **Collaborator** | Được mời cùng xem/chỉnh sửa một lịch trình |
| **Admin** | Duyệt đề xuất địa điểm, xử lý báo cáo, quản lý tài khoản |

### Tính năng cốt lõi

- Khám phá địa điểm theo tỉnh thành, loại hình, trending
- Widget thời tiết & sự kiện/lễ hội theo địa phương
- Lịch trình nhiều ngày, cảnh báo thời tiết/đông đúc/khoảng cách
- Cộng tác lịch trình, mời thành viên
- Cộng đồng: bài đăng, reaction, bình luận, repost, follow
- Thông báo theo thời gian thực
- Đề xuất địa điểm & quy trình kiểm duyệt Admin

> **Lưu ý phạm vi:** Phần gamification (thành tích, bảng xếp hạng) chưa triển khai trong bản hiện tại và có thể bổ sung ở giai đoạn tiếp theo.

---

## Logo

Logo thương hiệu **VIETNAM** gồm chữ đỏ, núi, sóng, máy bay và xe đạp — dùng trên top bar ứng dụng và tài liệu dự án.

<p align="center">
  <img src="travel_frontend/design/vietnam_animated_logo.svg" alt="Logo animated" width="320"/>
  <br/>
  <sub><strong>Logo động</strong> — dùng làm tiêu đề README &amp; top bar app</sub>
</p>

<p align="center">
  <img src="travel_frontend/design/vietnam_logo_static.svg" alt="Logo static" width="280"/>
  <br/>
  <sub><strong>Logo tĩnh</strong> — fallback khi không hỗ trợ SVG animation</sub>
</p>

| File | Mô tả |
|---|---|
| [`vietnam_animated_logo.svg`](travel_frontend/design/vietnam_animated_logo.svg) | Logo có animation (chữ, sóng, máy bay, xe đạp, leo núi) |
| [`vietnam_logo_static.svg`](travel_frontend/design/vietnam_logo_static.svg) | Logo tĩnh, dùng in ấn / môi trường không render animation |
| [`AnimatedVietnamLogo.kt`](travel_frontend/app/src/main/java/com/example/vietnam_travel_itinerary_android/ui/components/AnimatedVietnamLogo.kt) | Implementation Compose trong app |
| [`ic_launcher_foreground.xml`](travel_frontend/app/src/main/res/drawable/ic_launcher_foreground.xml) | Icon launcher — ngôi sao trắng trên nền đỏ |

**Non-render:**

```
Logo động : travel_frontend/design/vietnam_animated_logo.svg
Logo tĩnh : travel_frontend/design/vietnam_logo_static.svg
App icon  : travel_frontend/app/src/main/res/drawable/ic_launcher_foreground.xml
```

---

## UI Design (Figma)

Toàn bộ giao diện ứng dụng được thiết kế trên Figma, bao gồm các màn hình chính: Trang chủ, Khám phá địa điểm, Lịch trình, Cộng đồng, Hồ sơ, Thông báo, Admin và các component UI (weather widget, logo, form, v.v.).

**[🎨 travel-VietNam — Figma Design](https://www.figma.com/design/Ao46NmxaB21je4dKqXuSU5/travel-VietNam?node-id=0-1&t=razabF79oDnBfNEK-1)**

| Tài nguyên | Mô tả |
|---|---|
| **Figma** | File thiết kế UI/UX đầy đủ — layout, màu sắc, component |
| **Logo** | Xem mục [Logo](#logo) ở trên |

---

## Tải & cài đặt

### 1. Tải file APK

Tải **VietNamTravel.apk** từ Google Drive:

**[📁 VietNamTravel — Google Drive](https://drive.google.com/drive/folders/1SnC808VUBcOjSkkttOQDJcJngClAQJvu?usp=sharing)**

*(Kích thước ~75 MB — bản debug, đã ký debug keystore)*

### 2. Cài trên điện thoại Android

1. Mở link Google Drive trên điện thoại (hoặc tải APK về máy tính rồi chuyển sang điện thoại).
2. Chạm **Tải xuống** / **Download** để tải `VietNamTravel.apk`.
3. Mở file APK vừa tải (thường nằm trong thư mục **Downloads**).
4. Nếu Android yêu cầu, bật **Cài đặt ứng dụng không rõ nguồn gốc** cho trình duyệt hoặc **Files**:
   - *Cài đặt → Bảo mật → Cài đặt ứng dụng không xác định* (tên menu có thể khác tùy hãng).
5. Chạm **Cài đặt** → **Mở** để sử dụng.

### 3. Cài qua ADB (dành cho developer)

```bash
adb install -r VietNamTravel.apk
adb shell am start -n com.example.vietnam_travel_itinerary_android/.MainActivity
```

### Yêu cầu hệ thống

- **Android 11.0 (API 30)** trở lên
- Kết nối Internet để đăng nhập và đồng bộ dữ liệu
- Cả Backend (Spring Boot) và dịch vụ Supabase cần hoạt động để sử dụng đầy đủ tính năng

---

## Công nghệ

| Thành phần | Công nghệ / Thư viện | Chi tiết |
|---|---|---|
| **Mobile (Frontend)** | Android (Kotlin) | Jetpack Compose, Retrofit, Coroutines, OkHttp, Supabase Kotlin SDK (Auth, Storage, Realtime, Postgrest) |
| **Backend API** | Spring Boot (Java 17) | Spring Web, Spring Data JPA (Hibernate), Spring Security (OAuth2 Resource Server để xác thực JWT của Supabase) |
| **Database & Services** | Supabase | PostgreSQL (Cơ sở dữ liệu chính), Supabase Auth (Quản lý tài khoản & xác thực), Supabase Storage (Lưu trữ ảnh/media) |
| **Thời tiết & Dữ liệu** | Open-Meteo API | API dự báo thời tiết cho các tỉnh thành |

---

## Luồng sử dụng cơ bản

1. **Khám phá** địa điểm theo tỉnh thành hoặc loại hình
2. **Tạo lịch trình**, thêm điểm đến từng ngày và sắp xếp thứ tự
3. **Mời cộng tác viên** (nếu đi cùng nhóm)
4. Sau chuyến đi: **đánh giá**, **check-in** và **đăng bài** chia sẻ
5. Phát hiện địa điểm mới? **Gửi đề xuất** — Admin duyệt trước khi hiển thị

---

<p align="center">
  <sub>Cập nhật: 21/06/2026 · Vietnam Travel Itinerary</sub>
</p>
