FUNCTION_HOST=${1:localhost:8080}
CMD="curl -X POST --header \"Content-Type: application/json\" $FUNCTION_HOST/invoke -d @request.json"
echo $CMD
eval $CMD
