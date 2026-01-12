-- Создание таблицы orders
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    order_id UUID UNIQUE NOT NULL,
    shopping_cart_id UUID,
    order_state VARCHAR NOT NULL,
    payment_id UUID,
    delivery_id UUID,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    total_price DOUBLE PRECISION,
    delivery_price DOUBLE PRECISION,
    product_price DOUBLE PRECISION
);

-- Создание таблицы order_products
CREATE TABLE IF NOT EXISTS order_products (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);
