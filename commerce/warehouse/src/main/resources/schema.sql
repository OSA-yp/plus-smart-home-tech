-- Создание таблицы products_in_warehouse
CREATE TABLE IF NOT EXISTS products_in_warehouse (
    id UUID PRIMARY KEY,
    product_id UUID UNIQUE NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN,
    quantity BIGINT NOT NULL
);

-- Создание таблицы order_bookings
CREATE TABLE IF NOT EXISTS order_bookings (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,
    delivery_id UUID
);