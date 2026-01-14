-- Создание таблицы deliveries
CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY,
    delivery_id UUID UNIQUE NOT NULL,
    order_id UUID NOT NULL,
    from_country VARCHAR,
    from_city VARCHAR,
    from_street VARCHAR,
    from_house VARCHAR,
    from_flat VARCHAR,
    to_country VARCHAR,
    to_city VARCHAR,
    to_street VARCHAR,
    to_house VARCHAR,
    to_flat VARCHAR,
    delivery_state VARCHAR NOT NULL
);
