-- =============================================================================
-- Supabase: bucket Storage "avatars" + RLS policies
-- Chạy trên: Supabase Dashboard → SQL Editor → New query → Run
--
-- Path upload từ app: avatars/{userId}/{timestamp}.jpg
-- Ví dụ: avatars/a1b2c3d4-.../1717948800123.jpg
-- =============================================================================

-- 1) Tạo bucket public (ai cũng xem được URL, chỉ owner mới upload/sửa/xóa)
INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO UPDATE
SET public = EXCLUDED.public;

-- Giới hạn file: 5MB, chỉ ảnh
UPDATE storage.buckets
SET
    file_size_limit = 5242880,
    allowed_mime_types = ARRAY[
        'image/jpeg',
        'image/jpg',
        'image/png',
        'image/webp'
    ]
WHERE id = 'avatars';

-- 2) Xóa policy cũ (nếu chạy lại script)
DROP POLICY IF EXISTS "avatars_public_read" ON storage.objects;
DROP POLICY IF EXISTS "avatars_owner_insert" ON storage.objects;
DROP POLICY IF EXISTS "avatars_owner_update" ON storage.objects;
DROP POLICY IF EXISTS "avatars_owner_delete" ON storage.objects;

-- 3) SELECT — mọi người đọc được (public bucket, hiển thị avatar trên app)
CREATE POLICY "avatars_public_read"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'avatars');

-- 4) INSERT — user đăng nhập chỉ upload vào thư mục của chính mình
--    folder đầu tiên trong path phải trùng auth.uid()
CREATE POLICY "avatars_owner_insert"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
);

-- 5) UPDATE — sửa/ghi đè file trong thư mục của mình (upsert)
CREATE POLICY "avatars_owner_update"
ON storage.objects
FOR UPDATE
TO authenticated
USING (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
)
WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
);

-- 6) DELETE — xóa avatar cũ (tùy chọn, dọn dẹp)
CREATE POLICY "avatars_owner_delete"
ON storage.objects
FOR DELETE
TO authenticated
USING (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
);

-- =============================================================================
-- Kiểm tra sau khi chạy (optional — xem kết quả trong Results)
-- =============================================================================
-- SELECT id, name, public, file_size_limit, allowed_mime_types
-- FROM storage.buckets WHERE id = 'avatars';
--
-- SELECT policyname, cmd, roles
-- FROM pg_policies
-- WHERE tablename = 'objects' AND schemaname = 'storage'
--   AND policyname LIKE 'avatars_%';
