-- Fix seed user passwords (plaintext: password, BCrypt strength 12)
UPDATE users
SET password_hash = '$2b$12$QnbSsXc0GDugZ51jLGwBZu4Dhj/UOf81VMWxEiOPJ0BPVQ.cOk3IO'
WHERE password_hash = '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y';
