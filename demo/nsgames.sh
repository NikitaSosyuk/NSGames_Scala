#!/bin/bash

# Я вообще первый раз на bash пишу, не злитесь

printf "Creating user...\n"
curl -X POST http://localhost:80/auth/signup -d @user.json --header "Content-Type: application/json"

printf "\n\n"
printf "Login...\n"
curl -X POST http://localhost:80/auth/login -d @login.json --header "Content-Type: application/json"

printf "\n\n"
printf "Enter the token: "
read

printf "\n\n"
printf "Creating article...\n"

curl -X POST http://localhost:80/article/create -d @article.json -H "Content-Type: application/json" -H "Authorization: $REPLY"
printf "\n\n"

printf "Creating comments...\n"
curl -X POST http://localhost:80/comment/create -d @comment.json -H "Content-Type: application/json" -H "Authorization: $REPLY"
printf "\n\n"

printf "Creating feedback...\n"
curl -X POST http://localhost:80/feedback/create -d @feedback.json -H "Content-Type: application/json" -H "Authorization: $REPLY"

printf "Getting list of articles...\n"
curl -X GET http://localhost:80/article/list -H "Authorization: $REPLY"
printf "\n\n"

printf "Getting list of articles headers...\n"
curl -X GET http://localhost:80/article/headers -H "Authorization: $REPLY"
printf "\n\n"

printf "Getting list of comment for article with id = 1...\n"
curl -X GET http://localhost:80/comment/list/1 -H "Authorization: $REPLY"
printf "\n\n"