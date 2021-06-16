#!/usr/bin/env bash

set -euox pipefail

test_dir="$(cd "$(dirname "${0}")" &>/dev/null && pwd)"
runtime_output_logfile="$(mktemp)"
function_invocation_response_file="$(mktemp)"
port=54321

pushd "${test_dir}"
./mvnw compile
popd

"${test_dir}"/../../../sf-fx-runtime-java serve -p "${port}" "${test_dir}" >"${runtime_output_logfile}" &
runtime_pid=$!

# The curl version used by CircleCI does not correctly support curl's --retry-connrefused
# which would work around having this fixed sleep in here. We should revisit this code in the future
# to see if we can get rid of this sleep then.
sleep 5

curl "http://localhost:${port}" \
	-d '{"name": "Johanna", age: 5}' \
	-H "Content-Type: application/json" \
	-H "ce-specversion: 1.0" \
	-H "ce-id: 1b4188b6710148d8ec2bdb7f" \
	-H "ce-source: urn:source" \
	-H "ce-type: com.salesforce.function.invoke.sync" \
	-H "ce-time: 2020-09-03T20:56:28.297915Z" \
	-H "ce-sfcontext: eyJhcGlWZXJzaW9uIjoiNTAuMCIsInBheWxvYWRWZXJzaW9uIjoiMC4xIiwidXNlckNvbnRleHQiOnsib3JnSWQiOiIwMER4eDAwMDAwMDZJWUoiLCJ1c2VySWQiOiIwMDV4eDAwMDAwMVg4VXoiLCJvbkJlaGFsZk9mVXNlcklkIjpudWxsLCJ1c2VybmFtZSI6InRlc3QtenFpc25mNnl0bHF2QGV4YW1wbGUuY29tIiwic2FsZXNmb3JjZUJhc2VVcmwiOiJodHRwOi8vcGlzdGFjaGlvLXZpcmdvLTEwNjMtZGV2LWVkLmxvY2FsaG9zdC5pbnRlcm5hbC5zYWxlc2ZvcmNlLmNvbTo2MTA5Iiwib3JnRG9tYWluVXJsIjoiaHR0cDovL3Bpc3RhY2hpby12aXJnby0xMDYzLWRldi1lZC5sb2NhbGhvc3QuaW50ZXJuYWwuc2FsZXNmb3JjZS5jb206NjEwOSJ9fQ==" \
	-H "ce-sffncontext: eyJhY2Nlc3NUb2tlbiI6IjAwRHh4MDAwMDAwNklZSiFBUUVBUU5SYWM1YTFoUmhoZjAySFJlZ3c0c1NadktoOW9ZLm9oZFFfYV9LNHg1ZHdBZEdlZ1dlbVhWNnBOVVZLaFpfdVkyOUZ4SUVGTE9adTBHZjlvZk1HVzBIRkxacDgiLCJmdW5jdGlvbkludm9jYXRpb25JZCI6bnVsbCwiZnVuY3Rpb25OYW1lIjoiTXlGdW5jdGlvbiIsImFwZXhDbGFzc0lkIjpudWxsLCJhcGV4Q2xhc3NGUU4iOm51bGwsInJlcXVlc3RJZCI6IjAwRHh4MDAwMDAwNklZSkVBMi00WTRXM0x3X0xrb3NrY0hkRWFaemUtLU15RnVuY3Rpb24tMjAyMC0wOS0wM1QyMDo1NjoyNy42MDg0NDRaIiwicmVzb3VyY2UiOiJodHRwOi8vZGhhZ2Jlcmctd3NsMTo4MDgwIn0=" \
	-H "Authorization: C2C eyJ2ZXIiOiIxLjAiLCJraWQiOiJDT1JFLjAwRHh4MDAwMDAwNklZSi4xNTk5MTU5NjQwMzUwIiwidHlwIjoiand0IiwiY2x2IjoiSjIuMS4xIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJwbGF0Zm9ybS1mdW5jdGlvbnMiLCJhdXQiOiJTRVJWSUNFIiwibmJmIjoxNTk5MTY2NTU4LCJjdHgiOiJzZmRjLnBsYXRmb3JtLWZ1bmN0aW9ucyIsImlzcyI6ImNvcmUvZGhhZ2Jlcmctd3NsMS8wMER4eDAwMDAwMDZJWUpFQTIiLCJzdHkiOiJUZW5hbnQiLCJpc3QiOjEsImV4cCI6MTU5OTE2NjY3OCwiaWF0IjoxNTk5MTY2NTg4LCJqdGkiOiJDMkMtMTA3NTg2OTg1NTMxNTMyOTkzMjE3OTEyMzQwNTIzMjgzOTEifQ.jZZ4ksYlq0vKtBf0yEfpJVL2yYh3QHOwp0KCk-QxzDyF_7VARB-N74Cqpj2JWhVP4TcBLGXYuldB-Sk6P5HlGQ" \
	>"${function_invocation_response_file}"

kill "${runtime_pid}"

grep -xq '{"result":"Hello Johanna, you are 5 years old."}' "${function_invocation_response_file}"
grep -q "Found function: com.salesforce.PojoGsonFunction" "${runtime_output_logfile}"
