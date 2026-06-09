-- Chạy trên Supabase SQL Editor
-- Thêm username (@handle) cho bảng users

ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS username text;

-- Unique, không phân biệt hoa thường
CREATE UNIQUE INDEX IF NOT EXISTS users_username_unique_idx
ON public.users (lower(username))
WHERE username IS NOT NULL;

-- Backfill từ email cho user hiện có
UPDATE public.users
SET username = lower(split_part(email, '@', 1))
WHERE username IS NULL
  AND email IS NOT NULL
  AND email LIKE '%@%';
