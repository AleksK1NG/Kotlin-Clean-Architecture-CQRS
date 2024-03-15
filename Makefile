.PHONY:

# ==============================================================================
# Docker

local:
	@echo Starting local docker compose
	docker-compose -f docker-compose.yaml up -d --build

docker:
	@echo Starting dev docker compose
	./gradlew jibDockerBuild
	docker-compose -f docker-compose-dev.yaml up -d --build

# ==============================================================================
# Docker and k8s support grafana - prom-operator

FILES := $(shell docker ps -aq)

down-local:
	docker stop $(FILES)
	docker rm $(FILES)

clean:
	docker system prune -f

logs-local:
	docker logs -f $(FILES)