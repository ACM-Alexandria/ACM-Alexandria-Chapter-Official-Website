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
