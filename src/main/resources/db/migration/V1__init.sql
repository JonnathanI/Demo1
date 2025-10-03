-- ==========================================================
-- MIGRATION V1: CORE STRUCTURE (USERS, POINTS, QUESTIONS)
-- ==========================================================

-- Table 1: USERS
-- Stores the student's registration and profile information.
CREATE TABLE IF NOT EXISTS USERS (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
                                     email VARCHAR(100) UNIQUE NOT NULL,
                                     password_hash VARCHAR(255) NOT NULL, -- Store the hashed password (NEVER plaintext!)
                                     full_name VARCHAR(100),
                                     current_level VARCHAR(20) DEFAULT 'Principiante', -- Starter level for progression
                                     registration_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: USER_POINTS
-- Stores the current point balance for each user.
CREATE TABLE IF NOT EXISTS USER_POINTS (
                                           user_id INT PRIMARY KEY,
                                           total_points INT DEFAULT 0 CHECK (total_points >= 0),
                                           FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE
);

-- Table 3: QUESTIONS
-- Stores the main game content.
CREATE TABLE IF NOT EXISTS QUESTIONS (
                                         id SERIAL PRIMARY KEY,
                                         question_text TEXT NOT NULL,
                                         difficulty_level VARCHAR(20) NOT NULL CHECK (difficulty_level IN ('Principiante', 'Normal', 'Difícil')),
                                         points_awarded INT NOT NULL, -- Points gained for a correct answer (more for 'Difícil')
                                         category VARCHAR(50) -- E.g., 'Vocabulary', 'Grammar'
);

-- Table 4: RESPONSE_OPTIONS
-- Stores the possible answers for each question.
CREATE TABLE IF NOT EXISTS RESPONSE_OPTIONS (
                                                id SERIAL PRIMARY KEY,
                                                question_id INT NOT NULL,
                                                option_text TEXT NOT NULL,
                                                is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                                                FOREIGN KEY (question_id) REFERENCES QUESTIONS (id) ON DELETE CASCADE
);

-- Table 5: GAME_SESSIONS
-- Registers the start and end of a learning/test round.
CREATE TABLE IF NOT EXISTS GAME_SESSIONS (
                                             id SERIAL PRIMARY KEY,
                                             user_id INT NOT NULL,
                                             game_type VARCHAR(20) NOT NULL CHECK (game_type IN ('Aprender', 'Prueba')),
                                             difficulty_level VARCHAR(20) NOT NULL,
                                             points_earned INT DEFAULT 0,
                                             start_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                             end_time TIMESTAMP WITHOUT TIME ZONE,
                                             FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE
);

-- Table 6: RESPONSE_LOG
-- Detailed record of every user response within a session.
CREATE TABLE IF NOT EXISTS RESPONSE_LOG (
                                            id SERIAL PRIMARY KEY,
                                            session_id INT NOT NULL,
                                            question_id INT NOT NULL,
                                            selected_option_id INT,
                                            is_correct BOOLEAN NOT NULL,
                                            points_gained INT DEFAULT 0,
                                            response_time_ms INT,
                                            FOREIGN KEY (session_id) REFERENCES GAME_SESSIONS (id) ON DELETE CASCADE,
                                            FOREIGN KEY (question_id) REFERENCES QUESTIONS (id) ON DELETE RESTRICT,
                                            FOREIGN KEY (selected_option_id) REFERENCES RESPONSE_OPTIONS (id) ON DELETE SET NULL
);