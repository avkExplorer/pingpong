# pingpong

An app on the AVK platform, created with `python -m avk new-app pingpong`. Spring Boot 4 on Java 21.

| | |
|---|---|
| Test copy (stg) | http://127.0.0.1:4822/ |
| Production | https://pingpong.avkaiexplorer.com |
| Pipeline | pull request gates (`ci`), then on merge to `main` a signed release that waits for the owner's approval of `prod` |

## Work on it
```
.\check.cmd                 # build, all tests, coverage gate (the same gate the pipeline runs)
python -m avk stg up pingpong --build    # from the platform repo: run it on stg
```
Every change goes through a pull request: the pipeline scans for secrets, vulnerable dependencies and licences, builds and scans the image,
and runs CodeQL. Merging starts a release; nothing reaches prod without the owner's approval and a valid signature.

## What is in here
- `src/main/java/.../observability`: request ids, an access log line without personal data, metrics on a private port.
- `avk.yaml`: the contract with the platform (port, health paths, smoke checks, environment names). `.github/workflows`: the pipeline callers.
- `e2e/`: browser and API checks that run against the built image in the pipeline.
- Bearer-token security (`auth/SecurityConfig`): callers send an access token from the shared login; `/api/v1/me` shows who they are.
