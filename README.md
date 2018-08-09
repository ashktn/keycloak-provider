# keycloak-provider
Custom keycloak provider with user storage in database

Clone the repo and type:
`mvn clean install`

Create a datasource in standalone.xml pointing to your database where all your users reside.

Create realm and client in your keycloak admin console.

Deploy this jar file in keycloak server and start the server.

