@echo off
rem overlay.html -> 1080x1920 PNG (Chrome headless, 2x). HUD katmanlari seffaf, son kart opak.
set CHROME="C:\Program Files\Google\Chrome\Application\chrome.exe"
set HERE=%~dp0
set URL=file:///%HERE:\=/%overlay.html
set OUT=%HERE%out\
if not exist "%OUT%" mkdir "%OUT%"
set OPTS=--headless=new --hide-scrollbars --force-device-scale-factor=2 --window-size=540,960 --default-background-color=00000000 --virtual-time-budget=8000

%CHROME% %OPTS% --screenshot="%OUT%hud_tekrar_0_uyari.png"    "%URL%?v=hud&n=0&mode=warn"
%CHROME% %OPTS% --screenshot="%OUT%hud_tekrar_1.png"          "%URL%?v=hud&n=1&score=98"
%CHROME% %OPTS% --screenshot="%OUT%hud_tekrar_1_mukemmel.png" "%URL%?v=hud&n=1&msg=mukemmel"
%CHROME% %OPTS% --screenshot="%OUT%son_kart.png"              "%URL%?v=end"
