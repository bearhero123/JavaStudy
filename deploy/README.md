# Server release

The production server uses a source checkout at `/opt/mis-study/source` and keeps its runtime configuration in `/opt/mis-study/backend/application.yaml`. The configuration file is intentionally not stored in Git.

After pushing a reviewed commit to `main`, connect to the server and run:

```bash
bash /opt/mis-study/source/deploy/release-server.sh
```

The script pulls only fast-forward updates, builds the Vue frontend and Spring Boot backend, switches the frontend to a versioned release directory, replaces the backend JAR, restarts `mis-study-backend.service`, and restores the prior JAR and frontend release if the service cannot start.

It uses the isolated Node runtime at `/opt/mis-study/tools/node`, so it does not modify the server's system Node installation.
