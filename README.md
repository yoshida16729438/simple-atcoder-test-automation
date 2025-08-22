# simple-atcoder-test-automation
[日本語版](./README_JP.md)

## abstract
This project consists of two applications, sample downloading tool (AtCoderHelper) and testing tool (TestExecutor). \
AtCoderHelper is an interactive tool with command line interface. It can detect input and output samples from contest page and download them. \
TestExecutor is a command line tool. It executes Main.java with inputs downloaded by AtCoderHelper and asserts the outputs. \
These tools provides you very simple and a few functionalities, but that means you can easily understand the source code and you can customize it. \
This project only supports Java to test by default, but you only have to make your own testing tool for language you want. AtCoderHelper can easily be customized to collaborate with that. \
This project only supports Windows as execution environment by default, but you can also easily customize by creating shell scripts.

## differentiation points with other tools
### strong points
- simple and small projects which are easiliy understandable and customizable
- interactive interface provides you to operate with simple, short and understandable commands
### weak points
- login, submission, and downloading samples during contests are not supported
- default language support is only for Java

## composition
Each tools consist of files below:
- AtCoderHelper:
  - start.ps1
  - AtCoderHelper.java
  - Constants.java
- TestExecutor:
  - execute_tests.ps1
  - TestExecutor.java
  - Constants.java

## definition of terms
- contest name and task name
  If you are required to input "contest name" or "task (name)" by AtCoderHelper, you need to see the URL of the contest task page. \
  https://atcoder.jp/contests/[contest name]/tasks/[contest name]_[task (name)]

## how to use
Firstly, please download this repository from "Download ZIP" button. \
("git clone" is not recommended since you will rewrite Main.java so much times.)

### AtCoderHelper
Execute start.ps on Powershell to start the tool. \
This tool will keep running until you input the exit command. \
Commands below are available:
- contest
  This tool will switch the target contest (for example, "abc416", "arc123", etc.).
- task
  This tool will switch the target task (for example, "a", "d", etc. which mean "abc416_a" or "arc123_d" or some other contests, depends on the contest name you input with "contest" command) and download sample data. \
  Sample inputs and outputs are downloaded to "./testdata/input/" and "./testdata/answer/". (Directory names can be configured by editing Constants.java)
- test
  This tool will start TestExecutor.
- exit
  This tool will stop.
Available commands are always shown on the terminal, so you don't have to remember these.

### TestExecutor
Execute execute_tests.ps1 on Powershell to run the tool. \
This tool will automatically stop when all test cases are executed. \
This tool will read input data from "./testdata/input/" and save outputs made by Main.java to "./testdata/output/", and assert the outputs with "./testdata/answer/". (Directory names can be configured by editing Constants.java) \
You can get assertion results and elapsed time of each samples from standard output. \
This tool don't have commands or input parameters.

## notes
- AtCoderHelper will automatically detect sample inputs and outputs but its detection logic is very simple, so will fail in some tasks. (ex. some old contests like before abc013, when there were some different ways of using html "\<pre\>" tag like abc411_d, or maybe some interactive tests)
- Since AtCoderHelper doesn't login to AtCoder and AtCoder requires login to access to task pages during contests, sample downloading funtionality is unavailable during a contest is being held. \
  (I won't implement login process, but thinking of creating a Chrome extension which helps downloading samples.)
- Assertion of TestExecutor is very simple, only one difference of whitespace will make it fail. \
  Sometimes testing system of AtCoder will make it AC while TestExecutor results are "failed", so you should check output folder to clarify the cause of fail, assertion failure or wrong answer.
- If some problems occur and need to stop forcibly, press Ctrl+C.
