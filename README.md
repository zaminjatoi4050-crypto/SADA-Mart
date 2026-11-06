# SADA Mart

SADA Mart is a JavaFX-based inventory and point-of-sale application for a
retail mart. It was developed for the **SW 212 Data Structures & Algorithms**
open-ended lab, where the assigned data structure is a **Stack**.

The application provides a store-friendly interface for products, inventory,
sales, customers, suppliers, reports, users and settings. It also supports
Oracle-backed authentication and database migrations.

## Assignment alignment

| Requirement | Implementation |
|---|---|
| Assigned data structure | Generic LIFO `InventoryStack<T>` |
| Core operations | `push`, `pop`, `peek`, replace/update, delete and snapshot |
| Inventory use case | Products are managed through Stack-backed inventory operations |
| CRUD demonstration | Add, view, update and delete product workflows |
| Practical algorithms | Cart totals, discounts, tax, payment allocation and stock checks |
| User interface | JavaFX screens with product cards, search, filters and POS checkout |
| Database | Oracle schema managed through Liquibase |

The Stack follows **LIFO (Last In, First Out)**:

```text
Push(Rice)
Push(Oil)
Push(Coffee)
Pop()  -> Coffee
```

The implementation and operation explanation are available in
[`documentation/STACK_ASSIGNMENT.md`](documentation/STACK_ASSIGNMENT.md).
The report is available at
[`documentation/STACK_ASSIGNMENT_REPORT.docx`](documentation/STACK_ASSIGNMENT_REPORT.docx).

## Main features

- Secure login with role-based access
- Admin, manager and cashier roles
- Stack-based inventory operations
- Product search and category filtering
- Product images and product cards
- Point-of-sale basket and checkout
- Quantity, discount, tax and change calculations
- Inventory and low-stock views
- Supplier and customer management screens
- Sales and store reports
- Light and dark themes
- Resizable and movable JavaFX window
- Oracle database authentication
- Automatic Liquibase schema and seed migrations

## Technology

- Java 21
- JavaFX 21
- Spring Boot 4
- Oracle JDBC
- Oracle Database
- Liquibase
- Maven Wrapper
- JUnit 5
- PBKDF2 password hashing

## Requirements

- Windows, macOS or Linux desktop with a graphical display
- JDK 21
- Network access for Maven dependencies on the first build
- Oracle Database with the `FREEPDB1` service for connected mode

On Windows, the project uses the included `mvnw.cmd` Maven Wrapper. The
launcher can automatically use the Microsoft JDK 21 installation at:

```text
C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot
```

You may also set `JAVA_HOME` to another valid JDK 21 installation.

## Build and test

From the repository root:

```powershell
.\mvnw.cmd verify
```

This compiles the project and runs the automated test suite.

To start the JavaFX application without connected Oracle configuration:

```powershell
.\mvnw.cmd javafx:run
```

Connected mode is described below.

## Oracle connected mode

The recommended one-command launcher is:

```powershell
.\run-connected.cmd
```

The launcher configures:

```text
Host:        localhost
Port:        1521
Service:     FREEPDB1
User:        sadamart_app
TCPS:        false
```

It asks for the password of the Oracle application user
`sadamart_app`. This is the **database password**, not the SADA Mart
operator password.

### Create the Oracle application user

Connect to the `FREEPDB1` pluggable database as an Oracle administrator, not
as the application user:

```sql
ALTER SESSION SET CONTAINER = FREEPDB1;

CREATE USER sadamart_app IDENTIFIED BY "Engineer786";

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE,
      CREATE TRIGGER, CREATE VIEW TO sadamart_app;

ALTER USER sadamart_app QUOTA UNLIMITED ON USERS;
```

If the user already exists and its password must be reset:

```sql
ALTER USER sadamart_app IDENTIFIED BY "Engineer786" ACCOUNT UNLOCK;
```

Do not use `SYS` or `SYSTEM` as the application account in the launcher.

### Database migrations

On the first successful login attempt, Liquibase creates the application
tables and inserts the demonstration operator accounts. Later launches report
that the database is already up to date.

The changelog is:

[`src/main/resources/db/changelog/db.changelog-master.xml`](src/main/resources/db/changelog/db.changelog-master.xml)

## Presentation credentials

These are demonstration operator accounts created by the Liquibase seed
migration:

| Role | Username | Password |
|---|---|---|
| Admin | `engineer.admin` | `Engineer786` |
| Manager | `store.manager` | `Manager786` |
| Cashier | `store.cashier` | `Cashier786` |

Do not use these demonstration passwords in a production deployment. Customers
do not have a separate login; authorized store staff manage customer records.

## Typical demonstration workflow

1. Start the application with `run-connected.cmd`.
2. Log in as `engineer.admin`.
3. Open the dashboard and explain sales, profit and stock indicators.
4. Open Point of Sale and search or filter products.
5. Add a product to the basket and explain subtotal, discount, tax and total.
6. Select a payment method and complete the sale.
7. Demonstrate Stack behavior:
   - Add a product: `push`
   - View the latest item: `peek`
   - Remove the latest item: `pop`
   - Change product information: update/replace
   - Remove a product: delete
8. Sign out and demonstrate the manager and cashier roles.

## Project structure

```text
src/main/java/com/sadamart
├── application      JavaFX launcher and application entry points
├── domain           Persistence entities
├── infrastructure   Oracle datasource configuration
├── repository       Data access interfaces
├── security         Password hashing and role/session support
├── service          InventoryStack and business services
└── ui               JavaFX controllers and workspace state

src/main/resources
├── css              Application theme
├── fxml             Login, workspace and connection views
├── images           SADA Mart logo and product artwork
└── db/changelog     Oracle Liquibase schema and seed migrations
```

## Verification

Run:

```powershell
.\mvnw.cmd verify
```

The project currently includes automated tests for:

- Oracle connection properties
- Stack operations
- Cart totals
- Payment allocation
- Security and role behavior
- Sale return validation
- Weighted average cost calculations

## Important notes

- Oracle must be running and reachable on port `1521` for connected mode.
- The password entered at the launcher prompt belongs to `sadamart_app`.
- The operator password is entered later on the SADA Mart login screen.
- Never commit database passwords, production credentials or personal customer
  information.
- The included product catalog is used for the current POS demonstration;
  Oracle authentication, schema and user migrations are connected through the
  database. Product and operational repository wiring can be expanded as the
  next persistence phase.
