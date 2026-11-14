# 100 Squat Challenge — gerçekçi hamamböceği reels'i

Bu dosya, videoyu Veo 3.1 ile (Google Flow) üretmek için kopyala-yapıştır promptları içerir.
Promptlar İngilizce (modeller İngilizce tarifi daha iyi izliyor), replikler Türkçe ve senaryodaki gibi.

Promptlar henüz bir araçta denenmedi; ilk üretimlerde küçük düzeltme gerekebilir (bkz. "Sorun çıkarsa").

**Otomatik yol:** `uret.py` aynı sahneleri Seedance 2.0 (fal.ai API) ile üretir ve kurguyu yapar:
`py -3.10 -I uret.py tahmin` maliyeti gösterir, `py -3.10 -I uret.py hepsi --onay` üretir. Anahtar
`%USERPROFILE%\.fal_key` dosyasında durur. Ayrıntılar betiğin başında.

## Çalışma sırası

1. **Referans görselleri üret** (Bölüm 1): Hamdi, Hülya ve boş park. Beğendiğin birer kareyi kaydet.
2. **Her sahneyi ayrı klip olarak üret** (Bölüm 2). Her üretimde ilgili karakterin ve parkın referans görselini ekle
   (Flow'da "Ingredients", diğer araçlarda "reference image / first frame"). Tek klipte tek karakter konuşsun.
3. Klipleri `reels/squat_challenge/klipler/` klasörüne `s1.mp4 … s7.mp4` adıyla koy.
4. **Kurgu** (Bölüm 3): kes, uygulama katmanlarını bindir, son kartı ekle.

Karakterlerin her klipte aynı görünmesi için prompttaki karakter ve mekân paragraflarını **değiştirmeden** bırak.
Attığın iki örnek görseli araca referans diye yükleme: görüntüyü çizgi film/3D'ye çeker ve başkasının karakteridir.
Kendi referansını Bölüm 1'deki promptlarla üret.

## Veo 3.1 ayarları (Google Flow)

Flow'un yardım sayfasından, Kasım 2026'da kontrol edildi; arayüzdeki adlar değişmiş olabilir.

| Ayar | Seçilecek |
|---|---|
| Mod | Ingredients to Video (referans görselle üretim) |
| Model | Veo 3.1 – Fast. Bu mod Quality modelinde yok. |
| En-boy oranı | 9:16 (dikey) |
| Süre | 8 sn (bu modda tek seçenek); fazlasını kurguda kes |
| İstek başına çıktı | 1. Flow tek istekte 2 video üretebiliyor ve kredi ikisi için de düşüyor. |

- **Referans görseller:** bir üretime en çok 3 görsel eklenir. S1–S6'da sahnedeki karakter + park, S7'de Hamdi + Hülya + park.
  Görselleri Flow'un ya da Gemini uygulamasının görsel modeliyle (Nano Banana Pro) üret.
- **Kredi:** Fast üretim 20 kredi; Google AI Pro ayda 1000 kredi veriyor. 7 sahne × yaklaşık 3 deneme ≈ 420 kredi.
- **Klip sessiz çıkarsa:** aynı promptu Frames to Video modunda dene; referans kareyi ilk kare olarak ver.

## 1. Referans görseller (görsel üretim aracı)

**Hamdi**

```text
Photorealistic full-body photograph, vertical 9:16, natural daylight, sharp focus, shot on a smartphone.
A human-sized male cockroach in his mid-20s who stands upright like a person, a scruffy neighbourhood slacker. Dark reddish-brown shell, scratched, stained and cracked in places. Huge bulging bloodshot eyes; a wide creepy grin with sparse crooked teeth and one missing front tooth. Two long antennae, one of them repaired with dirty white tape. Stained white ribbed sleeveless undershirt stretched over a round pot belly. He holds a paper cone of sunflower seeds.
He stands facing the camera in a Turkish municipal neighbourhood park with yellow-and-blue outdoor fitness equipment and a green wooden bench.
Real insect anatomy blended with human posture; realistic chitin with pores, scratches and reflections. Not a cartoon, not a 3D render, not a toy. No text.
```

**Hülya**

```text
Photorealistic full-body photograph, vertical 9:16, natural daylight, sharp focus, shot on a smartphone.
A human-sized female cockroach who stands upright like a person, a vain social-media influencer diva. Polished glossy caramel-brown shell studded with rhinestones. Human-like face with enormous over-filled glossy lips, very long eyelash extensions, laminated arched brows and heavy make-up. Long pink acrylic nails. Matching hot-pink sports set: high-waist leggings and a cropped sports top. She holds a selfie stick with a phone; a ring light on a tripod stands in front of her.
She poses in a Turkish municipal neighbourhood park with yellow-and-blue outdoor fitness equipment.
Real insect anatomy blended with human posture; realistic chitin with reflections. Not a cartoon, not a 3D render, not a toy. No text.
```

**Park (boş)**

```text
Photorealistic photograph, vertical 9:16, late-afternoon sunlight, shot on a smartphone. An empty Turkish municipal neighbourhood park with outdoor fitness equipment: chunky steel exercise machines painted yellow and blue, slightly chipped; red rubber ground tiles; a green slatted wooden park bench; plane trees; beige apartment blocks with balconies and satellite dishes in the background. No people, no text.
```

## 2. Sahne promptları

### S1 — Hülya, kanca

```text
Photorealistic live-action video, vertical 9:16, filmed handheld on a smartphone, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces with accurate lip-sync. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HÜLYA: a human-sized female cockroach, a vain social-media influencer diva. Polished glossy caramel-brown shell studded with rhinestones. Human-like face with enormous over-filled glossy lips, very long eyelash extensions, laminated arched brows and heavy make-up. Long pink acrylic nails. Matching hot-pink sports set: high-waist leggings and a cropped sports top. She holds a selfie stick with a phone; a ring light on a tripod stands in front of her.

Shot: front-facing medium-wide shot at selfie height, as if the viewer is her story camera; the ring light is reflected in her eyes. Far behind her, out of focus, a male cockroach in a white undershirt sits on the green bench.
Action: she pouts at the camera and does tiny, lazy quarter squats — her knees bend only a few centimetres — proudly, as if they were perfect.
Dialogue: Hülya says in Turkish, in a high-pitched, drawn-out, sugary influencer voice: "Güzellerimm, yüz squat challenge! Formumuz kusursuz bebeğim!"
Audio: park ambience, sparrows, distant children. No music.
```

### S2 — Hamdi laf atıyor

```text
Photorealistic live-action video, vertical 9:16, filmed handheld on a smartphone, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces with accurate lip-sync. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HAMDI: a human-sized male cockroach in his mid-20s, a scruffy neighbourhood slacker. Dark reddish-brown shell, scratched, stained and cracked in places. Huge bulging bloodshot eyes that never blink; his left eye keeps twitching. A permanent wide creepy grin with sparse crooked teeth and one missing front tooth. Two long antennae, one of them repaired with dirty white tape. Stained white ribbed sleeveless undershirt stretched over a round pot belly. He holds a paper cone of sunflower seeds.

Shot: medium close-up, slightly low angle. Hamdi slouches on the green park bench with his legs spread, sunflower seed shells scattered around his feet. Blurred at the edge of the frame in the foreground, a female cockroach in a pink sports set does tiny squats.
Action: he stares at her without blinking, left eye twitching, cracks a sunflower seed between his teeth, speaks through his creepy grin, then spits the shell onto the ground.
Dialogue: Hamdi says in Turkish, in a fast, nasal, mocking voice with a thick rural Anatolian accent: "Nörüyoon abla? O squat değil, dolmuşa mı binmeye çalışıyorsun?"
Audio: a seed cracking, a wet "ptu" spit, park ambience. No music.
```

### S3 — Hülya tersliyor

```text
Photorealistic live-action video, vertical 9:16, filmed handheld on a smartphone, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces with accurate lip-sync. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HÜLYA: a human-sized female cockroach, a vain social-media influencer diva. Polished glossy caramel-brown shell studded with rhinestones. Human-like face with enormous over-filled glossy lips, very long eyelash extensions, laminated arched brows and heavy make-up. Long pink acrylic nails. Matching hot-pink sports set: high-waist leggings and a cropped sports top. She holds a selfie stick with a phone; a ring light on a tripod stands in front of her.

Shot: medium close-up of Hülya.
Action: she whips her head toward the bench off-screen, lowers the selfie stick, looks him up and down with pure disgust, then waves him away with her long pink nails and turns back to her phone.
Dialogue: Hülya says in Turkish, in a sharp, snobbish, high-pitched voice: "İğrenç! Takipçin kaç senin, on iki mi? Yürü git bebeğim."
Audio: park ambience. No music.
```

### S4 — Telefon: sıfır

```text
Photorealistic live-action video, vertical 9:16, filmed handheld on a smartphone, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces with accurate lip-sync. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HAMDI: a human-sized male cockroach in his mid-20s, a scruffy neighbourhood slacker. Dark reddish-brown shell, scratched, stained and cracked in places. Huge bulging bloodshot eyes that never blink; his left eye keeps twitching. A permanent wide creepy grin with sparse crooked teeth and one missing front tooth. Two long antennae, one of them repaired with dirty white tape. Stained white ribbed sleeveless undershirt stretched over a round pot belly. He holds a paper cone of sunflower seeds.

Shot: over-the-shoulder shot from behind a female cockroach in a pink sports set; her shoulder and one curled antenna are blurred in the foreground.
Action: Hamdi gets up from the green bench, steps toward the camera with a psychotic grin and thrusts a smartphone forward at arm's length, screen facing the camera. The phone screen is dark and shows only one huge glowing white digit "0".
Dialogue: Hamdi says in Turkish, in a fast, nasal, mocking voice with a thick rural Anatolian accent: "Yüz dedin, sıfırsın abla. İzle gör, squat nası yapılırmış."
Audio: park ambience. No music.
```

### S5 — Hamdi'nin derin squat'ı

```text
Photorealistic live-action video, vertical 9:16, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HAMDI: a human-sized male cockroach in his mid-20s, a scruffy neighbourhood slacker. Dark reddish-brown shell, scratched, stained and cracked in places. Huge bulging bloodshot eyes that never blink; his left eye keeps twitching. A permanent wide creepy grin with sparse crooked teeth and one missing front tooth. Two long antennae, one of them repaired with dirty white tape. Stained white ribbed sleeveless undershirt stretched over a round pot belly.

Shot: static, locked-off wide side-profile shot from knee height, as if filmed by a phone leaning against the bench; his whole body is in frame from antennae to feet.
Action: Hamdi drops a paper cone of sunflower seeds on the bench, cracks his neck left and right, stretches his arms forward and performs ONE slow, textbook deep squat: hips back, thighs well below parallel, back straight, heels flat on the ground. He stands back up tall and holds his creepy grin, unblinking.
Dialogue: none — Hamdi does not speak.
Audio: two loud neck cracks, a short phone vibration buzz as he stands up, then a flat robotic female text-to-speech phone voice says in Turkish: "Bir." Park ambience. No music.
```

### S6 — Hülya yumuşuyor

```text
Photorealistic live-action video, vertical 9:16, filmed handheld on a smartphone, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces with accurate lip-sync. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HÜLYA: a human-sized female cockroach, a vain social-media influencer diva. Polished glossy caramel-brown shell studded with rhinestones. Human-like face with enormous over-filled glossy lips, very long eyelash extensions, laminated arched brows and heavy make-up. Long pink acrylic nails. Matching hot-pink sports set: high-waist leggings and a cropped sports top. She holds a selfie stick with a phone.

Shot: close-up of Hülya, shallow depth of field.
Action: her jaw drops, impressed; her attitude flips to flirty. She leans toward the bench off-screen, bats her long lashes, raises the selfie stick invitingly and blows a kiss.
Dialogue: Hülya says in Turkish, in a suddenly sweet, flirty, drawn-out voice: "Aaa, enerciii… Story'me gelsene aşkım."
Audio: park ambience. No music.
```

### S7 — Füze

```text
Photorealistic live-action video, vertical 9:16, natural late-afternoon sunlight, real-world textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary people: real insect chitin, human posture and body language, expressive human-like faces with accurate lip-sync. Not a cartoon, not 3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music. Only the quoted line is spoken, once; no other speech.

Location: a Turkish municipal neighbourhood park with outdoor fitness equipment — chunky steel exercise machines painted yellow and blue, red rubber ground tiles, a green slatted wooden park bench, plane trees, beige apartment blocks with balconies and satellite dishes in the background.

HAMDI: a human-sized male cockroach in his mid-20s, a scruffy neighbourhood slacker. Dark reddish-brown scratched shell, huge bulging bloodshot unblinking eyes, a permanent wide creepy grin with one missing front tooth, one antenna repaired with dirty white tape, stained white ribbed sleeveless undershirt over a round pot belly, a paper cone of sunflower seeds in his hand.
HÜLYA: a human-sized female cockroach influencer diva. Glossy caramel-brown shell with rhinestones, enormous over-filled lips, long eyelash extensions, pink acrylic nails, hot-pink leggings and cropped sports top, selfie stick in hand, ring light on a tripod in front of her.

Shot: static wide two-shot. Hamdi sits on the green bench in the left foreground cracking seeds; Hülya stands a few metres away on the right, posing at her ring light.
Action: Hamdi spits a shell, glances up at the sky without blinking and speaks casually, like asking a friend for a favour. A rising whistle; a small rocket streaks down from the sky and lands exactly where Hülya is standing — a huge comedic burst of grey smoke and pink glitter swallows her spot, the ring light topples over and the selfie stick spins through the air. Hamdi does not flinch; he cracks another seed, grinning. Slapstick cartoon-logic comedy: nobody is hurt, no gore.
Dialogue: Hamdi says in Turkish, in a fast, nasal, mocking voice with a thick rural Anatolian accent: "Kirvem, şuralara bi el at."
Audio: rising whistle, a deep comedic boom, glitter sprinkling, a seed crack. No music.
```

## 3. Kurgu (CapCut vb.)

Hazır katmanlar `out/` klasöründe, hepsi 1080×1920. HUD'lar şeffaf PNG; klibin üstüne tam ekran koyman yeterli,
takip (tracking) gerekmez. Yeniden basmak için `render.cmd`.

| Sıra | Klip | Kullanılacak kısım | Süre | Üstüne |
|---|---|---|---|---|
| 1 | s1 | replik | ~2.8 sn | — |
| 2 | s2 | replik + kabuk tükürme | ~3.5 sn | — |
| 3 | s3 | replik | ~3.0 sn | — |
| 4 | s4 | replik | ~3.0 sn | "sıfırsın" derken 0.7 sn araya gir: s1'den bir çeyrek squat karesi + `hud_tekrar_0_uyari.png` (uygulamanın gözünden Hülya). Hamdi'nin sesi altta devam etsin. |
| 5 | s5 | boyun çıtlatma + squat | ~3.0 sn | Ayağa kalktığı anda `hud_tekrar_1.png` |
| 6 | s6 | replik | ~2.0 sn | — |
| 7 | s7 | replik + füze | ~3.0 sn | — |
| 8 | `son_kart.png` | — | 1.5 sn | — |

Toplam yaklaşık 21–22 sn. Otomatik altyazı ekle (çoğu kişi sessiz izliyor) ve müziği kurguda ver.

**Uygulama mesajı:** uygulamanın şu anki sürümü tam tekrarda "1. tekrar! Form %98" yazar ve sesli "bir" der;
çeyrek squat'ta kırmızı "Biraz daha derine in" uyarısı verir, sayaç 0'da kalır. `hud_tekrar_1.png` bu gerçek mesajı
taşır. Senaryodaki "MÜKEMMEL FORM! 1. tekrar" metnini istersen `hud_tekrar_1_mukemmel.png` de hazır.

## 4. Tam 15 saniye istersen

Senaryodaki replikler doğal konuşma hızında 15 sn'ye sığmıyor. Kısaltılmış replikler (promptta yalnızca
tırnak içini değiştir):

| Sahne | Kısa replik | Süre |
|---|---|---|
| S1 | "Güzellerimm, yüz squat challenge!" | 2.0 |
| S2 | "Nörüyoon abla, dolmuşa mı biniyon?" | 2.0 |
| S3 | "İğrenç! Yürü git bebeğim." | 1.6 |
| S4 | "Yüz dedin, sıfırsın abla. İzle." | 2.2 |
| S5 | (squat + "Bir.") | 2.3 |
| S6 | "Aaa enerciii… Story'me gelsene aşkım." | 2.0 |
| S7 | "Kirvem, bi el at." + füze | 1.9 |
| Son kart | — | 1.2 |

## Sorun çıkarsa

- **Çizgi film gibi çıkıyor:** referans görseli daha gerçekçi olanla değiştir; prompta `documentary footage, shot on iPhone` ekle.
- **Karakter klipten klibe değişiyor:** aynı referans görseli her üretimde ekle, karakter paragrafına dokunma.
- **Türkçe ya da şive bozuk:** repliği okunduğu gibi yaz ("squat" yerine "skuat"). Olmuyorsa klibi sessiz üret,
  sesi ayrı bir seslendirme aracında yapıp dudak senkronu (lip-sync) uygula.
- **Telefon ekranındaki yazı bozuk:** normal; ekrandaki her yazıyı `out/` içindeki katmanlarla ver.
- **Araç füzeyi reddediyor:** `a small rocket streaks down…` cümlesini `a huge comedic puff of grey smoke and pink
  glitter suddenly bursts on her spot` ile değiştir, ıslık ve patlama sesini kurguda ekle.
- **Konuşmayı desteklemeyen araç (ör. Kling'in bazı sürümleri):** `Dialogue:` satırını `She/He talks to the camera`
  yap, sesi sonradan ekle.
- Instagram'da paylaşırken "Yapay zekâ ile üretildi" etiketini aç.
