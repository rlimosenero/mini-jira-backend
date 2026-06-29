# Mini Jira — Spring Boot Backend

Drop-in replacement for your `json-server` backend. Same data shape, same
basic endpoint structure (`/projects`, `/resources`, `/users`, `/tickets`),
so your Angular services should need little to no change beyond the base
URL.

## Stack

- Spring Boot 3.3 (Java 17)
- Spring Data JPA
- H2, file-based (`./data/minijira.mv.db`) — data survives restarts
- No auth yet (see `UserController` notes below)

## Run it

```bash
cd mini-jira-backend
mvn spring-boot:run
```

Or build a jar and run that:

```bash
mvn clean package
java -jar target/mini-jira-backend.jar
```

The API comes up on **http://localhost:8080**.

On first run, `DataSeeder` inserts the same projects/resources/users/tickets
you already had in `db.json`. After that it's a no-op (it checks if the
`projects` table already has rows).

H2 console (handy for poking at the data directly): http://localhost:8080/h2-console
JDBC URL: `jdbc:h2:file:./data/minijira`, user `sa`, no password.

## Endpoints

| Resource | Routes |
|---|---|
| Projects | `GET/POST /projects`, `GET/PUT/DELETE /projects/{id}` |
| Resources | `GET/POST /resources`, `GET/PUT/DELETE /resources/{id}` |
| Users | `GET/POST /users`, `GET/PUT/DELETE /users/{id}` |
| Tickets | `GET/POST /tickets`, `GET/PUT/PATCH/DELETE /tickets/{id}` |

**Filtering** (matches what you were already doing against json-server):

```
GET /tickets?projectId=p1
GET /tickets?projectId=p1&status=done
GET /tickets?resourceId=hCLaVXGjyEk
GET /users?username=admin&password=admin   <- your existing "login" call still works
```

**Partial ticket updates** (new — useful for drag-and-drop status changes,
so you don't have to PUT the whole ticket):

```
PATCH /tickets/{id}
Body: { "status": "in-progress" }
```

## What's different from json-server (worth knowing)

1. **`key` on Project** is mapped to a `project_key` column under the hood
   (KEY is a reserved SQL word), but the JSON field is still `"key"` — no
   frontend change needed.
2. **New IDs** are generated as UUIDs on `POST` if you don't supply one,
   instead of nanoid-style strings. Doesn't affect anything unless your
   frontend assumes a specific ID format/length.
3. **Tickets are flat/denormalized** (`projectId`, `resourceId` as plain
   strings, not JPA relations) — same as json-server. If you later want
   the DB to enforce that a ticket's `projectId` actually exists, swap
   these for `@ManyToOne` relations.
4. **No auth.** The `/users?username=&password=` route is left in only to
   match what your Angular app already calls — it's not secure (it both
   accepts and returns plaintext passwords, with no token/session issued).
   When you're ready, swap this for Spring Security + JWT and protect the
   other endpoints behind it.

## Connecting your Angular app

Update `environment.ts`:

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080'
};
```

Your services that already call `/projects`, `/resources`, `/users`,
`/tickets` should work unchanged. If you were tunneling json-server through
ngrok, you can do the same with this backend — `ngrok http 8080` — and you
won't need the `ngrok-skip-browser-warning` header workaround for *this*
server unless you tunnel it too (that header only matters when hitting an
ngrok-fronted endpoint from a browser-style request).
