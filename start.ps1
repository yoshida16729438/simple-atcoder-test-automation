$Host.ui.RawUI.WindowTitle = "AtCoder Helper"
javac -d ./compile/ AtCoderHelper.java Constants.java
if ( 0 -eq ${LASTEXITCODE} ){
    java -cp ./compile/ AtCoderHelper.java
}
