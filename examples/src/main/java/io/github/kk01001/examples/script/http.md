```shell
# 执行Groovy脚本
curl -X POST "http://localhost:8080/examples/script/groovy?scriptId=test1" \
     -H "Content-Type: text/plain" \
     -d 'def name = params.name; def timestamp = params.timestamp; return "Hello ${name} at ${timestamp}"'

```

```shell

# 执行JavaScript脚本
curl -X POST "http://localhost:8080/examples/script/javascript?scriptId=test2" \
     -H "Content-Type: text/plain" \
     -d 'const name = params.name; const timestamp = params.timestamp; `Hello ${name} at ${timestamp}`;'

```

```shell

# 执行Java脚本
curl -X POST "http://localhost:8080/examples/script/java?scriptId=test3" \
     -H "Content-Type: text/plain" \
     -d 'public class TestScript { public Object execute(Map<String, Object> params) { return "Hello " + params.get("name"); } }'


```