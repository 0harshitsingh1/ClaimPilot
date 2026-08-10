echo "--- b. POST /api/auth/login ---"
LOGIN_RES=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"admin@expenseflow.com","password":"Admin@123"}' http://localhost:8080/api/auth/login)
echo $LOGIN_RES
ACCESS_TOKEN=$(echo $LOGIN_RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo $LOGIN_RES | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)

echo -e "\n--- c. Unauthenticated request to protected endpoint ---"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/dummy-protected
echo ""

echo -e "\n--- d. Authenticated request to protected endpoint ---"
# Since dummy-protected might not exist, I'll hit /api/auth/refresh without token just to see if it's protected, wait, I'll just check if there's any protected endpoint, or just create a dummy one real quick if it doesn't exist. Actually AuthControllerIntegrationTest has "/api/v1/dummy-protected" but wait, did I create a dummy protected endpoint in AuthController? Let's check if it exists or use some other one. Let's just use /api/v1/dummy-protected
curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:8080/api/v1/dummy-protected
echo ""

echo -e "\n--- e. POST /api/auth/refresh ---"
REFRESH_RES=$(curl -s -X POST -H "Content-Type: application/json" -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" http://localhost:8080/api/auth/refresh)
echo $REFRESH_RES
NEW_ACCESS_TOKEN=$(echo $REFRESH_RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

echo -e "\n--- f. POST /api/auth/logout then refresh ---"
curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $NEW_ACCESS_TOKEN" -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" http://localhost:8080/api/auth/logout
echo "Refresh after logout:"
curl -s -X POST -H "Content-Type: application/json" -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" http://localhost:8080/api/auth/refresh
echo ""
