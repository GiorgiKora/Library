-- Create the library database (if it doesn't exist)
CREATE DATABASE IF NOT EXISTS library;

-- Create the books table
CREATE TABLE IF NOT EXISTS books (
    code VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    CHECK (quantity >= 0) -- Ensure quantity is not negative
);

-- Create the members table
CREATE TABLE IF NOT EXISTS members (
    id SERIAL PRIMARY KEY,  -- Auto-incrementing ID
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL, -- Unique email constraint
    join_date DATE NOT NULL
);

-- Create the borrowings table
CREATE TABLE IF NOT EXISTS borrowings (
    book_code VARCHAR(255) REFERENCES books(code) NOT NULL,
    member_id INTEGER REFERENCES members(id) NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE,
    PRIMARY KEY (book_code, member_id, borrow_date), -- Composite primary key
    CHECK (borrow_date <= return_date OR return_date IS NULL) -- Return date must be after borrow date or null
);

-- Example inserts (optional - you can add these later)
--INSERT INTO books (code, title, author, quantity) VALUES ('12345', 'The Lord of the Rings', 'J.R.R. Tolkien', 5);
--INSERT INTO books (code, title, author, quantity) VALUES ('67890', 'Pride and Prejudice', 'Jane Austen', 3);
--
--INSERT INTO members (name, email, join_date) VALUES ('John Doe', 'john.doe@example.com', '2023-10-26');
--INSERT INTO members (name, email, join_date) VALUES ('Jane Smith', 'jane.smith@example.com', '2023-10-27');

-- Example borrow (optional)
--INSERT INTO borrowings (book_code, member_id, borrow_date) VALUES ('12345', 1, '2023-10-28');