@echo off
rem reels.html -> 1080x1920 Reels PNG'leri (Chrome headless, 2x, seffaf arka plan)
set CHROME="C:\Program Files\Google\Chrome\Application\chrome.exe"
set HERE=%~dp0
set URL=file:///%HERE:\=/%reels.html
set OUT=%HERE%out\
if not exist "%OUT%" mkdir "%OUT%"
set OPTS=--headless=new --hide-scrollbars --force-device-scale-factor=2 --window-size=540,960 --default-background-color=00000000 --virtual-time-budget=8000

%CHROME% %OPTS% --screenshot="%OUT%01_cap_salonda_herkes.png"     "%URL%?v=cap1"
%CHROME% %OPTS% --screenshot="%OUT%02_poster_neon_afis.png"       "%URL%?v=poster"
%CHROME% %OPTS% --screenshot="%OUT%03_cap_aydinlanma_ani.png"     "%URL%?v=cap2"
%CHROME% %OPTS% --screenshot="%OUT%04_cap_yapay_zeka.png"         "%URL%?v=cap3"
%CHROME% %OPTS% --screenshot="%OUT%05_hud_tekrar_1.png"           "%URL%?v=hud&n=1"
%CHROME% %OPTS% --screenshot="%OUT%06_hud_tekrar_2.png"           "%URL%?v=hud&n=2"
%CHROME% %OPTS% --screenshot="%OUT%07_hud_uyari_curl.png"         "%URL%?v=hud&mode=warn&msg=curl&n=2&t=00:07"
%CHROME% %OPTS% --screenshot="%OUT%08_hud_tekrar_3.png"           "%URL%?v=hud&n=3"
%CHROME% %OPTS% --screenshot="%OUT%09_end_kapanis_seffaf.png"     "%URL%?v=end"
%CHROME% %OPTS% --screenshot="%OUT%09b_end_kapanis_opak.png"      "%URL%?v=end&solid=1"
