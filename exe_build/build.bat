:: 此文件必须在compile目录下运行！
set PROJECT_NAME="qqrobot-normal"

:: 清理输出目录
cd ..
rmdir /s /q .\build

:: 构建
call gradlew bootJar
cd exe_build

copy "%PROJECT_NAME%.exe4j" ..\build\libs\
cd ..\build\libs
exe4jc "%PROJECT_NAME%.exe4j"
