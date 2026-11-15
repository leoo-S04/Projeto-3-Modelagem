@echo off
rem ---------------------------------------------------------------------------
rem Regenera, no .asta da equipe, os diagramas do Trabalho 03 e exporta PNG + PDF.
rem   - 10 diagramas de classes (um por padrao), 10 de sequencia (um por cenario R01..R10)
rem   - maquina de estados do Livro atualizada
rem Requisitos: Astah UML instalado (padrao: C:\Program Files\astah-UML) e um JDK 17+ no PATH.
rem Uso (na raiz do projeto):  ferramentas\astah\gerar.cmd
rem Pode ser executado de novo: o que foi gerado antes e apagado e recriado a partir do codigo.
rem ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0\..\.."
if "%ASTAH_HOME%"=="" set "ASTAH_HOME=C:\Program Files\astah-UML"
if not exist "%ASTAH_HOME%\astah-api.jar" (
  echo Astah nao encontrado em "%ASTAH_HOME%". Defina a variavel ASTAH_HOME.
  exit /b 1
)
set "ASTA=PROJETO 2- Leo T, Vicente A, Pedro B e Gustavos L.asta"
set "OUT=target\astah"
set "JAVA_ASTAH=%ASTAH_HOME%\jre\bin\java.exe"
set "PNG=%OUT%\png\PROJETO 2- Leo T, Vicente A, Pedro B e Gustavos L"
set "DESTINO=target\astah\diagramas"

echo [1/5] Compilando o projeto (as classes sao lidas do codigo compilado)...
call .\mvnw.cmd -q compile dependency:build-classpath -Dmdep.outputFile=target\cp.txt || exit /b 1

echo [2/5] Compilando o gerador...
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%\bin"
javac --release 17 -encoding UTF-8 -d "%OUT%\bin" -cp "%ASTAH_HOME%\astah-api.jar;%ASTAH_HOME%\lib\itext-1.3.3.jar" ferramentas\astah\GeradorDiagramasT03.java ferramentas\astah\MontarPdf.java || exit /b 1

echo [3/5] Gerando os diagramas no .asta...
"%JAVA_ASTAH%" -Xshare:off -Dfile.encoding=UTF-8 "-Djava.library.path=%ASTAH_HOME%\lib\rlm" -cp "%OUT%\bin;%ASTAH_HOME%\astah-api.jar;%ASTAH_HOME%\astah-uml.jar;%ASTAH_HOME%\lib\*" GeradorDiagramasT03 "%ASTA%" "%OUT%\gerado.asta" target\classes src\main\java target\cp.txt || exit /b 1
copy /y "%OUT%\gerado.asta" "%ASTA%" >nul

echo [4/5] Exportando as imagens...
"%JAVA_ASTAH%" -Xshare:off "-Djava.library.path=%ASTAH_HOME%\lib\rlm" -cp "%ASTAH_HOME%\astah-uml.jar" com.change_vision.jude.cmdline.JudeCommandRunner -image all -f "%CD%\%ASTA%" -t png -o "%CD%\%OUT%\png" >nul
if exist "%DESTINO%" rmdir /s /q "%DESTINO%"
mkdir "%DESTINO%"
copy /y "%PNG%\Livro\Maquina de Estados - Livro.png" "%DESTINO%\" >nul
for /d %%d in ("%PNG%\Req *") do copy /y "%%d\*.png" "%DESTINO%\" >nul

echo [5/5] Montando o PDF...
"%JAVA_ASTAH%" -Xshare:off -cp "%OUT%\bin;%ASTAH_HOME%\lib\itext-1.3.3.jar" MontarPdf "docs\uml\Trabalho03_Diagramas.pdf" "Trabalho 03 - Diagramas UML (padroes GoF)" "%DESTINO%" || exit /b 1
echo Pronto: "%ASTA%" e docs\uml\Trabalho03_Diagramas.pdf (imagens soltas em %DESTINO%)
endlocal
