-- Создание таблицы payments
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    payment_id UUID UNIQUE NOT NULL,
    order_id UUID NOT NULL,
    product_price DOUBLE PRECISION NOT NULL,
    delivery_price DOUBLE PRECISION NOT NULL,
    total_price DOUBLE PRECISION NOT NULL,
    fee_total DOUBLE PRECISION NOT NULL,
    payment_state VARCHAR NOT NULL
);
