CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           TEXT        NOT NULL UNIQUE,
    password_hash   TEXT        NOT NULL,
    goal_calories   INT         NOT NULL,
    goal_protein_g  INT,
    goal_fat_g      INT,
    goal_carb_g     INT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users (id),
    name                TEXT           NOT NULL,
    brand               TEXT,
    calories_per_100g   NUMERIC        NOT NULL,
    protein_per_100g    NUMERIC        NOT NULL,
    fat_per_100g        NUMERIC        NOT NULL,
    carb_per_100g       NUMERIC        NOT NULL,
    is_public           BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE dishes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users (id),
    name            TEXT        NOT NULL,
    is_public       BOOLEAN     NOT NULL DEFAULT FALSE,
    total_weight_g  NUMERIC     NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE dish_items (
    id          BIGSERIAL PRIMARY KEY,
    dish_id     BIGINT  NOT NULL REFERENCES dishes (id),
    product_id  BIGINT  NOT NULL REFERENCES products (id),
    amount_g    NUMERIC NOT NULL
);

CREATE TABLE entries (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    eaten_at    TIMESTAMPTZ NOT NULL,
    meal_type   VARCHAR     NOT NULL,
    source_type VARCHAR     NOT NULL,
    product_id  BIGINT      REFERENCES products (id),
    dish_id     BIGINT      REFERENCES dishes (id),
    amount_g    NUMERIC     NOT NULL,
    calories    NUMERIC     NOT NULL,
    protein_g   NUMERIC     NOT NULL,
    fat_g       NUMERIC     NOT NULL,
    carb_g      NUMERIC     NOT NULL,

    CONSTRAINT chk_meal_type
        CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK')),

    CONSTRAINT chk_source_type
        CHECK (source_type IN ('PRODUCT', 'DISH')),

    CONSTRAINT chk_source_exclusive
        CHECK (
            (product_id IS NOT NULL AND dish_id IS NULL)
            OR
            (product_id IS NULL AND dish_id IS NOT NULL)
        )
);

CREATE INDEX idx_products_user_id  ON products (user_id);
CREATE INDEX idx_dishes_user_id    ON dishes (user_id);
CREATE INDEX idx_dish_items_dish   ON dish_items (dish_id);
CREATE INDEX idx_entries_user_id   ON entries (user_id);
CREATE INDEX idx_entries_eaten_at  ON entries (eaten_at);
