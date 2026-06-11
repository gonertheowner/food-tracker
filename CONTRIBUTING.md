# Соглашения разработки

## Коммиты — Conventional Commits

Формат: `type(scope): summary`

- Язык — **английский**, summary в повелительном наклонении, с маленькой буквы, без точки в конце.
- Типы: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `build`, `ci`.
- `scope` — опционально, область изменения (`products`, `dishes`, `entries`, `auth`, `reports`, `infra`).

Примеры:
```
feat(entries): add meal type to food log
fix(dishes): correct per-100g calorie calculation
docs(adr): record entry nutrition snapshot decision
chore(infra): add docker-compose with postgres
```

## Ветки — `type/short-description`

- Префикс совпадает с типом коммита: `feat/`, `fix/`, `chore/`, `docs/`, `refactor/`.
- Описание — kebab-case, английский, коротко.
- `main` — основная ветка (транк), всегда в рабочем состоянии.

Примеры:
```
feat/entry-log
fix/dish-calories
chore/docker-compose
```
