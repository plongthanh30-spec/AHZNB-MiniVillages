AHZNB MINI VILLAGES — source project
Minecraft Java 1.12.2 Forge (14.23.5.2860)

Ý tưởng:
- 5 kiểu làng mini: Lá, Cát, Sương, Mây, Âm.
- Mỗi công trình nhỏ, có rương loot.
- Mật độ mục tiêu khoảng 200–500 block giữa các lần thử sinh (cấu hình được).
- Công trình được tạo khi chunk mới được sinh; không tự xuất hiện trong chunk đã khám phá.
- Mặc định rương có vật phẩm vanilla để kiểm tra hoạt động.
- Có cấu hình để thêm item từ AHZNB bằng registry ID chính xác.

QUAN TRỌNG:
Đây là source project, chưa phải file .jar đã biên dịch và chưa được kiểm thử với AHZNB.
Không thể xác nhận tương thích item AHZNB cho tới khi biết đúng modid/registry IDs của bản AHZNB bạn dùng.
Không đoán ID item vì có thể làm rương trống hoặc gây lỗi.

Cách build trên máy tính:
1. Cài JDK 8.
2. Cài/giải nén Forge MDK 1.12.2, hoặc mở project này bằng môi trường Gradle hỗ trợ ForgeGradle 2.3.
3. Chạy `gradlew setupDecompWorkspace` rồi `gradlew build`.
4. File jar sẽ nằm trong `build/libs/`.
Lưu ý: ForgeGradle 2.3 cũ có thể cần cấu hình Java/Gradle phù hợp.

Cách cấu hình AHZNB:
Sau lần chạy đầu, sửa config `config/ahznbminivillages.cfg`.
Trong `loot`, thêm registry IDs theo định dạng `modid:item_name` và tùy chọn metadata như `modid:item_name@2`.
Ví dụ chỉ minh họa cú pháp, KHÔNG phải ID AHZNB đã xác nhận.
Dùng JEI (nếu có) hoặc danh sách registry của mod để tìm ID thật.

Mật độ:
`spacingBlocks=200` tới `500`. Đây là khoảng cách gần đúng theo ô chunk/đợt thử sinh, không đảm bảo chính xác khoảng cách Euclid giữa mọi làng.
Chỉ áp dụng cho chunk mới được tạo trong Overworld.
