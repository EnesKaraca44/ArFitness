# Sakatlanmayı Önle — Yanlış vs Doğru Form (bölünmüş ekran, kurumsal reels)

Google Flow (Veo 3.1) için kopyala-yapıştır promptlar. Promptlar İngilizce (model İngilizce tarifi daha iyi izliyor),
ekrandaki bütün Türkçe yazılar `out/` klasöründeki hazır PNG katmanlarından gelir.

Promptlar henüz Flow'da denenmedi; ilk üretimlerde küçük düzeltme gerekebilir (bkz. "Sorun çıkarsa").

## Fikir

Hareket: **şınav**. Sebep: uygulamanın gerçekten yakaladığı ve bel sağlığıyla doğrudan ilgili form hatası burada.
Kalça düştüğünde `PushupAnalyzer` kırmızı kartla **"Kalçanı düz tut, vücudun tek çizgi olsun"** uyarısını verir
ve o tekrarın form puanını düşürür. Videoda gösterilen her yazı uygulamanın gerçek mesajıdır.

| Bölüm | Süre | Ne görünüyor |
|---|---|---|
| Kanca | 2.5 sn | Sporcu kötü formla şınav çekerken belini tutuyor |
| Bölünmüş ekran | 7 sn | Üstte YANLIŞ (kalça düşük, kırmızı uyarı, Form %45), altta DOĞRU (düz vücut, Form %98) |
| Ürün çekimi | 4 sn | Telefon yerde dambıla yaslı, sporcu arkada temiz şınav çekiyor |
| Son kart | 3 sn | Logo + "Şimdi Google Play'de" |

Toplam yaklaşık 16–17 sn.

## Çalışma sırası

1. **İki görsel üret** (Bölüm 1): sporcu referansı (G1) ve bölünmüş ekranın başlangıç karesi (G2).
2. **Dört klip üret** (Bölüm 2): K1 kanca, K2 yanlış, K3 doğru, K4 ürün çekimi.
3. **Kurgu** (Bölüm 3): klipleri çerçevenin altına yerleştir, HUD katmanlarını bindir, seslendirmeyi ekle.

Bölünmüş ekranın iki yarısı aynı kareden başlamalı. Bu yüzden K2 ve K3'ü **aynı G2 görselini ilk kare vererek**
üret: iki pencerede aynı adam, aynı salon, aynı kadraj olur; yalnızca hareket farklılaşır.

## Flow ayarları

Kasım 2026'da Flow'un yardım sayfasından alınan bilgilere dayanır; arayüzdeki adlar değişmiş olabilir.

| Üretim | Mod | En-boy | İlk kare / referans |
|---|---|---|---|
| G1 sporcu | Görsel üretimi (Nano Banana Pro) | 9:16 | — |
| G2 başlangıç karesi | Görsel üretimi | **16:9** | G1'i referans ekle |
| K1 kanca | Ingredients to Video | 9:16 | G1 |
| K2 yanlış | **Frames to Video** | **16:9** | İlk kare: G2 |
| K3 doğru | **Frames to Video** | **16:9** | İlk kare: G2 |
| K4 ürün çekimi | Ingredients to Video | 9:16 | G1 |

- K2 ve K3 yatay (16:9) üretilir, çünkü yandan şınav yatay bir harekettir ve dikey videoda üst üste iki yatay
  pencereye oturur.
- Klipler 8 sn çıkar; fazlasını kurguda kes. İstek başına çıktıyı 1 yap (2 seçiliyse kredi iki kat düşer).
- Ingredients modu Veo 3.1 Fast ile çalışır (üretim başına 20 kredi). 4 klip × yaklaşık 3 deneme ≈ 240 kredi.

## 1. Görseller

**G1 — Sporcu referansı (9:16)**

```text
Photorealistic full-body photograph, vertical 9:16, shot on a full-frame camera with a 35mm lens, sharp focus, natural skin texture.
A fit Turkish man in his late 20s with an athletic but natural build (not a bodybuilder), short dark hair with a low fade, light stubble, calm and focused expression. He wears a plain heather-grey fitted crew-neck t-shirt, black training shorts and black training shoes, with no logos or text.
He stands facing the camera with his arms relaxed in a modern premium gym: matte black rubber floor, dark charcoal walls, a rack of black hex dumbbells behind him, soft overhead key light, a subtle cool-blue rim light and one thin red LED strip running along the wall.
Clean, premium, commercial sports-brand look. No text, no logos, no watermark.
```

**G2 — Bölünmüş ekranın başlangıç karesi (16:9, G1 referans olarak ekli)**

```text
Photorealistic photograph, horizontal 16:9, shot on a full-frame camera with a 35mm lens, sharp focus.
The same man from the reference image, in the same heather-grey t-shirt, black shorts and black shoes, holds the top position of a push-up (high plank) in the same gym.
Exact side profile, camera locked off at knee height, perfectly level. His head is on the right side of the frame and his feet are on the left. His whole body is in frame from head to toes, with generous empty space on both sides. The line where his hands and toes touch the floor sits one third up from the bottom of the frame; the upper third of the frame is empty dark charcoal wall with one thin red LED strip.
His arms are fully straight, hands under his shoulders, body in one straight line from head to heels, eyes on the floor.
Matte black rubber floor, soft overhead key light, subtle cool-blue rim light along his back. Clean, premium, commercial look. No text, no logos, no watermark, no other people.
```

Kadrajdaki boşluklar bilerek istendi: sol üst köşeye YANLIŞ/DOĞRU rozeti, sağ üste süre, alttaki zemin şeridine
tekrar sayısı ve mesaj kartı gelecek.

## 2. Klipler

### K1 — Kanca (9:16, Ingredients: G1)

```text
Photorealistic live-action commercial footage, vertical 9:16, cinematic but natural, shallow depth of field, real skin texture and sweat, real-world physics. Not CGI, not animation. No subtitles, no captions, no on-screen text, no logos, no watermark. No speech, no voice-over, no music.

Location: a modern premium gym — matte black rubber floor, dark charcoal walls, a rack of black hex dumbbells, soft overhead key light, a subtle cool-blue rim light and one thin red LED strip along the wall.

THE ATHLETE: a fit Turkish man in his late 20s, athletic but natural build, short dark hair with a low fade, light stubble. Plain heather-grey fitted crew-neck t-shirt, black training shorts, black training shoes, no logos.

Shot: low three-quarter front angle from floor level, slow push-in.
Action: he is struggling through a sloppy push-up, his hips sagging toward the floor and his lower back arched. He gives up halfway, drops to his knees, sits back on his heels, presses one hand against his lower back and winces, then exhales and shakes his head in frustration.
Audio: strained breathing, a short grunt, quiet gym room tone. No music.
```

### K2 — YANLIŞ form (16:9, Frames to Video, ilk kare: G2)

```text
Photorealistic live-action commercial footage, horizontal 16:9, natural look, real skin texture and sweat, real-world physics. Not CGI, not animation. No subtitles, no captions, no on-screen text, no logos, no watermark. No speech, no voice-over, no music.

Shot: one continuous take. Locked-off static camera on a tripod at knee height, exact side profile, perfectly level. No camera movement, no zoom, no cuts. The framing stays exactly as in the first frame: head on the right, feet on the left, whole body in frame.

Action: the man holds the high plank for one second, then performs two slow push-ups with clearly bad form. As he lowers, his hips drop first and sag toward the floor, his lower back arches deeply like a banana, his belly almost touches the floor, his head droops and his elbows flare straight out to the sides. He pushes his chest back up while his hips stay low and lag behind. Each repetition takes about one and a half seconds down and one and a half seconds up. His face shows strain.
Audio: strained breathing, quiet gym room tone. No music.
```

### K3 — DOĞRU form (16:9, Frames to Video, ilk kare: G2)

```text
Photorealistic live-action commercial footage, horizontal 16:9, natural look, real skin texture and sweat, real-world physics. Not CGI, not animation. No subtitles, no captions, no on-screen text, no logos, no watermark. No speech, no voice-over, no music.

Shot: one continuous take. Locked-off static camera on a tripod at knee height, exact side profile, perfectly level. No camera movement, no zoom, no cuts. The framing stays exactly as in the first frame: head on the right, feet on the left, whole body in frame.

Action: the man holds the high plank for one second, then performs two slow, controlled, textbook push-ups. His body stays in one rigid straight line from head to heels the whole time: core braced, hips level, neck neutral, elbows tucked at about 45 degrees from his torso. He lowers until his chest is a fist's height above the floor, then presses back up to fully straight arms. Each repetition takes about one and a half seconds down and one and a half seconds up. Calm, controlled, confident.
Audio: steady controlled breathing, quiet gym room tone. No music.
```

### K4 — Ürün çekimi (9:16, Ingredients: G1)

```text
Photorealistic live-action commercial footage, vertical 9:16, cinematic but natural, shallow depth of field, real-world physics. Not CGI, not animation. No subtitles, no captions, no on-screen text, no logos, no watermark. No speech, no voice-over, no music.

Location: a modern premium gym — matte black rubber floor, dark charcoal walls, a rack of black hex dumbbells, soft overhead key light, a subtle cool-blue rim light and one thin red LED strip along the wall.

THE ATHLETE: a fit Turkish man in his late 20s, athletic but natural build, short dark hair with a low fade, light stubble. Plain heather-grey fitted crew-neck t-shirt, black training shorts, black training shoes, no logos.

Shot: static low-angle shot from floor level. In the sharp foreground on the right, a plain black smartphone stands upright in portrait orientation on the rubber floor, leaning against a black hex dumbbell. We see the phone from behind at a three-quarter angle: its back faces the camera, its screen faces away toward the athlete and is not visible; a soft blue glow from the screen spills onto the floor. In the background, two metres away, the athlete is side-on to the phone.
Action: he performs smooth, textbook push-ups with a perfectly straight body. Halfway through the clip the focus racks slowly from the phone to the athlete.
Audio: steady breathing, quiet gym room tone, one short phone vibration buzz as he finishes a repetition. No music.
```

Telefonun ekranı bilerek gösterilmiyor: video modelleri ekran yazısını bozuyor. Uygulamanın gerçek kullanımına da
uygun: şınavda telefon yere, 1.5–2 m yana konur ve ekranı kullanıcıya bakar.

## 3. Kurgu (CapCut vb.)

Tuval 9:16, 1080×1920. Hazır katmanlar `out/` klasöründe, hepsi 1080×1920; tam ekran koyman yeterli, konum ayarı
gerekmez. Yeniden basmak için `render.cmd`. Yerleşimin bitmiş hâli: `out/_onizleme.png`.

| Zaman | Görüntü | Üstüne | Seslendirme |
|---|---|---|---|
| 0.0–2.5 | K1 (belini tuttuğu an) | `yazi_kanca.png` | "Şınavda kalçan düşüyorsa, yükü belin taşır." |
| 2.5–9.5 | K2 üstte + K3 altta, üstlerinde `cerceve.png` | HUD katmanları (aşağıda) | "Yanlış form, sakatlığa davetiyedir. ArFitness, telefonunun kamerasıyla formunu anlık izler; hatanı anında gösterir, her tekrarını puanlar." |
| 9.5–13.5 | K4 | `yazi_urun.png` | "Telefonu yere koy, başla. On beş harekette yapay zekâ koçun yanında." |
| 13.5–16.5 | `son_kart.png` | — | "ArFitness. Şimdi Google Play'de." |

**Bölünmüş ekranı kurmak** (katman sırası alttan üste: K2, K3, `cerceve.png`, HUD):

- K2'yi %109 ölçekle, kırmızı çerçeveli üst pencereye taşı (pencere merkezi tuval merkezinin 410 px üstünde).
- K3'ü %109 ölçekle, yeşil çerçeveli alt pencereye taşı (merkez 262 px altta).
- Pencereler 1080×660 px: üstteki y 220–880, alttaki y 892–1552. Taşan kenarları çerçeve kapatır.
- İki klibi, ilk şınavın **inişe başladığı kare** aynı ana gelecek şekilde hizala; tempo farkı varsa birinin hızını
  %90–110 arasında oynat.

**HUD sırası** (saniye yerine harekete göre yerleştir; her klipte zamanlama biraz farklı çıkar):

| Pencere | An | Katman | Uygulamadaki karşılığı |
|---|---|---|---|
| Üst (YANLIŞ) | Plank'ta beklerken | `hud_yanlis_basla.png` | "Göğsünü yere doğru indir" |
| Üst | Kalça düşmeye başlayınca | `hud_yanlis_uyari.png` | Kırmızı kart: "Kalçanı düz tut, vücudun tek çizgi olsun" |
| Üst | İlk tekrar bitip kollar düzelince, 1.2 sn | `hud_yanlis_puan.png` | "1. tekrar! Form %45" |
| Üst | İkinci inişte | `hud_yanlis_uyari.png` | Aynı uyarı |
| Alt (DOĞRU) | Başından ilk tekrarın sonuna | `hud_dogru_basla.png` | "Göğsünü yere doğru indir" |
| Alt | İlk tekrar bitip kollar düzelince | `hud_dogru_tekrar.png` | "1. tekrar! Form %98" + sayaç 1 |

Puanlar uydurma değil, `RepScorer` formülünden: puanın yarısı hareketin şekli, yarısı uyarısız geçen karelerin oranı.
Kalça tekrar boyunca düşükse ikinci yarı sıfırlanır ve puan %45 civarına iner; temiz ve derin bir tekrar %97–98 alır.
Uygulama kalça düşükken de tekrarı sayar, yalnızca puanı düşürür; bu yüzden üst pencerede sayaç 1 olur.

**Ses:** Seslendirmeyi CapCut'ın Türkçe sesiyle ya da kendi sesinle ekle; kliplerin kendi sesini %20–30'a indir, K2'yi
tamamen kıs (iki nefes sesi üst üste binmesin). Alt pencerede ilk tekrar bittiğinde kısa bir "bir" sesi eklersen
uygulamanın sesli sayımını da göstermiş olursun. Müziği kurguda ver, otomatik altyazı aç.

**Gerçek uygulama görüntüsü:** kurumsal videoda güven veren şey gerçek arayüzdür. K4 ile son kart arasına 1.5–2 sn
ekran kaydı (ana sayfa → Push-Up → Başla) ya da `uygulamaekrangoruntusu1 (1).jpeg` (ana sayfa) koyabilirsin.
Ekran görüntülerini kullanırken:

- `uygulamaekrangoruntusu1 (4).jpeg` (profil) adını ve e-posta adresini gösteriyor; videoda kullanma.
- `uygulamaekrangoruntusu (2).png` eski (turkuaz) tasarım ve "Vücut tam olarak görünmüyor." hata mesajını taşıyor;
  güncel egzersiz ekranı mavi ve tam ekran kamera. Tanıtımda kullanma.
- Diğerlerinde üstteki durum çubuğunu (bildirim simgeleri, pil) kırp.

## 4. Tek promptla hızlı deneme

Kurguyla uğraşmadan fikri görmek için bölünmüş ekranı Veo'ya tek klipte yaptırabilirsin. İki yarının aynı adam ve aynı
tempoda çıkması şansa kalır; bu yolda `cerceve.png` pencereleri tutmaz, YANLIŞ/DOĞRU yazılarını CapCut'ta elle ekle.

```text
Photorealistic live-action commercial footage, vertical 9:16. A split-screen comparison: the frame is divided into two equal halves, top and bottom, by a thin black horizontal line. Both halves show the same man in the same gym, filmed by the same locked-off side-profile camera at knee height, doing push-ups at the same time and at the same slow tempo.

THE ATHLETE: a fit Turkish man in his late 20s, athletic but natural build, short dark hair with a low fade, light stubble. Plain heather-grey fitted crew-neck t-shirt, black training shorts, black training shoes, no logos. Location: a modern premium gym with a matte black rubber floor, dark charcoal walls and one thin red LED strip along the wall.

TOP HALF — wrong form: as he lowers, his hips sag toward the floor, his lower back arches deeply like a banana, his head droops and his elbows flare out to the sides.
BOTTOM HALF — correct form: his body stays in one rigid straight line from head to heels, core braced, elbows tucked at about 45 degrees; he lowers his chest to a fist's height above the floor and presses back up to straight arms.

Static cameras in both halves, no zoom, no cuts. No subtitles, no captions, no labels, no icons, no on-screen text, no logos, no watermark. No speech, no music. Audio: breathing, quiet gym room tone.
```

## 5. Aynı kalıpla başka hareketler

`overlay.html` hareketi `ex` parametresiyle değiştirir (`render.cmd` içindeki adreslere `&ex=squat` ekle). Yalnızca
uygulamanın gerçekten yakaladığı hataları göster:

| Hareket | Videoda yanlış taraf | Uygulamanın uyarısı | `ex` |
|---|---|---|---|
| Squat | Çeyrek squat, derine inmeme | "Biraz daha derine in" (sayaç 0'da kalır, puan çıkmaz) | `squat` |
| Biceps Curl | Dirseği omuzdan savurma | "Dirseğini sabit tut, omuzdan savurma" | `curl` |
| Deadlift | Kalça yerine çömelerek kaldırma | "Çömelme, hareketi kalçadan yap" | `deadlift` |

Uygulamada **olmayan** kontroller: deadlift'te sırt yuvarlama (koddan kaldırılmış), squat'ta dizlerin içe çökmesi ve
sırt eğimi. Bunları "ArFitness yakalıyor" diye göstermek yanıltıcı olur.

## Başlık hakkında

"Sakatlanmayı Önle" bir çağrı olarak başlıkta duruyor. Seslendirmede ve açıklamada "ArFitness sakatlanmayı önler"
gibi kesin bir sağlık vaadi kurma; uygulamanın yaptığı şey form hatasını göstermek ve puanlamak. Kesin sağlık
iddiaları Instagram reklam onayında ve Google Play mağaza politikasında sorun çıkarabilir.

## Sorun çıkarsa

- **Kalça yeterince düşmüyor (yanlış taraf doğru gibi görünüyor):** K2'ye `extremely exaggerated sagging hips, the
  pelvis nearly touches the floor while the chest is still high` ekle.
- **Kamera oynuyor ya da yakınlaşıyor:** `tripod shot, absolutely static frame, surveillance-camera stillness` ekle.
- **İki pencerede adam farklı görünüyor:** K2 ve K3'ü aynı G2 karesinden üretmediysen olur; ikisine de G2'yi ilk kare ver.
- **Tempo tutmuyor:** kliplerden birinin hızını kurguda değiştir; iki tekrar yerine tek tekrarı kullan.
- **Yapay görünüyor:** prompta `documentary footage, shot on Sony FX3, available light` ekle.
- **Telefonda bozuk yazılı ekran çıktı (K4):** `the phone screen is never visible` cümlesini ekle ya da o kareyi kes.
- Instagram'da paylaşırken "Yapay zekâ ile üretildi" etiketini aç.
