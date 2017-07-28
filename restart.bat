java -cp bin;lib/* com.game.GameStop
@echo 不要在意上面的报错，开始更新了,大概需要10s...
@echo off
set a=10
:dao
set /a a=a-1
ping -n 2 -w 500 127.1>nul
@echo 倒计时：%a%
if %a%==0 (goto next) else (goto dao)

:next
@echo 更新配置
start.bat
pause
