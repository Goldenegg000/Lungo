# Getting Started

run the App.main function and use -h to get help about the flags.

## Folder Structure

- `src`: the folder to maintain sources.
- `lib`: the folder to maintain dependencies.

the compiled output files will be generated in the `bin` folder by default.<br>
using the `java: Export Jar...` will output the jar file in the "/Lungo/" directory.

## dependency's

- ### for python tester

  - needs pyyaml.

- ### for Lungo Browser

  - java version 17 or higher.

No other dependency's are required.

## how to run

- have java 17 or higher installed and it can be launched using the `java` command
  - you can check this by typing `java --version` and it should say java 17 or higher

<br>

- then using powershell run the `run.ps1` file by typing `powershell ./run.ps1`
  - you can add flags by typing for example to enable debug: `powershell ./run.ps1 -d` or `powershell ./run.ps1 -debug` only use one `-` (Dash).
