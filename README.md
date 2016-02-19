# akka-http-crypto
A small study project how to create an akka-http web service service fast!
    
# Calling the web service
Service | Verb | [httpie](https://github.com/jkbrzt/httpie) | Description
--------|------|--------|-------------
crypto/aes/encrypt | post | http post localhost:8080/crypto/aes/encrypt text=='Hello World!' | Encrypt text using aes
crypto/aes/decrypt | post | http post localhost:8080/crypto/aes/decrypt encrypted=='+9HboInOM5FlhGkEKv24xlbr/6Snkl08Cv8GaJuye+o=' | Decrypt text using aes
crypto/bcrypt/hash | post | http post localhost:8080/crypto/bcrypt/hash text=='Hello World!' | Generate a hash using BCrypt 
crypto/bcrypt/validate | post | http post localhost:8080/crypto/bcrypt/validate candidate=='Hello World!' hash=='$2a$15$GtS/Z856gzrPf382QmAfHOdm1K5ZqtqamnMZOpaG7XWoXNyxfPNdq' | Validate the candidate text with the Bcrypt hash

Have fun!