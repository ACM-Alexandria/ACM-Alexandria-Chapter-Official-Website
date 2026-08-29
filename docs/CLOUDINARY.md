# Cloudinary Setup

This document explains how to obtain Cloudinary credentials and configure the application for local development.

## Steps

1. Create a free account at [Cloudinary](https://cloudinary.com/) (click "Sign up").
2. Sign in and open the **Dashboard**.
3. In the Dashboard, locate the **API** (or Credentials) section and copy the following values:
   - **Cloud name** (often listed as `cloud_name`)
   - **API key**
   - **API secret**

## Exact property mappings

Add the following to your local `application.properties` (or set equivalent environment variables):

```properties
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret
```

## Notes and best practices

- Treat `cloudinary.api-secret` as a secret and never commit it to version control.
- Provide credentials via a local `application.properties`, environment variables, or a secret manager.
- If you have multiple Cloudinary environments (dev/staging/production), use separate accounts or environment-specific configuration.
