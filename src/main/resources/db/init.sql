-- 거안결 MVP — 초기 DDL 스크립트
-- 새 MySQL 데이터베이스(toss_invoice)에서 한 번 실행하면 모든 테이블이 만들어집니다.
-- ddl-auto: validate 정책이므로 엔티티가 바뀌면 이 파일도 같이 갱신해야 합니다.
-- 실행:  mysql -uroot -p toss_invoice < src/main/resources/db/init.sql


-- =============================================================================
-- users : 회원(거래처) 테이블
--   - email, business_number는 unique
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    company_name    VARCHAR(100) NOT NULL,
    business_type   VARCHAR(20)  NOT NULL,
    business_number VARCHAR(20)  NOT NULL,
    ceo_name        VARCHAR(20)  NOT NULL,
    account         VARCHAR(20)  NOT NULL,
    bank            VARCHAR(20)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    address         VARCHAR(200) NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    deleted_at      DATE         NULL,
    company_type    VARCHAR(20)  NOT NULL,
    created_at      DATETIME(6)  NULL,
    modified_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_business_number (business_number),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- refresh_tokens : 사용자별 refresh 토큰 1:1 테이블 (RTR 정책)
--   - user_id가 PK 겸 FK
--   - 로그아웃 / 회원탈퇴 시 ON DELETE CASCADE로 자동 정리
-- =============================================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(512) NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    created_at  DATETIME(6)  NULL,
    modified_at DATETIME(6)  NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- trade : 거래 1건의 코어
--   - status : PENDING_PO / PENDING_SELLER_SIGN / PENDING_INVOICE
--              / PENDING_BUYER_CONFIRM / COMPLETED / CANCELLED
--   - row 생성 시점 = PI 발행 시점 (수주처가 견적서 발행 누른 순간)
--   - total_amount = SUM(order_items.subtotal) + tax (서버 재계산)
-- =============================================================================
CREATE TABLE IF NOT EXISTS trade (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    seller_id     BIGINT       NOT NULL,
    buyer_id      BIGINT       NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    deposit_rate  DECIMAL(5, 2) NULL,
    total_amount  INT          NULL,
    tax           INT          NULL,
    created_at    DATETIME(6)  NULL,
    modified_at   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_trade_seller (seller_id),
    KEY idx_trade_buyer  (buyer_id),
    CONSTRAINT fk_trade_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT fk_trade_buyer  FOREIGN KEY (buyer_id)  REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- proforma_invoice : 견적서(PI) — trade와 1:1
--   - valid_until : 발주처가 이 시각까지 PO 작성을 시작해야 유효 (지나면 스케줄러가 CANCELLED 처리)
--   - seller_signature_url : 수주처 서명 S3 URL
-- =============================================================================
CREATE TABLE IF NOT EXISTS proforma_invoice (
    id                          BIGINT       NOT NULL AUTO_INCREMENT,
    trade_id                    BIGINT       NOT NULL,
    doc_number                  VARCHAR(30)  NOT NULL,
    proforma_invoice_datetime   DATETIME(6)  NOT NULL,
    production_days             INT          NOT NULL,
    valid_until                 DATETIME(6)  NOT NULL,
    seller_signature_url        VARCHAR(512) NULL,
    seller_signed_at            DATETIME(6)  NULL,
    created_at                  DATETIME(6)  NULL,
    modified_at                 DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_proforma_invoice_trade (trade_id),
    UNIQUE KEY uk_proforma_invoice_doc_number (doc_number),
    CONSTRAINT fk_proforma_invoice_trade FOREIGN KEY (trade_id) REFERENCES trade (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- purchase_order : 발주서(PO) — trade와 1:1
--   - row 생성 시점 = 발주처가 "발주서 작성하기" 클릭 직후 (라우팅 복귀용 빈 row)
--   - buyer_signature_url 채워지면 = 발주처 발행 완료
--   - seller_signature_url 채워지면 = 수주처 카운터서명 완료 → PO 확정(선금 자동입금)
--   - seller_account_snapshot/seller_bank_snapshot : 결제 트리거 시점 계좌 감사용
-- =============================================================================
CREATE TABLE IF NOT EXISTS purchase_order (
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    trade_id                  BIGINT       NOT NULL,
    doc_number                VARCHAR(30)  NOT NULL,
    purchase_order_datetime   DATETIME(6)  NOT NULL,
    desired_delivery_date     DATE         NULL,
    confirmed_delivery_date   DATE         NULL,
    buyer_signature_url       VARCHAR(512) NULL,
    buyer_signed_at           DATETIME(6)  NULL,
    seller_signature_url      VARCHAR(512) NULL,
    seller_signed_at          DATETIME(6)  NULL,
    seller_account_snapshot   VARCHAR(20)  NULL,
    seller_bank_snapshot      VARCHAR(20)  NULL,
    deposit_paid_at           DATETIME(6)  NULL,
    deposit_amount            INT          NULL,
    created_at                DATETIME(6)  NULL,
    modified_at               DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchase_order_trade (trade_id),
    UNIQUE KEY uk_purchase_order_doc_number (doc_number),
    CONSTRAINT fk_purchase_order_trade FOREIGN KEY (trade_id) REFERENCES trade (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- invoice : 최종 인보이스 — trade와 1:1
--   - row 생성 시점 = 수주처가 인보이스 발행 (seller_signature_url 채워진 상태로 insert)
--   - buyer_signature_url 채워지면 = 발주처 최종 서명 → 거래 종결(후금 자동입금)
-- =============================================================================
CREATE TABLE IF NOT EXISTS invoice (
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    trade_id                  BIGINT       NOT NULL,
    doc_number                VARCHAR(30)  NOT NULL,
    invoice_datetime          DATETIME(6)  NOT NULL,
    shipping_info             VARCHAR(255) NULL,
    seller_signature_url      VARCHAR(512) NULL,
    seller_signed_at          DATETIME(6)  NULL,
    buyer_signature_url       VARCHAR(512) NULL,
    buyer_signed_at           DATETIME(6)  NULL,
    seller_account_snapshot   VARCHAR(20)  NULL,
    seller_bank_snapshot      VARCHAR(20)  NULL,
    balance_paid_at           DATETIME(6)  NULL,
    balance_amount            INT          NULL,
    created_at                DATETIME(6)  NULL,
    modified_at               DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_trade (trade_id),
    UNIQUE KEY uk_invoice_doc_number (doc_number),
    CONSTRAINT fk_invoice_trade FOREIGN KEY (trade_id) REFERENCES trade (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- order_items : 거래 라인 아이템 — trade에 1:N
--   - 가격(unit_price/subtotal)은 PI 작성 시점에 락이며 PO/Invoice에서 수정 불가
--   - subtotal은 서버에서 재계산해 저장(클라가 보낸 값 신뢰 X)
-- =============================================================================
CREATE TABLE IF NOT EXISTS order_items (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    trade_id      BIGINT       NOT NULL,
    product_name  VARCHAR(255) NOT NULL,
    product_num   INT          NOT NULL,
    quantity      INT          NOT NULL,
    unit_price    INT          NOT NULL,
    subtotal      INT          NOT NULL,
    created_at    DATETIME(6)  NULL,
    modified_at   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_order_items_trade (trade_id),
    CONSTRAINT fk_order_items_trade FOREIGN KEY (trade_id) REFERENCES trade (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- report : 신고 이력 — users와 1:N
--   - reported_id는 trade 테이블에서 상대방으로 특정
-- =============================================================================
CREATE TABLE IF NOT EXISTS report (
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    reported_id               BIGINT       NOT NULL,
    trade_id                  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_report_trade_reported (trade_id, reported_id),
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_id) REFERENCES users (id),
    CONSTRAINT fk_report_trade    FOREIGN KEY (trade_id)    REFERENCES trade (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- notifications : 알림 테이블
--   - sender_id  : 발신자 (계좌변경 알림처럼 시스템 발송 시 NULL 가능)
--   - receiver_id: 수신자
--   - trade_id   : 거래 관련 알림이 아닌 경우 NULL
--   - message    : 실제 노출 메시지 (발신자 이름 등 동적 값 포함)
-- =============================================================================
CREATE TABLE IF NOT EXISTS notifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    sender_id         BIGINT       NULL,
    receiver_id       BIGINT       NOT NULL,
    trade_id          BIGINT       NULL,
    notification_type VARCHAR(30)  NOT NULL,
    message           VARCHAR(255) NOT NULL,
    created_at        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_receiver (receiver_id),
    CONSTRAINT fk_notifications_sender   FOREIGN KEY (sender_id)   REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_trade    FOREIGN KEY (trade_id)    REFERENCES trade (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
