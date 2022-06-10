CREATE USER crawler_user WITH PASSWORD 'crawler_password';

GRANT ALL PRIVILEGES ON DATABASE se_crawler_data to crawler_user;