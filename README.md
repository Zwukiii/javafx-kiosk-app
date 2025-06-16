## How to Run the Project

### 1. Start the PostgreSQL Database Using Docker

Run the following command in your terminal to launch a PostgreSQL container:

```bash
docker run --name postgresql \
  -p 5432:5432 \
  -e POSTGRESQL_USERNAME=my_user \
  -e POSTGRESQL_PASSWORD=password123 \
  -e POSTGRESQL_DATABASE=smartkiosk \
  bitnami/postgresql:latest
---
## Use Gradle to build and run the project:

bash
Copy
Edit
./gradlew build
./gradlew run

