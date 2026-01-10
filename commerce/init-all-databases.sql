-- Создание базы данных и пользователя для shopping-cart
CREATE DATABASE "shopping-cart-db";
CREATE USER shopping_cart_user WITH PASSWORD 'shopping_cart_password';
GRANT ALL PRIVILEGES ON DATABASE "shopping-cart-db" TO shopping_cart_user;

-- Переключение на БД shopping-cart-db для выдачи прав на схему
\c "shopping-cart-db"
GRANT CREATE ON SCHEMA public TO shopping_cart_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO shopping_cart_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO shopping_cart_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO shopping_cart_user;

-- Возврат к postgres БД для создания следующей
\c postgres

-- Создание базы данных и пользователя для shopping-store
CREATE DATABASE "shopping-store-db";
CREATE USER shopping_store_user WITH PASSWORD 'shopping_store_password';
GRANT ALL PRIVILEGES ON DATABASE "shopping-store-db" TO shopping_store_user;

-- Переключение на БД shopping-store-db для выдачи прав на схему
\c "shopping-store-db"
GRANT CREATE ON SCHEMA public TO shopping_store_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO shopping_store_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO shopping_store_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO shopping_store_user;

-- Возврат к postgres БД для создания следующей
\c postgres

-- Создание базы данных и пользователя для warehouse
CREATE DATABASE "warehouse-db";
CREATE USER warehouse_user WITH PASSWORD 'warehouse_password';
GRANT ALL PRIVILEGES ON DATABASE "warehouse-db" TO warehouse_user;

-- Переключение на БД warehouse-db для выдачи прав на схему
\c "warehouse-db"
GRANT CREATE ON SCHEMA public TO warehouse_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO warehouse_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO warehouse_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO warehouse_user;

-- Возврат к postgres БД для создания следующей
\c postgres

-- Создание базы данных и пользователя для analyzer
CREATE DATABASE analyzer_db;
CREATE USER analyzer_user WITH PASSWORD 'analyzer_password';
GRANT ALL PRIVILEGES ON DATABASE analyzer_db TO analyzer_user;

-- Переключение на БД analyzer_db для выдачи прав на схему
\c analyzer_db
GRANT CREATE ON SCHEMA public TO analyzer_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO analyzer_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO analyzer_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO analyzer_user;
