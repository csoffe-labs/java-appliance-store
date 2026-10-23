# Home Appliance Store - README  
  
A Java application for managing a home appliance store, with console and web interfaces built around a shared object-oriented domain model. Uses Java 8, SQLite and Java’s built-in HttpServer, without external web frameworks.

---

## Project Structure

src/  
├── model/        # Domain classes (behaviour + invariants)  
├── dao/          # Database access and persistence
├── handlers/     # Web layer (HTML rendering + orchestration)  
├── console/      # Console menu CRUD  
└── server/       # HttpServer setup + routing  

---

## How to Run

1. Open in a Java IDE (such as Eclipse or IntelliJ IDEA), or compile and run using Java 8 from the command line.
2. Ensure `homeStore_V2.db` is in the project root  
3. Run either:    
   - **Console version:** Run `StoreConsole`  
   - **Web version:** Run `StoreServer`and then visit http://localhost:8080  

---

## Admin Login

The system already includes 3 demo admin accounts in the `users` table.  
Any of them can be used to access the admin-only CRUD pages on the web UI.

Primary account:

- **Username:** admin  
- **Password:** admin123  

**Note: Demo credentials are provided for local testing only. Authentication is not intended for production use.**

Additional users can be added with:

```sql
INSERT INTO users (username, password) VALUES ('newuser', 'newpass');