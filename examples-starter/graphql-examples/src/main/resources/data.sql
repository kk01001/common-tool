INSERT INTO authors (id, name) VALUES (1, 'Robert Martin');
INSERT INTO authors (id, name) VALUES (2, 'Joshua Bloch');
INSERT INTO authors (id, name) VALUES (3, 'Martin Fowler');

INSERT INTO books (id, title, author_id, description, price, publish_date, cover_image) VALUES (101, 'Clean Code', 1, 'A Handbook of Agile Software Craftsmanship', 30.0, '2008-08-01 00:00:00', 'http://example.com/clean_code.jpg');
INSERT INTO books (id, title, author_id, description, price, publish_date, cover_image) VALUES (102, 'Effective Java', 2, 'Best practices for the Java platform', 45.0, '2008-05-28 00:00:00', 'http://example.com/effective_java.jpg');
INSERT INTO books (id, title, author_id, description, price, publish_date, cover_image) VALUES (103, 'Refactoring', 3, 'Improving the Design of Existing Code', 50.0, '1999-07-08 00:00:00', 'http://example.com/refactoring.jpg');
