@echo off
chcp 65001 > nul
call gradlew.bat run --console=plain
pause
