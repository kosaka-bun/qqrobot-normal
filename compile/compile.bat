set PROJECT_NAME="qqrobot-normal"

cd ..
del /s /q .\build\libs\*
for /r .\build\libs /d %%a in (*) do rmdir /s /q "%%a"
call gradlew bootJar
cd compile

copy "%PROJECT_NAME%.exe4j" ..\build\libs\
cd ..\build\libs
exe4jc "%PROJECT_NAME%.exe4j"
