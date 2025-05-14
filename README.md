# CZERTAINLY-Webhook-Notification-Provider

> This repository is part of the commercial open-source project CZERTAINLY. You can find more information about the project at [CZERTAINLY](https://github.com/CZERTAINLY/CZERTAINLY) repository, including the contribution guide.

Webhook Notification Provider `Connector` is the implementation of the following `Function Groups` and `Kinds`:

| Function Group          | Kind    |
|-------------------------|---------|
| `Notification Provider` | `WEBHOOK` |

It is compatible with the `Notification Provider` interface. This connector provides the following features:
- Send webhook notifications

## Database requirements

Webhook Notification Provider `Connector` requires the PostgreSQL database to store the data about the configured webhooks.

## Interfaces

Webhook Notification Provider implements `Notification Provider` interfaces. To learn more about the interfaces and end points, refer to the [CZERTAINLY Interfaces](https://github.com/CZERTAINLY/CZERTAINLY-Interfaces).

For more information, please refer to the [CZERTAINLY documentation](https://docs.czertainly.com).

## Docker container

Webhook Notification Provider `Connector` is provided as a Docker container. Use the `czertainly/czertainly-webhook-notification-provider:tagname` to pull the required image from the repository. It can be configured using the following environment variables:

| Variable        | Description                                              | Required                                           | Default value |
|-----------------|----------------------------------------------------------|----------------------------------------------------|---------------|
| `JDBC_URL`      | JDBC URL for database access                             | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`         |
| `JDBC_USERNAME` | Username to access the database                          | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`         |
| `JDBC_PASSWORD` | Password to access the database                          | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`         |
| `DB_SCHEMA`     | Database schema to use                                   | ![](https://img.shields.io/badge/-NO-red.svg)      | `webhooknp`   |
| `PORT`          | Port where the service is exposed                        | ![](https://img.shields.io/badge/-NO-red.svg)      | `8080`        |
| `JAVA_OPTS`     | Customize Java system properties for running application | ![](https://img.shields.io/badge/-NO-red.svg)      | `N/A`         |
