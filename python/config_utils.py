import os

import yaml

from file_utils import file_to_string

# Constants
DEPLOY_SERVER_CONFIG_FILEPATH = "config/server/deploy.yaml"
TEST_SERVER_CONFIG_FILEPATH = "config/server/test.yaml"
DEPLOY_CLIENT_CONFIG_FILEPATH = "config/client/deploy.yaml"
TEST_CLIENT_CONFIG_FILEPATH = "config/client/test.yaml"

def load_config(is_server: bool) -> dict:
    env = os.getenv("PY_ENV", "test")

    if env == "deploy":
        if is_server:
            return yaml.safe_load(file_to_string(DEPLOY_SERVER_CONFIG_FILEPATH))
        else:
            return yaml.safe_load(file_to_string(DEPLOY_CLIENT_CONFIG_FILEPATH))
    elif env == "test":
        if is_server:
            return yaml.safe_load(file_to_string(TEST_SERVER_CONFIG_FILEPATH))
        else:
            return yaml.safe_load(file_to_string(TEST_CLIENT_CONFIG_FILEPATH))
    else:
        raise NotImplementedError(f"Cannot load configs for unknown env: {env}")