# MySQL Configuration

Make sure MySQL is running locally on port `3306`.

For a standard MySQL installation, use:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

The username is usually `root`, and the password is the one you created when installing MySQL.

You can verify your credentials with:

```bash
mysql -u root -p
```

The database URL normally **does not need to be changed**:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/acm_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Only change the URL if your MySQL server uses a different host or port.

## Current board season

The starting year of the current academic season is stored in the singleton
`system_settings` row (`id = 1`). For example, `2026` means `2026/2027`.

Hibernate's `ddl-auto=update` adds the nullable column automatically. If applying
the schema change manually to an existing database, run this only when the column
does not already exist:

```sql
ALTER TABLE system_settings ADD COLUMN current_season INT NULL;
```

Leave the value `NULL` to initialize it on the first board-season read. The backend
uses the server date: September through December uses the current calendar year;
January through August uses the previous calendar year. The application currently
sets the server time zone to UTC. Once initialized, the season is stored rather
than recalculated as the date changes. Archiving advances the stored season.

## Testing board archiving

Board archiving copies committees, clubs, and current board members into the five
`past_*` tables, clears the current board tables, changes board users to `USER`
(except `SUPER_ADMIN`), and advances the season. It preserves the original
committee and club rows. Back up before testing this operation and use a scratch
copy rather than the production database:

```bash
mysqldump -u root -p --single-transaction acm_db > acm_db_backup.sql
mysql -u root -p -e "CREATE DATABASE acm_boards_scratch;"
mysql -u root -p acm_boards_scratch < acm_db_backup.sql
```

Point the backend's datasource at `acm_boards_scratch` when testing manually.
The archive service's automated integration tests use a separate, in-memory H2
database; they do not read or modify your local MySQL data. MySQL-specific behavior
should also be verified on the scratch copy before deployment.

The archive confirmation includes the starting year, so a repeated request with
the old year is rejected instead of archiving the newly advanced season. Its
summary counts distinct users whose security role actually changed to `USER`.
An already-`USER` board member still has their committee association cleared,
but is not counted as a role downgrade.
