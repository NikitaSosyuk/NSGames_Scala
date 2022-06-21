#!/bin/bash

brew install jq

echo "Enjoy the best service in the world"

echo "Creating user..."
curl -X POST http://localhost:80/auth/signup -d @user.json --header "Content-Type: application/json"

echo "Login..."
curl -X POST http://localhost:80/auth/login -d @login.json --header "Content-Type: application/json"

# Если бы кто-то объяснил как сохранить токен, то я бы сделал запросы дальше