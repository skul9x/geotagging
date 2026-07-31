# 📍 Batch Geotagging & File Range Manager Android App

Ứng dụng Android đa năng hỗ trợ **gắn tọa độ địa lý (Geotagging)** hàng loạt cho hình ảnh qua dữ liệu EXIF và **Quản lý dải tệp tin (File Range Manager)** nâng cao. Được phát triển bằng **Kotlin** và giao diện hiện đại **Jetpack Compose (Material 3)**.

---

## 🌟 Tính năng chính

### 1. 📍 Batch Geotagging (Gắn tọa độ EXIF hàng loạt)
- 📸 **Chọn ảnh linh hoạt**:
  - Chọn một hoặc nhiều ảnh lẻ bằng **Android Photo Picker**.
  - Chọn toàn bộ thư mục ảnh thông qua **Storage Access Framework (SAF)**.
- 🗺️ **Đọc & Hiển thị thông tin EXIF GPS**: Tự động trích xuất vĩ độ (Latitude) và kinh độ (Longitude) từ dữ liệu EXIF của từng tệp ảnh.
- ✏️ **Cập nhật tọa độ hàng loạt**: Cho phép nhập tọa độ GPS mới và áp dụng cập nhật EXIF cho tất cả các bức ảnh đã chọn cùng lúc.
- ⚡ **Xử lý bất đồng bộ (Coroutines)**: Ghi dữ liệu EXIF chạy trên IO Thread kèm thanh tiến trình thời gian thực, đảm bảo giao diện luôn mượt mà.

### 2. 🗂️ Quản lý dải tệp tin (File Range Manager)
- 🔢 **Lọc theo dải (Range Filter)**: Lọc và chọn tệp tin theo chỉ số bắt đầu và kết thúc (vd: từ file số `001` đến file số `050`).
- 🔤 **Natural Order Sorting**: Sắp xếp tên tệp thông minh theo thứ tự tự nhiên (ví dụ: `file1.jpg` -> `file2.jpg` -> `file10.jpg` thay vì kiểu sắp xếp ký tự thông thường).
- 🛠️ **Thao tác hàng loạt**:
  - **Đổi tên (Rename)**: Đổi tên các file theo mẫu đánh số tự động.
  - **Sao chép (Copy)**: Sao chép dải tệp tin tới thư mục đích.
  - **Di chuyển (Move)**: Di chuyển dải tệp tin tới vị trí mới.
  - **Xóa (Delete)**: Xóa an toàn dải tệp tin đã chọn.
- 📊 **Hộp thoại tiến trình thời gian thực**: Hiển thị % tiến độ, số lượng tệp đã xử lý và cho phép hủy thao tác đang chạy.

---

## 🛠️ Công nghệ & Thư viện sử dụng

- **Ngôn ngữ**: [Kotlin 2.0.0](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) với **Material 3**
- **Điền hướng (Navigation)**: `androidx.navigation:navigation-compose` (2.7.7)
- **Kiến trúc**: **MVVM (Model - View - ViewModel)** + Unidirectional Data Flow (StateFlow, Coroutines)
- **Các thư viện chính**:
  - `androidx.exifinterface:exifinterface` (v1.3.7): Đọc và ghi metadata EXIF của hình ảnh.
  - `io.coil-kt:coil-compose` (v2.6.0): Tải và hiển thị ảnh bất đồng bộ hiệu năng cao.
  - `androidx.documentfile:documentfile` (v1.0.1): Duyệt và quản lý tập tin từ `TreeUri` (Folder picker).
  - `androidx.activity:activity-compose` (v1.9.0): Tích hợp ActivityResultLauncher (Photo Picker, SAF).

---

## 📂 Cấu trúc thư mục dự án

```text
com.skul9x.geotagging/
├── MainActivity.kt                # Activity chính khởi chạy ứng dụng
├── data/
│   └── model/
│       ├── FileItem.kt            # Model lưu thông tin tệp tin chung
│       ├── FileOperationMode.kt   # Enum định nghĩa các chế độ thao tác tệp (Copy, Move, Rename, Delete)
│       └── GeoImage.kt            # Model lưu thông tin ảnh (Uri, Tên, Dung lượng, Tọa độ GPS)
├── ui/
│   ├── home/                      # Màn hình Geotagging chính
│   │   ├── HomeScreen.kt          # Giao diện xem danh sách ảnh và nhập tọa độ GPS
│   │   └── HomeViewModel.kt       # ViewModel xử lý đọc/ghi EXIF
│   ├── navigation/
│   │   └── MainTabScreen.kt       # Điều hướng tab giữa Geotagging và File Range Manager
│   ├── range/                     # Màn hình Quản lý dải tệp tin
│   │   ├── FileRangeScreen.kt     # Giao diện bộ lọc dải và thao tác tệp
│   │   ├── FileRangeViewModel.kt  # ViewModel quản lý state và các tác vụ dải tệp
│   │   └── components/            # Các UI Component phụ trách hiển thị tiến trình & Dialog
│   └── theme/                     # Cấu hình giao diện Material 3 (Color, Theme, Type)
└── utils/
    ├── ExifUtils.kt               # Helper utility hỗ trợ đọc/ghi vị trí GPS vào EXIF
    ├── FileOperationsHelper.kt    # Helper thực hiện đổi tên, sao chép, di chuyển, xóa tệp
    ├── FileRangeFilter.kt         # Thuật toán lọc tệp theo dải
    └── NaturalOrderComparator.kt  # Thuật toán sắp xếp tên file theo thứ tự tự nhiên
```

---

## 📋 Quyền ứng dụng (Permissions)

Ứng dụng khai báo các quyền trong `AndroidManifest.xml`:
- `android.permission.READ_MEDIA_IMAGES` (Android 13+)
- `android.permission.READ_EXTERNAL_STORAGE` (Android 12 trở xuống)
- `android.permission.ACCESS_MEDIA_LOCATION`: Đọc dữ liệu tọa độ vị trí EXIF từ tệp phương tiện.

---

## 🚀 Hướng dẫn cài đặt & Biên dịch

### Yêu cầu hệ thống
- **Android Studio**: Jellyfish (2023.3.1) hoặc mới hơn.
- **JDK**: Java 17 hoặc 21.
- **Android SDK**: Compile SDK 34, Minimum SDK 24 (Android 7.0+).

### Các bước biên dịch thủ công
1. **Clone repository**:
   ```bash
   git clone https://github.com/skul9x/geotagging.git
   cd geotagging
   ```

2. **Mở dự án trong Android Studio**:
   - Mở Android Studio -> **Open** -> Chọn thư mục dự án.
   - Chờ Gradle Sync hoàn tất các phụ thuộc.

3. **Biên dịch và Chạy (Build & Run)**:
   - Sử dụng Gradle Wrapper:
     ```bash
     ./gradlew assembleDebug
     ```
   - Tệp APK đầu ra sẽ nằm tại: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📖 Hướng dẫn sử dụng

### Tab 1: Geotagging
1. **Thêm ảnh**: Nhấn **"Ảnh"** để chọn tệp lẻ hoặc **"Thư mục"** để chọn toàn bộ thư mục chứa ảnh.
2. **Xem vị trí**: Tọa độ GPS trích xuất từ EXIF sẽ hiển thị góc dưới mỗi ảnh.
3. **Cập nhật tọa độ**: Nhấn nút FAB ✏️, nhập **Vĩ độ (Latitude)** và **Kinh độ (Longitude)**, sau đó nhấn **"Áp dụng"**.

### Tab 2: File Range Manager
1. **Chọn thư mục làm việc**: Nhấn chọn thư mục chứa các tệp cần xử lý.
2. **Lọc dải tệp**: Nhập chỉ số bắt đầu và kết thúc (vd: `1` đến `50`) để chọn danh sách tệp mong muốn.
3. **Thực hiện thao tác**: Chọn hành động (Đổi tên, Sao chép, Di chuyển, Xóa), thiết lập tham số và theo dõi tiến trình thực hiện thời gian thực.

---

## ⚙️ CI/CD & Tự động hóa

Dự án tích hợp **GitHub Actions CI/CD** tại `.github/workflows/build.yml`:
- Tự động đóng gói ứng dụng thành tệp `geotagging-v1.3.apk`.
- Tự động khởi tạo **GitHub Release** với tag `v1.3` và đính kèm bản build `geotagging-v1.3.apk` khi có thay đổi trên nhánh `main` hoặc khi đẩy tag mới.

---

## 📄 Bản quyền (Copyright)

Copyright 2026 Nguyễn Duy Trường
