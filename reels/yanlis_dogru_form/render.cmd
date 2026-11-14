@echo off
rem overlay.html -> 1080x1920 PNG (Chrome headless, 2x). Cerceve/HUD/altyazi seffaf, son kart opak.
set CHROME="C:\Program Files\Google\Chrome\Application\chrome.exe"
set HERE=%~dp0
set URL=file:///%HERE:\=/%overlay.html
set OUT=%HERE%out\
if not exist "%OUT%" mkdir "%OUT%"
set OPTS=--headless=new --hide-scrollbars --force-device-scale-factor=2 --window-size=540,960 --default-background-color=00000000 --virtual-time-budget=8000

%CHROME% %OPTS% --screenshot="%OUT%cerceve.png"            "%URL%?v=frame"
%CHROME% %OPTS% --screenshot="%OUT%hud_yanlis_basla.png"   "%URL%?v=hud&p1=go"
%CHROME% %OPTS% --screenshot="%OUT%hud_yanlis_uyari.png"   "%URL%?v=hud&p1=warn"
%CHROME% %OPTS% --screenshot="%OUT%hud_yanlis_puan.png"    "%URL%?v=hud&p1=rep:1:45"
%CHROME% %OPTS% --screenshot="%OUT%hud_dogru_basla.png"    "%URL%?v=hud&p2=go"
%CHROME% %OPTS% --screenshot="%OUT%hud_dogru_tekrar.png"   "%URL%?v=hud&p2=rep:1:98"
%CHROME% %OPTS% --screenshot="%OUT%yazi_kanca.png"         "%URL%?v=cap&k=kanca"
%CHROME% %OPTS% --screenshot="%OUT%yazi_urun.png"          "%URL%?v=cap&k=urun"
%CHROME% %OPTS% --screenshot="%OUT%son_kart.png"           "%URL%?v=end"
rem Yalnizca yerlesimi gormek icin (kurguda kullanilmaz): cerceve + iki HUD, pencerelerde gri yer tutucu
%CHROME% %OPTS% --screenshot="%OUT%_onizleme.png"          "%URL%?v=hud&p1=warn&p2=rep:1:98&mock=1"
