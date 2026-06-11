# Модель данных

Реализация доменных понятий из [CONTEXT.md](../CONTEXT.md). Глоссарий — там; здесь — таблицы и поля.

## Сущности

### users
| поле | тип | заметки |
|------|-----|---------|
| id | bigint PK | |
| email / username | text unique | для входа |
| password_hash | text | bcrypt |
| goal_calories | int | дневная Цель, обязательно |
| goal_protein_g | int null | опционально |
| goal_fat_g | int null | опционально |
| goal_carb_g | int null | опционально |
| created_at | timestamptz | |

Цель хранится прямо на пользователе (одна на юзера), отдельной таблицы пока нет.

### products (Продукт)
| поле | тип | заметки |
|------|-----|---------|
| id | bigint PK | |
| user_id | bigint FK null | NULL = seed/системный продукт |
| name | text | |
| brand | text null | для брендированных/ресторанных позиций |
| calories_per_100g | numeric | КБЖУ всегда на 100 г |
| protein_per_100g | numeric | |
| fat_per_100g | numeric | |
| carb_per_100g | numeric | |
| is_public | boolean | приватность |
| created_at | timestamptz | |

### dishes (Блюдо)
| поле | тип | заметки |
|------|-----|---------|
| id | bigint PK | |
| user_id | bigint FK | автор |
| name | text | |
| is_public | boolean | приватное/публичное |
| total_weight_g | numeric | сумма весов компонентов |
| created_at | timestamptz | |

### dish_items (состав Блюда)
| поле | тип | заметки |
|------|-----|---------|
| id | bigint PK | |
| dish_id | bigint FK | |
| product_id | bigint FK | |
| amount_g | numeric | сколько этого Продукта в Блюде |

КБЖУ Блюда выводится из dish_items, приводится к 100 г.

### entries (Запись / съеденное)
| поле | тип | заметки |
|------|-----|---------|
| id | bigint PK | |
| user_id | bigint FK | |
| eaten_at | timestamptz | момент приёма |
| meal_type | enum | breakfast / lunch / dinner / snack |
| source_type | enum | product / dish |
| product_id | bigint FK null | заполнено если source_type=product |
| dish_id | bigint FK null | заполнено если source_type=dish |
| amount_g | numeric | количество в граммах |
| calories | numeric | снапшот КБЖУ на момент записи (ADR-0004) |
| protein_g | numeric | снапшот |
| fat_g | numeric | снапшот |
| carb_g | numeric | снапшот |

Ровно одно из product_id / dish_id заполнено (CHECK-констрейнт).

## Ключевые инварианты
- КБЖУ Продукта и Блюда — всегда на 100 г.
- Запись всегда в граммах (и для Продукта, и для Блюда — см. CONTEXT.md).
- КБЖУ Записи — снапшот, не пересчитывается при правке Продукта/Блюда ([ADR-0004](adr/0004-entry-nutrition-snapshot.md)).
- `user_id` и `is_public` есть с самого начала, хотя пользователь пока один ([ADR-0003](adr/0003-multi-user-ready-schema.md)).
