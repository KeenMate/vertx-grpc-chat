# Envoy in the middle

### Build Envoy proxy using docker
Assuming you are in this directory in terminal (also where Dockerfile takes place) execute this: `docker build -t YOUR_NAMESPACE:DOCKER_IMAGE_NAME .`
And dont forget to add '.' at the end.

### Start built docker image
`docker run -d --network host YOUR_NAMESPACE:DOCKER_IMAGE_NAME`
`-d` for detached mode - docker container's process will not be locked to current terminal
`--network host` to prevent docker from creating separated sub-network on your network card which is default..
Last information is how you named your docker container
