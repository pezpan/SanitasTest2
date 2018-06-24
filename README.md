# Readme

Para cubrir el codigo con las pruebas, definiremos una clase de Test por cada clase que queremos testear. 
Al estar el codigo refactorizado en componentes de Spring, y siendo éstos publicos, podemos definir mocks para cada dependencia que necesite la clase a testear
como se ha hecho a modo de ejemplo en los ficheros de test incluidos. Estos mocks se inyectaran en el objeto de la clase a testear
con la anotacion @InjectMocks. Se podrán inicializar los mocks definiendo la salida deseada para cada entrada, de forma que el objeto
de la clase a testear tenga los datos necesarios para su ejecucion. 

# Ejecucion

1. Instalar el jar sportalclientesweb en el repositorio local con el comando mvn install:install-file -Dfile=sportalclientesweb-1.19.0.jar -DgroupId=sanitas.bravo.clientes -DartifactId=sportalclientesweb -Dversion=1.19.0 -Dpackaging=jar

2. Actualizar las dependencias y generar el jar con el comando mvn clean install. Este comando además ejecutará los test.
