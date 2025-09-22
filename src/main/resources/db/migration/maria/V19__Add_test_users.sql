-- 테스트용 사용자 계정 추가

-- 관리자 계정 (비밀번호: 1234)
INSERT IGNORE INTO users (email, password, nickname, role, created_at) VALUES
('admin@example.com', '$2a$10$Zk63iE9f2BM1bff87n7gO.VU9kXav8kHfMciH/SyOWixVDOEGMicq', '관리자', 'ADMIN', NOW());

-- 기존 사용자들을 관리자로 설정 (이미 role이 설정되지 않은 경우)
UPDATE users SET role = 'ADMIN' WHERE email IN ('test@example.com', 'admin@test.com') AND role IS NULL;