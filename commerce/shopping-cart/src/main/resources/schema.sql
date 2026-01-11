-- Создание таблицы shopping_carts
CREATE TABLE IF NOT EXISTS shopping_carts (
    shopping_cart_id UUID PRIMARY KEY,
    username VARCHAR NOT NULL,
    cart_state VARCHAR NOT NULL
);

-- Создание таблицы cart_products
CREATE TABLE IF NOT EXISTS cart_products (
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT,
    PRIMARY KEY (cart_id, product_id),
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(shopping_cart_id) ON DELETE CASCADE
);
