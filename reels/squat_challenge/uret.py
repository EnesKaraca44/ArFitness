"""100 Squat Challenge reels'i: Seedance 2.0 (fal.ai) ile üretim + kurgu.

Aşamalar
  tahmin   maliyet tahmini yazar, para harcamaz (varsayılan)
  refs     Nano Banana Pro ile 3 referans görsel: Hamdi, Hülya, park
  klipler  Seedance 2.0 reference-to-video ile 7 sahne (9:16, 720p, dudak senkronlu Türkçe ses)
  kurgu    klipleri keser; altyazı, uygulama ekranı (HUD) ve son kartı ekler -> squat_challenge.mp4
  hepsi    refs + klipler + kurgu

Para harcayan aşamalar (refs, klipler, hepsi) --onay ister.
API anahtarı: FAL_KEY ortam değişkeni ya da %USERPROFILE%\\.fal_key dosyası (depo dışında; bu betik onu yazdırmaz).
Üretilenler uretim.json'a yazılır; betiği yeniden çalıştırmak hazır olanlar için tekrar ödeme yapmaz.
Bir sahneyi/görseli yeniden üretmek için:  --sadece s3 --yeniden   (ya da --sadece hamdi --yeniden)

Kullanım (Python 3.10; numpy, scipy, opencv-python, imageio-ffmpeg ve kurulu Chrome gerekir):
  py -3.10 -I uret.py tahmin
  py -3.10 -I uret.py hepsi --onay
  py -3.10 -I uret.py kurgu
"""
import argparse
import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

import cv2
import imageio_ffmpeg
import numpy as np
from scipy.io import wavfile

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, 'out')
FF = imageio_ffmpeg.get_ffmpeg_exe()
STATE = os.path.join(HERE, 'uretim.json')
CHROME = r'C:\Program Files\Google\Chrome\Application\chrome.exe'
W, H, FPS, SR = 1080, 1920, 30, 48000

IMG_MODEL = 'fal-ai/nano-banana-pro'
VID_MODEL = {'fast': 'bytedance/seedance-2.0/fast/reference-to-video',
             'standart': 'bytedance/seedance-2.0/reference-to-video'}
# fal.ai model sayfalarındaki fiyatlar (Kasım 2026): görsel başına $, 720p video saniyesi başına $
PRICE = {'img': 0.15, 'fast': 0.2419, 'standart': 0.3024}

# ----------------------------------------------------------------------------- içerik
REF_STYLE = ('Photorealistic full-body photograph, vertical 9:16, natural daylight, sharp focus, shot on a smartphone. '
             'Real insect anatomy blended with human posture; realistic chitin with pores, scratches and reflections. '
             'Not a cartoon, not a 3D render, not a toy. No text.')
HAMDI_REF = ('A human-sized male cockroach in his mid-20s who stands upright like a person, a scruffy neighbourhood '
             'slacker. Dark reddish-brown shell, scratched, stained and cracked in places. Huge bulging bloodshot eyes; a '
             'wide creepy grin with sparse crooked teeth and one missing front tooth. Two long antennae, one of them '
             'repaired with dirty white tape. Stained white ribbed sleeveless undershirt stretched over a round pot belly. '
             'He holds a paper cone of sunflower seeds. He stands facing the camera in a Turkish municipal neighbourhood '
             'park with yellow-and-blue outdoor fitness equipment and a green wooden bench.')
HULYA_REF = ('A human-sized female cockroach who stands upright like a person, a vain social-media influencer diva. '
             'Polished glossy caramel-brown shell studded with rhinestones. Human-like face with enormous over-filled '
             'glossy lips, very long eyelash extensions, laminated arched brows and heavy make-up. Long pink acrylic '
             'nails. Matching hot-pink sports set: high-waist leggings and a cropped sports top. She holds a selfie stick '
             'with a phone; a ring light on a tripod stands in front of her. She poses in a Turkish municipal '
             'neighbourhood park with yellow-and-blue outdoor fitness equipment.')
PARK_REF = ('Photorealistic photograph, vertical 9:16, late-afternoon sunlight, shot on a smartphone. An empty Turkish '
            'municipal neighbourhood park with outdoor fitness equipment: chunky steel exercise machines painted yellow '
            'and blue, slightly chipped; red rubber ground tiles; a green slatted wooden park bench; plane trees; beige '
            'apartment blocks with balconies and satellite dishes in the background. No people, no text.')
REFS = {'hamdi': HAMDI_REF + ' ' + REF_STYLE, 'hulya': HULYA_REF + ' ' + REF_STYLE, 'park': PARK_REF}

STYLE = ('Photorealistic live-action smartphone footage, vertical 9:16, natural late-afternoon sunlight, real-world '
         'textures and imperfections. The characters are human-sized anthropomorphic cockroaches who live like ordinary '
         'people: real insect chitin, human posture and body language, expressive human-like faces. Not a cartoon, not '
         '3D animation, not a puppet. No subtitles, no captions, no on-screen text, no music.')
TALK = ' Accurate lip-sync. Only the quoted line is spoken, once, in Turkish; no other speech.'
HAMDI = ('HAMDI: a human-sized male cockroach in his mid-20s, a scruffy neighbourhood slacker. Dark reddish-brown '
         'scratched, stained, cracked shell; huge bulging bloodshot eyes that never blink, the left one twitching; a '
         'permanent creepy grin with sparse crooked teeth and one missing front tooth; one antenna repaired with dirty '
         'white tape; stained white ribbed sleeveless undershirt over a round pot belly.')
HULYA = ('HÜLYA: a human-sized female cockroach, a vain influencer diva. Glossy caramel-brown rhinestone-studded '
         'shell, enormous over-filled glossy lips, very long eyelash extensions, laminated brows, heavy make-up, long '
         'pink acrylic nails, hot-pink high-waist leggings and cropped sports top, selfie stick with a phone in hand.')
PARK = ('the location: a Turkish municipal park with yellow-and-blue outdoor fitness machines, red rubber tiles, a '
        'green slatted wooden bench, plane trees and beige apartment blocks behind.')
HAMDI_VOICE = 'in a fast, nasal, mocking voice with a thick rural Eastern Anatolian accent'


def scene(refs, body, talk=True):
    """refs sırası image_urls sırasıdır: ilki @Image1, ikincisi @Image2 ..."""
    roles = {'hamdi': HAMDI, 'hulya': HULYA, 'park': PARK}
    intro = ' '.join(f'@Image{i + 1} is {roles[r]}' for i, r in enumerate(refs))
    return f'{STYLE}{TALK if talk else ""}\n{intro}\n{body}'


SAHNELER = {
    's1': dict(refs=['hulya', 'park'], sure=5, altyazi='Güzellerimm, 100 squat challenge!\nFormumuz kusursuz bebeğim 💅',
               prompt=scene(['hulya', 'park'],
                            'Shot: front-facing medium-wide shot at selfie height, as if we are her phone\'s story camera; '
                            'a ring light on a tripod is reflected in her eyes. Far behind her, out of focus, a male '
                            'cockroach in a white undershirt sits on the green bench.\n'
                            'Action: she pouts into the camera and does tiny lazy quarter squats, knees bending only a few '
                            'centimetres, proudly, as if they were perfect.\n'
                            'Dialogue: Hülya says, in a high-pitched, drawn-out, sugary influencer voice: '
                            '"Güzellerimm, yüz squat challenge! Formumuz kusursuz bebeğim!"\n'
                            'Audio: park ambience, sparrows, distant children.')),
    's2': dict(refs=['hamdi', 'park'], sure=6, altyazi='Nörüyoon abla? O squat değil,\ndolmuşa mı binmeye çalışıyorsun?',
               prompt=scene(['hamdi', 'park'],
                            'Shot: medium close-up, slightly low angle. Hamdi slouches on the green park bench with his '
                            'legs spread, a paper cone of sunflower seeds in his hand, seed shells scattered around his '
                            'feet. Blurred in the foreground, a female cockroach in a pink sports set does tiny squats.\n'
                            'Action: he stares at her without blinking, left eye twitching, cracks a sunflower seed '
                            'between his teeth, speaks through his creepy grin, then spits the shell onto the ground.\n'
                            f'Dialogue: Hamdi says, {HAMDI_VOICE}: '
                            '"Nörüyoon abla? O squat değil, dolmuşa mı binmeye çalışıyorsun?"\n'
                            'Audio: a seed cracking, a wet spit, park ambience.')),
    's3': dict(refs=['hulya', 'park'], sure=5, altyazi='İğrenç! Takipçin kaç senin, 12 mi?\nYürü git bebeğim.',
               prompt=scene(['hulya', 'park'],
                            'Shot: medium close-up of Hülya next to her ring light.\n'
                            'Action: she whips her head toward the bench off-screen, lowers the selfie stick, looks him up '
                            'and down with pure disgust, waves him away with her long pink nails and turns back to her '
                            'phone.\n'
                            'Dialogue: Hülya says, in a sharp, snobbish, high-pitched voice: '
                            '"İğrenç! Takipçin kaç senin, on iki mi? Yürü git bebeğim."\n'
                            'Audio: park ambience.')),
    's4': dict(refs=['hamdi', 'park'], sure=5, altyazi='Yüz dedin, sıfırsın abla.\nİzle gör, squat nası yapılırmış.',
               prompt=scene(['hamdi', 'park'],
                            'Shot: over-the-shoulder shot from behind a female cockroach in a pink sports set; her '
                            'shoulder and one curled antenna are blurred in the foreground.\n'
                            'Action: Hamdi gets up from the green bench, steps toward the camera with a psychotic grin and '
                            'thrusts a smartphone forward at arm\'s length, screen facing the camera. The phone screen is '
                            'dark and shows only one huge glowing white digit "0".\n'
                            f'Dialogue: Hamdi says, {HAMDI_VOICE}: '
                            '"Yüz dedin, sıfırsın abla. İzle gör, squat nası yapılırmış."\n'
                            'Audio: park ambience.')),
    's5': dict(refs=['hamdi', 'park'], sure=5, altyazi=None,
               prompt=scene(['hamdi', 'park'],
                            'Shot: static, locked-off wide side-profile shot from knee height, as if filmed by a phone '
                            'leaning against the bench; his whole body is in frame from antennae to feet.\n'
                            'Action: Hamdi drops a paper cone of sunflower seeds on the bench, cracks his neck left and '
                            'right, stretches his arms forward and performs ONE slow, textbook deep squat: hips back, '
                            'thighs well below parallel, back straight, heels flat. He stands back up tall and holds his '
                            'creepy grin, unblinking.\n'
                            'Dialogue: none, Hamdi does not speak.\n'
                            'Audio: two loud neck cracks, a short phone vibration buzz as he stands up, then a flat robotic '
                            'female text-to-speech phone voice says in Turkish: "Bir." Park ambience.', talk=False)),
    's6': dict(refs=['hulya', 'park'], sure=4, altyazi='Aaa enerciii…\nStory\'me gelsene aşkım 😘',
               prompt=scene(['hulya', 'park'],
                            'Shot: close-up of Hülya, shallow depth of field.\n'
                            'Action: her jaw drops, impressed; her attitude flips to flirty. She leans toward the bench '
                            'off-screen, bats her long lashes, raises the selfie stick invitingly and blows a kiss.\n'
                            'Dialogue: Hülya says, in a suddenly sweet, flirty, drawn-out voice: '
                            '"Aaa, enerciii… Story\'me gelsene aşkım."\n'
                            'Audio: park ambience.')),
    's7': dict(refs=['hamdi', 'hulya', 'park'], sure=6, altyazi='Kirvem, şuralara bi el at.',
               prompt=scene(['hamdi', 'hulya', 'park'],
                            'Shot: static wide two-shot. Hamdi sits on the green bench in the left foreground cracking '
                            'sunflower seeds; Hülya stands a few metres away on the right, posing at her ring light.\n'
                            'Action: Hamdi spits a shell, glances up at the sky without blinking and speaks casually, like '
                            'asking a friend for a favour. A rising whistle; a small comedic toy rocket streaks down from '
                            'the sky onto exactly where Hülya stands and bursts into a huge cartoonish cloud of grey smoke '
                            'and pink glitter that swallows her spot; the ring light topples over and the selfie stick '
                            'spins through the air. Hamdi does not flinch; he cracks another seed, grinning. Slapstick '
                            'comedy, nobody is hurt, no gore.\n'
                            f'Dialogue: Hamdi says, {HAMDI_VOICE}: "Kirvem, şuralara bi el at."\n'
                            'Audio: rising whistle, a deep comedic boom, glitter sprinkling, a seed crack.')),
}

# Kurgu: once/sonra = konuşmanın önüne/arkasına bırakılan pay (sn). kesim.json ile elle ezilebilir: {"s3": [0.4, 3.1]}
KURGU = {
    's1': dict(once=0.3, sonra=0.45),
    's2': dict(once=0.3, sonra=0.9),                          # kabuk tükürme konuşmadan sonra
    's3': dict(once=0.25, sonra=0.45),
    's4': dict(once=0.25, sonra=0.45, araya=dict(kaynak='s1', hud='hud_tekrar_0_uyari.png', oran=0.28, sure=0.8)),
    's5': dict(tam=True, bas=0.2, hud='hud_tekrar_1.png', hud_son=1.5),
    's6': dict(once=0.25, sonra=0.5),
    's7': dict(once=0.25, sonu=True),                         # konuşmadan sonra füze, klibin sonuna kadar
}
SON_KART = 1.6


# ----------------------------------------------------------------------------- yardımcılar
def load_state():
    if os.path.exists(STATE):
        with open(STATE, encoding='utf-8') as fh:
            return json.load(fh)
    return {'refs': {}, 'klipler': {}, 'harcama': 0.0}


def save_state(st):
    with open(STATE, 'w', encoding='utf-8') as fh:
        json.dump(st, fh, ensure_ascii=False, indent=2)


def fal_key():
    key = os.environ.get('FAL_KEY', '').strip()
    if not key:
        p = os.path.join(os.path.expanduser('~'), '.fal_key')
        if os.path.exists(p):
            with open(p, encoding='utf-8-sig') as fh:              # Not Defteri BOM eklerse de okunsun
                key = fh.read().strip()
    if not key:
        sys.exit('fal.ai anahtarı yok: FAL_KEY ortam değişkenini ayarla ya da anahtarı %USERPROFILE%\\.fal_key dosyasına koy.')
    return key


def http(method, url, key, body=None, timeout=120):
    data = json.dumps(body).encode('utf-8') if body is not None else None
    req = urllib.request.Request(url, data=data, method=method,
                                 headers={'Authorization': 'Key ' + key, 'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return json.loads(r.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        raise RuntimeError(f'HTTP {e.code}: {e.read().decode("utf-8", "replace")[:1200]}') from None


def fal_run(model, args, key, label, limit_s=1500):
    sub = http('POST', 'https://queue.fal.run/' + model, key, args)
    status_url, resp_url = sub['status_url'], sub['response_url']
    t0 = time.time()
    last = None
    while True:
        st = http('GET', status_url, key)
        s = st.get('status')
        if s == 'COMPLETED':
            break
        if s not in ('IN_QUEUE', 'IN_PROGRESS'):
            raise RuntimeError(f'beklenmeyen durum: {st}')
        if time.time() - t0 > limit_s:
            raise RuntimeError(f'{limit_s} sn içinde bitmedi; istek kimliği {sub.get("request_id")}')
        if s != last or int(time.time() - t0) % 30 < 6:
            print(f'  {label}: {s} ({int(time.time() - t0)} sn)', flush=True)
        last = s
        time.sleep(6)
    return sub.get('request_id'), http('GET', resp_url, key)


def download(url, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    tmp = path + '.part'
    with urllib.request.urlopen(url, timeout=300) as r, open(tmp, 'wb') as fh:
        while True:
            chunk = r.read(1 << 20)
            if not chunk:
                break
            fh.write(chunk)
    os.replace(tmp, path)


def pick(names, only):
    if not only:
        return list(names)
    sel = [n for n in names if n in only]
    return sel


def estimate(a, st):
    refs = [n for n in pick(REFS, a.sadece) if a.yeniden or n not in st['refs']]
    clips = [n for n in pick(SAHNELER, a.sadece) if a.yeniden or n not in st['klipler']]
    sec = sum(SAHNELER[n]['sure'] for n in clips)
    cost = {'refs': len(refs) * PRICE['img'], 'klipler': sec * PRICE[a.kalite]}
    return refs, clips, sec, cost


# ----------------------------------------------------------------------------- üretim
def stage_refs(a, st, key):
    refs, _, _, cost = estimate(a, st)
    for n in refs:
        print(f'referans: {n} üretiliyor...', flush=True)
        _, res = fal_run(IMG_MODEL, {'prompt': REFS[n], 'aspect_ratio': '9:16', 'resolution': '2K',
                                     'num_images': 1, 'output_format': 'png'}, key, n, limit_s=600)
        url = res['images'][0]['url']
        local = os.path.join(HERE, 'referans', f'{n}.png')
        download(url, local)
        st['refs'][n] = {'url': url, 'dosya': os.path.relpath(local, HERE)}
        st['harcama'] = round(st['harcama'] + PRICE['img'], 4)
        save_state(st)
        print(f'  -> {local}')
    if not refs:
        print('referans: hepsi hazır')


def stage_clips(a, st, key):
    _, clips, _, _ = estimate(a, st)
    failed = []
    for n in clips:
        sc = SAHNELER[n]
        missing = [r for r in sc['refs'] if r not in st['refs']]
        if missing:
            sys.exit(f'{n}: önce referans görseller lazım: {missing} (refs aşaması)')
        args = {'prompt': sc['prompt'], 'image_urls': [st['refs'][r]['url'] for r in sc['refs']],
                'aspect_ratio': '9:16', 'resolution': '720p', 'duration': str(sc['sure']),
                'generate_audio': True}
        print(f'klip: {n} ({sc["sure"]} sn, {a.kalite}) üretiliyor...', flush=True)
        try:
            rid, res = fal_run(VID_MODEL[a.kalite], args, key, n)
        except RuntimeError as e:
            print(f'  !! {n} başarısız: {e}')
            failed.append(n)
            continue
        local = os.path.join(HERE, 'klipler', f'{n}.mp4')
        download(res['video']['url'], local)
        st['klipler'][n] = {'istek': rid, 'url': res['video']['url'], 'seed': res.get('seed'),
                            'dosya': os.path.relpath(local, HERE), 'kalite': a.kalite}
        st['harcama'] = round(st['harcama'] + sc['sure'] * PRICE[a.kalite], 4)
        save_state(st)
        print(f'  -> {local}')
    if failed:
        print('BAŞARISIZ sahneler:', ' '.join(failed), '— tekrar: --sadece', ' '.join(failed))


# ----------------------------------------------------------------------------- kurgu
def has_audio(path):
    r = subprocess.run([FF, '-hide_banner', '-i', path], capture_output=True, text=True, errors='replace')
    return 'Audio:' in r.stderr


def duration(path):
    cap = cv2.VideoCapture(path)
    n, fps = cap.get(cv2.CAP_PROP_FRAME_COUNT), cap.get(cv2.CAP_PROP_FPS) or 24
    cap.release()
    return n / fps


def speech_span(path, work):
    """Konuşmanın başı/sonu: kısa pencerelerde ses düzeyi, en yüksek düzeyin 14 dB altını geçen ilk/son an."""
    if not has_audio(path):
        return None
    wav = os.path.join(work, os.path.basename(path) + '.wav')
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', '-i', path, '-vn', '-ac', '1', '-ar', '16000',
                    '-c:a', 'pcm_s16le', wav], check=True)
    sr, x = wavfile.read(wav)
    x = x.astype(np.float32) / 32768.0
    win = int(0.05 * sr)
    n = len(x) // win
    if n < 3:
        return None
    db = 20 * np.log10(np.sqrt(np.mean(x[:n * win].reshape(n, win) ** 2, axis=1)) + 1e-9)
    db = np.convolve(np.pad(db, 1, mode='edge'), np.ones(3) / 3, mode='valid')   # kenarı sıfırla değil kendisiyle doldur
    thr = max(db.max() - 14, np.percentile(db, 30) + 6)
    loud = db > thr
    # en az 3 pencere (150 ms) süren bölümler: tek bir tıkırtı ya da kuş sesi konuşma sayılmasın
    runs, i = [], 0
    while i < n:
        if loud[i]:
            j = i
            while j < n and loud[j]:
                j += 1
            if j - i >= 3:
                runs.append((i, j))
            i = j
        else:
            i += 1
    return (runs[0][0] * 0.05, runs[-1][1] * 0.05) if runs else None


def render_png(query, path):
    url = 'file:///' + os.path.join(HERE, 'overlay.html').replace('\\', '/') + '?' + query
    subprocess.run([CHROME, '--headless=new', '--hide-scrollbars', '--force-device-scale-factor=2',
                    '--window-size=540,960', '--default-background-color=00000000', '--virtual-time-budget=8000',
                    f'--screenshot={path}', url], capture_output=True, check=True)
    if not os.path.exists(path):
        raise RuntimeError(f'Chrome görseli basamadı: {path}')


def caption_png(n, work):
    text = SAHNELER[n]['altyazi']
    if not text:
        return None
    path = os.path.join(work, f'altyazi_{n}.png')
    render_png('v=cap&t=' + urllib.parse.quote(text), path)
    return path


def window(n, src, work, overrides):
    d = duration(src)
    k = KURGU[n]
    sp = speech_span(src, work)
    if n in overrides:
        t0, t1 = overrides[n]
    elif k.get('tam') or sp is None:
        t0, t1 = k.get('bas', 0.0), d
    else:
        t0 = max(0.0, sp[0] - k['once'])
        t1 = d if k.get('sonu') else min(d, sp[1] + k['sonra'])
    t1 = min(t1, d)
    if t1 - t0 < 1.0:                                        # algılama saçmaladıysa klibin tamamı
        t0, t1 = 0.0, d
    return t0, t1, sp


def segment(n, clipdir, work, overrides, hud_override):
    src = os.path.join(clipdir, f'{n}.mp4')
    k = KURGU[n]
    t0, t1, sp = window(n, src, work, overrides)
    dur = t1 - t0
    ins = ['-ss', f'{t0:.3f}', '-t', f'{dur:.3f}', '-i', src]
    fc = [f'[0:v]scale={W}:{H}:force_original_aspect_ratio=increase:flags=lanczos,crop={W}:{H},setsar=1,fps={FPS}[b0]']
    cur, idx = 'b0', 1

    def add_png(path):
        nonlocal idx
        ins.extend(['-loop', '1', '-framerate', str(FPS), '-t', f'{dur:.3f}', '-i', path])
        idx += 1
        return idx - 1

    info = f'{n}: {t0:.2f}-{t1:.2f} sn'
    ar = k.get('araya')
    if ar:                                                     # uygulamanın gözünden kısa ara plan
        csrc = os.path.join(clipdir, f'{ar["kaynak"]}.mp4')
        cd = duration(csrc)
        cm = max(0.0, cd * 0.45)
        speech = (sp[0] - t0, sp[1] - t0) if sp and n not in overrides else (0.3, dur - 0.3)
        at = max(0.2, min(dur - ar['sure'] - 0.1, speech[0] + ar['oran'] * (speech[1] - speech[0])))
        ins.extend(['-ss', f'{cm:.3f}', '-t', f'{ar["sure"]:.3f}', '-i', csrc])
        ci = idx
        idx += 1
        hi = add_png(os.path.join(OUT, ar['hud']))
        fc.append(f'[{ci}:v]scale={W}:{H}:force_original_aspect_ratio=increase:flags=lanczos,crop={W}:{H},setsar=1,'
                  f'fps={FPS}[c0];[c0][{hi}:v]overlay=0:0:shortest=1[c1];[c1]setpts=PTS-STARTPTS+{at:.3f}/TB[c2];'
                  f'[{cur}][c2]overlay=0:0:eof_action=pass:enable=\'between(t,{at:.3f},{at + ar["sure"]:.3f})\'[b1]')
        cur = 'b1'
        info += f', ara plan {at:.2f}+{ar["sure"]}'
    cap = caption_png(n, work)
    if cap:
        if sp and n not in overrides:
            cs, ce = max(0.0, sp[0] - t0 - 0.1), min(dur, sp[1] - t0 + 0.25)
        else:
            cs, ce = 0.0, dur
        pi = add_png(cap)
        fc.append(f'[{cur}][{pi}:v]overlay=0:0:enable=\'between(t,{cs:.3f},{ce:.3f})\'[b2]')
        cur = 'b2'
    if k.get('hud'):
        hs = max(0.0, dur - k['hud_son'])
        pi = add_png(os.path.join(OUT, hud_override or k['hud']))
        fc.append(f'[{cur}][{pi}:v]overlay=0:0:enable=\'gte(t,{hs:.3f})\'[b3]')
        cur = 'b3'
        info += f', HUD {hs:.2f} sn\'den'
    fc.append(f'[{cur}]format=yuv420p[v]')
    if has_audio(src):
        fc.append(f'[0:a]aresample={SR},aformat=channel_layouts=stereo,afade=t=in:d=0.03,'
                  f'afade=t=out:st={max(0.0, dur - 0.06):.3f}:d=0.06[a]')
    else:
        ins.extend(['-f', 'lavfi', '-t', f'{dur:.3f}', '-i', f'anullsrc=r={SR}:cl=stereo'])
        fc.append(f'[{idx}:a]anull[a]')
        idx += 1
        info += ', SES YOK'
    out = os.path.join(work, f'seg_{n}.mp4')
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', *ins, '-filter_complex', ';'.join(fc),
                    '-map', '[v]', '-map', '[a]', '-t', f'{dur:.3f}', '-c:v', 'libx264', '-preset', 'medium',
                    '-crf', '18', '-pix_fmt', 'yuv420p', '-r', str(FPS), '-c:a', 'aac', '-b:a', '192k', '-ar', str(SR),
                    out], check=True)
    print(info)
    return out, dur


def stage_edit(a):
    clipdir = a.klipler or os.path.join(HERE, 'klipler')
    work = os.path.join(a.calisma or os.path.join(HERE, '_kurgu'))
    os.makedirs(work, exist_ok=True)
    missing = [n for n in SAHNELER if not os.path.exists(os.path.join(clipdir, f'{n}.mp4'))]
    if missing:
        sys.exit(f'kurgu: eksik klipler {missing} ({clipdir})')
    ov_path = os.path.join(HERE, 'kesim.json')
    overrides = {}
    if os.path.exists(ov_path):
        with open(ov_path, encoding='utf-8') as fh:
            overrides = {k: v for k, v in json.load(fh).items() if k in SAHNELER}
    hud = 'hud_tekrar_1_mukemmel.png' if a.mukemmel else None
    segs, total = [], 0.0
    for n in SAHNELER:
        p, d = segment(n, clipdir, work, overrides, hud)
        segs.append(p)
        total += d
    end = os.path.join(work, 'seg_son.mp4')
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', '-loop', '1', '-framerate', str(FPS),
                    '-t', str(SON_KART), '-i', os.path.join(OUT, 'son_kart.png'), '-f', 'lavfi', '-t', str(SON_KART),
                    '-i', f'anullsrc=r={SR}:cl=stereo', '-filter_complex',
                    f'[0:v]scale={W}:{H},setsar=1,fps={FPS},fade=t=in:d=0.2,format=yuv420p[v]', '-map', '[v]',
                    '-map', '1:a', '-t', str(SON_KART), '-c:v', 'libx264', '-crf', '18', '-pix_fmt', 'yuv420p',
                    '-c:a', 'aac', '-b:a', '192k', '-ar', str(SR), end], check=True)
    segs.append(end)
    total += SON_KART
    ins = []
    for p in segs:
        ins.extend(['-i', p])
    pairs = ''.join(f'[{i}:v][{i}:a]' for i in range(len(segs)))
    fc = (f'{pairs}concat=n={len(segs)}:v=1:a=1[v][a0];'
          f'[a0]loudnorm=I=-14:TP=-1.5:LRA=11,aresample={SR}[a]')
    out = a.cikti or os.path.join(HERE, 'squat_challenge.mp4')
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', *ins, '-filter_complex', fc, '-map', '[v]',
                    '-map', '[a]', '-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-profile:v', 'high',
                    '-pix_fmt', 'yuv420p', '-r', str(FPS), '-c:a', 'aac', '-b:a', '192k', '-ar', str(SR),
                    '-movflags', '+faststart', out], check=True)
    print(f'kurgu: {out} ({total:.1f} sn)')


# ----------------------------------------------------------------------------- giriş
def main():
    try:
        sys.stdout.reconfigure(encoding='utf-8')               # Windows konsolunda Türkçe karakterler bozulmasın
    except (AttributeError, ValueError):
        pass
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('asama', nargs='?', default='tahmin', choices=['tahmin', 'refs', 'klipler', 'kurgu', 'hepsi'])
    ap.add_argument('--onay', action='store_true', help='para harcayan aşamaları çalıştır')
    ap.add_argument('--sadece', nargs='*', default=[], help='yalnızca bu sahneler/referanslar (s1 s3 hamdi ...)')
    ap.add_argument('--yeniden', action='store_true', help='--sadece ile seçilenleri hazır olsalar da yeniden üret')
    ap.add_argument('--kalite', default='fast', choices=list(VID_MODEL))
    ap.add_argument('--mukemmel', action='store_true', help='HUD\'da "MÜKEMMEL FORM! 1. tekrar" metnini kullan')
    ap.add_argument('--klipler', help='kurgu: klip klasörü (varsayılan klipler/)')
    ap.add_argument('--calisma', help='kurgu: ara dosya klasörü (varsayılan _kurgu/)')
    ap.add_argument('--cikti', help='kurgu: çıktı dosyası (varsayılan squat_challenge.mp4)')
    a = ap.parse_args()
    if a.yeniden and not a.sadece:
        sys.exit('--yeniden yalnızca --sadece ile kullanılır (yanlışlıkla her şeyi yeniden ödememek için)')

    st = load_state()
    refs, clips, sec, cost = estimate(a, st)
    paid = a.asama in ('refs', 'klipler', 'hepsi')
    if a.asama == 'tahmin' or (paid and not a.onay):
        print(f'üretilecek referans: {refs or "-"}  ->  ~${cost["refs"]:.2f}')
        print(f'üretilecek klip    : {clips or "-"} ({sec} sn, {a.kalite})  ->  ~${cost["klipler"]:.2f}')
        print(f'toplam (deneme başına): ~${cost["refs"] + cost["klipler"]:.2f}   şimdiye kadar: ~${st["harcama"]:.2f}')
        if paid:
            print('Para harcamak için aynı komutu --onay ile çalıştır.')
        return
    if paid:
        key = fal_key()
        if a.asama in ('refs', 'hepsi'):
            stage_refs(a, st, key)
        if a.asama in ('klipler', 'hepsi'):
            stage_clips(a, st, key)
        print(f'toplam tahmini harcama: ~${st["harcama"]:.2f}')
    if a.asama in ('kurgu', 'hepsi'):
        stage_edit(a)


if __name__ == '__main__':
    main()
