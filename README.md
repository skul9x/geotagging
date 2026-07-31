# Batch Geotagging & File Range Manager 📍📁

Ứng dụng Android hiện đại, tối ưu cho việc **gán tọa độ GPS hàng loạt (Batch Geotagging)** vào dữ liệu ảnh EXIF và **quản lý dải tệp (File Range Manager)** linh hoạt, chính xác và trực quan.

![Android SDK 34](https://img.shields.io/badge/Android-SDK%2034-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin 2.0.0](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat&logo=githubactions&logoColor=white)

---

## 📌 Tính Năng Nổi Bật

### 1. 📍 Batch Geotagging (Gán Tọa Độ GPS Hàng Loạt)
- **Chọn ảnh linh hoạt**: Cho phép chọn nhiều ảnh lẻ từ thư viện hoặc quét toàn bộ ảnh trong một thư mục.
- **Phân tích tọa độ thông minh (`GpsCoordinateParser`)**:
  - Hỗ trợ cả định dạng dấu chấm (`21.0285, 105.8542`) và dấu phẩy (`21,0285, 105,8542`).
  - Phân tích cú pháp chuỗi tọa độ đơn dòng sao chép nhanh từ Google Maps (VD: `21.1573890, 106.1998193`).
  - Nút **"Dán từ bộ nhớ tạm"** tự động phát hiện và trích xuất tọa độ GPS từ clipboard.
- **Xem trước & Chỉnh sửa trực quan**: Xem trạng thái tọa độ EXIF của từng tệp ảnh ngay trên giao diện danh sách.

### 2. 📁 File Range Manager (Quản Lý Dải Tệp Tự Động)
- **Chọn dải tệp thông minh**: Lựa chọn tệp Bắt đầu (*Start File*) và tệp Kết thúc (*End File*) với thuật toán sắp xếp tự nhiên (*Natural Order Comparator*).
- **Menu chọn tệp cải tiến**: Tích hợp ô tìm kiếm tên tệp theo thời gian thực và xem trước ảnh thu nhỏ (Thumbnail) bằng Coil.
- **Thao tác sao chép & di chuyển (COPY / MOVE)**:
  - Tùy chọn gom dải tệp vào thư mục con mới (*Subfolder*).
  - Tự động chuyển vùng làm việc vào thư mục mới sau khi hoàn thành.
- **Theo dõi tiến trình**: Hiển thị thanh phần trăm và số lượng tệp đã xử lý theo thời gian thực.

---

## 🛠 Công Nghệ Sử Dụng

- **Ngôn ngữ lập trình**: Kotlin 2.0.0
- **Giao diện người dùng (UI)**: Jetpack Compose với hệ thống thiết kế Material 3
- **Tải & Xem trước ảnh**: Coil Compose (`io.coil-kt:coil-compose:2.7.0`)
- **Xử lý EXIF Metadata**: Android `ExifInterface`
- **Xử lý bất đồng bộ**: Kotlin Coroutines & Flow
- **Kiến trúc ứng dụng**: MVVM (Model-View-ViewModel) kết hợp Jetpack StateFlow
- **Hệ thống Build**: Gradle (Kotlin DSL, AGP 8.5+)
- **Tự động hóa CI/CD**: GitHub Actions tự động build APK và tạo Release GitHub

---

## 📂 Cấu Trúc Thư Mục Dự Án

```
geotagging-main/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/skul9x/geotagging/
│   │   │   │   ├── data/            # Data models & storage logic
│   │   │   │   ├── ui/
│   │   │   │   │   ├── home/        # Màn hình Batch Geotagging & Edit Location Dialog
│   │   │   │   │   ├── range/       # Màn hình File Range Manager & components
│   │   │   │   │   └── theme/       # Color palette & Typography (Material 3)
│   │   │   │   ├── utils/           # GpsCoordinateParser, NaturalOrderComparator, ExifHelper
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/                 # Resource drawables, Adaptive Icons, Mipmap XMLs
│   │   └── test/                    # Unit tests & End-to-End Integration Test Suite
├── .github/
│   └── workflows/
│       └── build.yml                # CI/CD pipeline tự động build APK v1.4
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### Cách 1: Tải file APK dựng sẵn
Bạn có thể tải trực tiếp bản build APK mới nhất từ phần [Releases](https://github.com/skul9x/geotagging/releases) của repository:
- **Tệp cài đặt**: `geotagging-v1.4.apk`

### Cách 2: Tự biên dịch từ mã nguồn (Build from source)
1. **Clone repository**:
   ```bash
   git clone https://github.com/skul9x/geotagging.git
   cd geotagging
   ```

2. **Mở dự án bằng Android Studio**:
   - Khuyến nghị sử dụng **Android Studio Ladybug (2024.2.1+)** hoặc phiên bản mới hơn.
   - Đảm bảo JDK 17 được cấu hình trong `Project Structure`.

3. **Chạy Unit Tests & Kiểm thử**:
   ```bash
   ./gradlew test
   ```

4. **Biên dịch APK Debug**:
   ```bash
   ./gradlew assembleDebug
   ```
   *File APK sau khi build nằm tại:* `app/build/outputs/apk/debug/app-debug.apk`

---

## 📖 Hướng Dẫn Sử Dụng

1. **Gán tọa độ GPS cho ảnh**:
   - Chọn mục **"Ảnh"** hoặc **"Thư mục"** ở thanh điều hướng dưới cùng để nạp ảnh vào danh sách.
   - Nhấn nút **Sửa (Edit)** ở góc dưới bên phải.
   - Nhập tọa độ Lat/Long hoặc dán chuỗi từ Google Maps (VD: `21.1573890, 106.1998193`), hoặc nhấn **"Dán từ bộ nhớ tạm"**.
   - Nhấn **"Áp dụng"** để cập nhật tọa độ EXIF cho toàn bộ danh sách ảnh.

2. **Quản lý dải tệp (File Range)**:
   - Chuyển sang thẻ **File Range**.
   - Chọn **Source Directory** (thư mục nguồn) và **Target Directory** (thư mục đích).
   - Chọn **Start File** và **End File** từ danh sách thả xuống (có thể tìm kiếm và xem thumbnail).
   - Chọn chế độ **COPY** hoặc **MOVE**, bật tùy chọn thư mục con nếu cần.
   - Bấm **"Start Operation"** để thực thi.

---

## 📄 Bản Quyền (Copyright)

Copyright 2026 Nguyễn Duy Trường
