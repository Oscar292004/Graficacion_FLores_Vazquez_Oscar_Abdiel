# MeowCompilator
Bienvenido a nuesto compilador de gatitos! :3

## Previos
Se necesita lo siguiente:
  - Tener instaladado java
  - Instalar y configurar javacc
  - Un IDE o editor de texto

## Ejecución
Para usar este compilador, se debe de clonar el repo y posteriormente, ejecutar estos comandos
  1. javacc MeowCompilerLexico.jj           <- Genera los archivos fuente .java
  2. javac *.java                           <- Compila todos los archivos .java
  3. java MeowCompilerLexico < prueba.txt   <- Ejecuta y lee el archivo .txt para verificar la parte léxica
