## Docker build commands

## Build multi-platform Docker image for part-order-service

### Build a multi-platform Docker image for `linux/amd64` and `linux/arm64` architectures using Docker Buildx.

```bash
DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
-t ram1uj/part-order-service .
```

### Explanation of the command:
- `DOCKER_BUILDKIT=1`: Enables Docker BuildKit, which is required for using Buildx features.
- `docker buildx build`: Invokes the Buildx command to build Docker images.
- `--platform linux/amd64,linux/arm64`: Specifies the target platforms for the build. In this case, it targets both `linux/amd64` and `linux/arm64` architectures.
- `-t ram1uj/part-order-service`: Tags the resulting image with the name `ram1uj/part-order-service`.
- `.`: Indicates the build context, which is the current directory.
- This command will create a multi-platform Docker image that can run on both AMD64 and ARM64 architectures.

### Note:
To push the built image to a Docker registry, you can add the `--push` flag
to the command:

```bash
DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
-t ram1uj/part-order-service \
--push .
```

## Build multi-platform Docker image for part-inventory-service

### Build a multi-platform Docker image for `linux/amd64` and `linux/arm64` architectures using Docker Buildx.

```bash
DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
-t ram1uj/part-inventory-service .
```

### Push the built image to a Docker registry
To push the built image to a Docker registry, you can add the `--push` flag
to the command:
```bash
DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
-t ram1uj/part-inventory-service \
--push .
```


