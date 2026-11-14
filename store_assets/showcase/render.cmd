@echo off
rem showcase.html -> Play Store PNG'leri (Chrome headless, 2x)
set CHROME="C:\Program Files\Google\Chrome\Application\chrome.exe"
set HERE=%~dp0
set URL=file:///%HERE:\=/%showcase.html
set OPTS=--headless=new --hide-scrollbars --force-device-scale-factor=2 --virtual-time-budget=8000

%CHROME% %OPTS% --window-size=1920,1080 --screenshot="%HERE%arfitness_showcase_3840x2160.png" "%URL%"
%CHROME% %OPTS% --window-size=960,1080 --screenshot="%HERE%arfitness_showcase_01_1920x2160.png" "%URL%?part=left"
%CHROME% %OPTS% --window-size=960,1080 --screenshot="%HERE%arfitness_showcase_02_1920x2160.png" "%URL%?part=right"
