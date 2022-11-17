:: 此文件必须在exe_build目录下运行！
set PROJECT_NAME="qqrobot-normal"

:: 清理输出目录，构建
cd ..
call gradlew clean bootJar
cd exe_build

copy "%PROJECT_NAME%.exe4j" ..\build\libs\
cd ..\build\libs
exe4jc "%PROJECT_NAME%.exe4j"
