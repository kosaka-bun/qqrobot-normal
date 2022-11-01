:: 此文件必须在compile目录下运行！
set PROJECT_NAME="qqrobot-normal"

:: 清理输出目录
cd ..
del /s /q .\build\libs\*
for /r .\build\libs /d %%a in (*) do rmdir /s /q "%%a"

:: 构建
call gradlew bootJar
cd compile

copy "%PROJECT_NAME%.exe4j" ..\build\libs\
cd ..\build\libs
exe4jc "%PROJECT_NAME%.exe4j"
