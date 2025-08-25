$Host.ui.RawUI.WindowTitle = "AtCoder Helper"
javac -encoding UTF-8 -d ./compile/ AtCoderHelper.java Constants.java
if ( 0 -eq ${LASTEXITCODE} ){
    java -cp ./compile/ AtCoderHelper
}
