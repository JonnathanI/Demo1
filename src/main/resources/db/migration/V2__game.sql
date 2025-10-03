-- ==========================================================
-- MIGRATION V2: GAMIFICATION (COSMETICS, ADVANTAGES, INVENTORY)
-- ==========================================================

-- Table 7: PROFILE_COSMETICS
-- Catalog of aesthetic items redeemable (backgrounds, frames).
CREATE TABLE IF NOT EXISTS PROFILE_COSMETICS (
                                                 id SERIAL PRIMARY KEY,
                                                 name VARCHAR(50) UNIQUE NOT NULL,
                                                 type VARCHAR(50) NOT NULL, -- E.g., 'Background', 'Frame', 'Avatar'
                                                 point_cost INT NOT NULL CHECK (point_cost > 0),
                                                 resource_url VARCHAR(255) -- Path to the image/CSS resource
);

-- Table 8: COSMETICS_INVENTORY
-- Tracks which cosmetic items each user owns and which is active.
CREATE TABLE IF NOT EXISTS COSMETICS_INVENTORY (
                                                   id SERIAL PRIMARY KEY,
                                                   user_id INT NOT NULL,
                                                   cosmetic_id INT NOT NULL,
                                                   acquisition_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                                   is_active BOOLEAN DEFAULT FALSE, -- Whether the item is currently applied to the profile
                                                   FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
                                                   FOREIGN KEY (cosmetic_id) REFERENCES PROFILE_COSMETICS (id) ON DELETE RESTRICT,
                                                   UNIQUE (user_id, cosmetic_id)
);

-- Table 9: ADVANTAGES
-- Catalog of redeemable advantages (extra time, hints).
CREATE TABLE IF NOT EXISTS ADVANTAGES (
                                          id SERIAL PRIMARY KEY,
                                          name VARCHAR(50) UNIQUE NOT NULL,
                                          description TEXT,
                                          point_cost INT NOT NULL CHECK (point_cost > 0),
                                          effect VARCHAR(50) NOT NULL -- E.g., 'Extra_Time_10s', 'Hint_Remove_Option'
);

-- Table 10: ADVANTAGE_PURCHASES
-- Logs the advantages purchased by the user.
CREATE TABLE IF NOT EXISTS ADVANTAGE_PURCHASES (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   user_id INT NOT NULL,
                                                   advantage_id INT NOT NULL,
                                                   purchase_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                                   is_used BOOLEAN DEFAULT FALSE, -- For one-time use advantages
                                                   FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
                                                   FOREIGN KEY (advantage_id) REFERENCES ADVANTAGES (id) ON DELETE RESTRICT
);

-- V2.1 Modification: Add column to track advantage usage in the game
-- Crucial for business logic and detailed logging.
ALTER TABLE RESPONSE_LOG
    ADD COLUMN IF NOT EXISTS advantage_used BOOLEAN DEFAULT FALSE;