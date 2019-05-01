FROM envoyproxy/envoy-dev:latest

RUN apt-get update && apt-get -q install -y \
    curl

RUN cat /etc/hosts && cat /etc/resolv.conf
    
COPY ./envoy-proxy.yaml /etc/front-envoy.yaml
CMD /usr/local/bin/envoy -c /etc/front-envoy.yaml --service-cluster chat-service
