# Mini Jira — Spring Boot Backend

Drop-in replacement for your `json-server` backend. Same data shape, same
basic endpoint structure (`/projects`, `/resources`, `/users`, `/tickets`),
so your Angular services should need little to no change beyond the base
URL.

## Stack

- Spring Boot 3.3 (Java 17)
- Spring Data JPA
- Spring Security + JWT authentication
- H2, file-based (`./data/minijira.mv.db`) — data survives restarts
- BCrypt password hashing

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

### Upload to server 
```bash
scp -o "ProxyCommand=cloudflared access ssh --hostname %h" mini-jira-backend.jar rbl@terminal-ssh.customadsph.online:/home/rbl/bpi/mini-jira-back
```

### Or use WinSCP
- Run in a separate terminal and keep open (if port is forbidden try another port)

```bash
cloudflared access tcp --hostname terminal-ssh.customadsph.online --url localhost:222
```

- Use this `localhost:222` in WinSCP login form

### Access server
```bash
ssh rbl@terminal-ssh.customadsph.online -o "ProxyCommand=cloudflared access ssh --hostname %h"
```

### Run app
```bash
./run-jira.sh
```

### Check app log
```bash
tail -f app.log
```

### Stop app
```bash
pkill -f mini-jira-backend.jar
```

### Check app running
```bash
ps aux | grep java
```

The API comes up on **http://localhost:8080**.

On first run, `DataSeeder` inserts the same projects/resources/users/tickets
you already had in `db.json`. After that it's a no-op (it checks if the
`projects` table already has rows).

H2 console (handy for poking at the data directly): http://localhost:8080/h2-console
JDBC URL: `jdbc:h2:file:./data/minijira`, user `sa`, no password.

## Authentication & Authorization

### JWT Tokens
- All protected endpoints require a valid JWT token in the `Authorization` header
- Format: `Authorization: Bearer <token>`

### Roles
- **ADMIN** - Full access, can delete projects/users
- **PROJECT_MANAGER** - Can create/update projects, sprints, resources; delete tickets
- **DEVELOPER** - Can create/update/patch tickets, leave comments
- **VIEWER** - Read-only access, can comment on tickets

### Endpoints

| Endpoint | Method | Requires | Purpose |
|---|---|---|---|
| `/auth/login` | POST | None | Login with username/password, get JWT token |
| `/auth/register` | POST | None | Register new user (defaults to VIEWER role) |
| `/users/by-role/{role}` | GET | ADMIN | Get all users with a specific role |

### Test the Authentication

**1. Register a new user:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "testuser",
  "role": "VIEWER"
}
```

**2. Login with existing user (from seed data):**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin"}'
```

**3. Use the token to access protected endpoints:**
```bash
# Get all projects (requires authentication)
curl -X GET http://localhost:8080/projects \
  -H "Authorization: Bearer <your_token_here>"

# Create a new project (requires PROJECT_MANAGER or ADMIN)
curl -X POST http://localhost:8080/projects \
  -H "Authorization: Bearer <your_token_here>" \
  -H "Content-Type: application/json" \
  -d '{"id":"p999","key":"TEST","name":"Test Project","description":"A test project"}'

# Create a ticket (requires DEVELOPER, PROJECT_MANAGER, or ADMIN)
curl -X POST http://localhost:8080/tickets \
  -H "Authorization: Bearer <your_token_here>" \
  -H "Content-Type: application/json" \
  -d '{"title":"New ticket","projectId":"p1","status":"todo","priority":"medium"}'
```

### Default Test Users (from seed data)

| Username | Password | Role |
|---|---|---|
| admin | admin | ADMIN |
| pm | pm | PROJECT_MANAGER |
| dev | dev | DEVELOPER |
| Other | N/A | VIEWER |

## Endpoints

| Resource | Routes |
|---|---|
| Auth | `POST /auth/login`, `POST /auth/register` |
| Projects | `GET/POST /projects`, `GET/PUT/DELETE /projects/{id}` |
| Resources | `GET/POST /resources`, `GET/PUT/DELETE /resources/{id}` |
| Users | `GET/POST /users`, `GET/PUT/DELETE /users/{id}`, `GET /users/by-role/{role}` |
| Tickets | `GET/POST /tickets`, `GET/PUT/PATCH/DELETE /tickets/{id}` |
| Sprints | `GET/POST /sprints`, `GET/PUT/DELETE /sprints/{id}` |
| Comments | `GET /tickets/{ticketId}/comments`, `POST /tickets/{ticketId}/comments` |

**Filtering** (matches what you were already doing against json-server):

```
GET /tickets?projectId=p1
GET /tickets?projectId=p1&status=done
GET /tickets?resourceId=hCLaVXGjyEk
GET /sprints?projectId=p1
```

**Partial ticket updates** (useful for drag-and-drop status changes):

```
PATCH /tickets/{id}
Body: { "status": "in-progress" }
Authorization: Bearer <token>
```

## Role-Based Access Control (RBAC)

### Create/Update Operations
- **Projects**: PROJECT_MANAGER | ADMIN
- **Tickets**: DEVELOPER | PROJECT_MANAGER | ADMIN
- **Sprints**: PROJECT_MANAGER | ADMIN
- **Resources**: PROJECT_MANAGER | ADMIN
- **Comments**: DEVELOPER | PROJECT_MANAGER | ADMIN | VIEWER

### Delete Operations
- **Projects**: ADMIN only
- **Tickets**: PROJECT_MANAGER | ADMIN
- **Sprints**: ADMIN only
- **Resources**: ADMIN only
- **Users**: ADMIN only

### Read Operations
- All roles can read (GET requests are not restricted)

## What's different from json-server (worth knowing)

1. **Authentication required** - Most endpoints now require a JWT token (except `/auth/*`)
2. **Role-based access** - Write/delete operations are restricted by user role
3. **Password hashing** - All passwords are stored as BCrypt hashes, never plaintext
4. **`key` on Project** is mapped to a `project_key` column under the hood
   (KEY is a reserved SQL word), but the JSON field is still `"key"` — no
   frontend change needed.
5. **New IDs** are generated as UUIDs on `POST` if you don't supply one,
   instead of nanoid-style strings. Doesn't affect anything unless your
   frontend assumes a specific ID format/length.
6. **Tickets are flat/denormalized** (`projectId`, `resourceId` as plain
   strings, not JPA relations) — same as json-server. If you later want
   the DB to enforce that a ticket's `projectId` actually exists, swap
   these for `@ManyToOne` relations.

## Connecting your Angular app

Update `environment.ts`:

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080'
};
```

### Update your auth service to use JWT

```typescript
// auth.service.ts
login(username: string, password: string) {
  return this.http.post(`${this.apiUrl}/auth/login`, {
    username,
    password
  }).pipe(
    tap((response: any) => {
      localStorage.setItem('token', response.token);
      localStorage.setItem('userId', response.userId);
      localStorage.setItem('role', response.role);
    })
  );
}

// Add token to all HTTP requests
// In your HTTP interceptor:
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  const token = localStorage.getItem('token');
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next.handle(req);
}
```

Your services that already call `/projects`, `/resources`, `/users`,
`/tickets` should work unchanged. Just make sure your HTTP interceptor
adds the JWT token to the `Authorization` header for all requests.
