javac -d ./compile/ TestExecutor.java Main.java
if ( 0 -eq ${LASTEXITCODE} ){
    java -cp ./compile/ TestExecutor.java
}
