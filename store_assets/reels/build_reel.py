"""ArFitness Reels derleyici.

Orijinal klibin tamamını (yaklaşık 10.6 sn) olduğu gibi kullanır, ardından reel.html'deki
~9.85 sn'lik devam animasyonunu ekler ve 1080x1920 / 30 fps bir MP4 üretir.

Gerekenler (Python 3.10 ortamında mevcut): numpy, scipy, opencv-python, playwright, imageio-ffmpeg
ve kurulu Google Chrome.

Kullanım:
  python -I build_reel.py --src "<video.mp4>" --work "<geçici klasör>" --out "<çıktı.mp4>"
  python -I build_reel.py ... --stage preview --at 0.3,1.5,3.6   (yalnızca seçili anların karesi)
Aşamalar: assets, frames, audio, mux, all (varsayılan), preview
"""
import argparse
import json
import math
import os
import subprocess

import cv2
import imageio_ffmpeg
import numpy as np
from scipy import signal
from scipy.io import wavfile

FF = imageio_ffmpeg.get_ffmpeg_exe()
HERE = os.path.dirname(os.path.abspath(__file__))
LOGO = os.path.normpath(os.path.join(HERE, '..', '..', 'play_store_icon_512x512.png'))
FPS = 30
W, H = 1080, 1920
SR = 44100
CONT = 9.85                                  # devam bölümünün süresi — reel.html ile aynı
B0, C0, D0, E0 = 0.70, 2.40, 3.15, 7.35      # sahne başlangıçları — reel.html ile aynı
R0 = PER = 0.9375                            # squat tekrarları (2 vuruş @128 BPM)
TREAD = (1.70, 3.34)                         # kaynakta Ersoy'un koşu bandı planı (kapanış arka planı)
FLICK = {                                    # neon yanıp sönme anları (B sahnesine göre) — reel.html ile aynı
    'frame': [0.04, 0.08, 0.12, 0.15, 0.20], 'chip': [0.22, 0.26, 0.30],
    'red': [0.30, 0.33, 0.37, 0.39, 0.44], 'birak': [0.62, 0.66, 0.70, 0.72, 0.78]}


def n_cont():
    return math.ceil(CONT * FPS - 1e-9)


def file_url(p):
    return 'file:///' + os.path.abspath(p).replace('\\', '/')


# ----------------------------------------------------------------------------- assets
def cover(img):
    h, w = img.shape[:2]
    s = max(H / h, W / w)
    nw, nh = round(w * s), round(h * s)
    r = cv2.resize(img, (nw, nh), interpolation=cv2.INTER_LANCZOS4)
    x, y = (nw - W) // 2, (nh - H) // 2
    return r[y:y + H, x:x + W]


def sharpen(img, amt, sigma=1.4):
    return cv2.addWeighted(img, 1 + amt, cv2.GaussianBlur(img, (0, 0), sigma), -amt, 0)


def stage_assets(a):
    out = os.path.join(a.work, 'assets')
    os.makedirs(out, exist_ok=True)
    cap = cv2.VideoCapture(a.src)
    tread, last, last_t = [], None, 0.0
    while True:
        ok, f = cap.read()
        if not ok:
            break
        last_t = cap.get(cv2.CAP_PROP_POS_MSEC) / 1000.0
        last = f
        if TREAD[0] <= last_t <= TREAD[1]:
            name = f'tread_{len(tread):03d}.jpg'
            cv2.imwrite(os.path.join(out, name), sharpen(cover(f), 0.35), [cv2.IMWRITE_JPEG_QUALITY, 93])
            tread.append({'t': round(last_t, 4), 'file': name})
    cv2.imwrite(os.path.join(out, 'last.png'), sharpen(cover(last), 0.5))
    n1 = int(round(last_t * FPS)) + 1          # fps=30'a çevrilmiş orijinal klibin kare sayısı
    with open(os.path.join(out, 'meta.json'), 'w') as fh:
        json.dump({'tread': tread, 'last_t': last_t, 'n1': n1}, fh)
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', '-i', a.src, '-vn', '-ac', '2',
                    '-ar', str(SR), '-c:a', 'pcm_s16le', os.path.join(out, 'orig.wav')], check=True)
    print(f'assets: {len(tread)} koşu bandı karesi, son kare t={last_t:.3f}s, n1={n1}')


def meta(a):
    with open(os.path.join(a.work, 'assets', 'meta.json')) as fh:
        return json.load(fh)


# ----------------------------------------------------------------------------- frames
def open_page(pw, a):
    br = pw.chromium.launch(channel='chrome', headless=True)
    pg = br.new_page(viewport={'width': 540, 'height': 960}, device_scale_factor=2)
    pg.goto(file_url(os.path.join(HERE, 'reel.html')))
    pg.evaluate('cfg => init(cfg)', {'assets': file_url(os.path.join(a.work, 'assets')),
                                     'logo': file_url(LOGO), 'tread': meta(a)['tread']})
    return br, pg


def stage_frames(a):
    from playwright.sync_api import sync_playwright
    fdir = os.path.join(a.work, 'frames')
    os.makedirs(fdir, exist_ok=True)
    with sync_playwright() as pw:
        br, pg = open_page(pw, a)
        pg.evaluate('renderIntro()')
        pg.screenshot(path=os.path.join(a.work, 'intro_cap.png'), omit_background=True)
        pg.evaluate('exitIntro()')
        n = n_cont()
        lo, hi = (float(x) for x in a.range.split(',')) if a.range else (0.0, CONT)
        for i in range(n):
            if not lo <= i / FPS < hi:
                continue
            pg.evaluate('u => renderAt(u)', i / FPS)
            pg.screenshot(path=os.path.join(fdir, f'f_{i:04d}.png'))
            if i % 30 == 0:
                print(f'frames: {i}/{n}', flush=True)
        br.close()
    print(f'frames: {n} kare hazır')


def stage_preview(a):
    from playwright.sync_api import sync_playwright
    pdir = os.path.join(a.work, 'preview')
    os.makedirs(pdir, exist_ok=True)
    ats = [float(x) for x in a.at.split(',')]
    tiles = []
    with sync_playwright() as pw:
        br, pg = open_page(pw, a)
        for u in ats:
            pg.evaluate('u => renderAt(u)', u)
            p = os.path.join(pdir, f'u_{u:05.2f}.png')
            pg.screenshot(path=p)
            im = cv2.imread(p)
            im = cv2.resize(im, (360, 640), interpolation=cv2.INTER_AREA)
            cv2.putText(im, f'u={u:.2f}', (8, 24), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2, cv2.LINE_AA)
            tiles.append(im)
        br.close()
    while len(tiles) % 4:
        tiles.append(np.zeros_like(tiles[0]))
    rows = [np.hstack(tiles[i:i + 4]) for i in range(0, len(tiles), 4)]
    cv2.imwrite(os.path.join(pdir, 'sheet.jpg'), np.vstack(rows), [cv2.IMWRITE_JPEG_QUALITY, 88])
    print('preview:', os.path.join(pdir, 'sheet.jpg'))


# ----------------------------------------------------------------------------- audio
rng = np.random.default_rng(1109)


def tt(d):
    return np.arange(int(round(d * SR))) / SR


def noise(d):
    return rng.standard_normal(int(round(d * SR)))


def filt(x, kind, f, order=2):
    wn = np.array(f) / (SR / 2)
    b, aa = signal.butter(order, wn, kind)
    return signal.lfilter(b, aa, x)


def norm(x):
    m = np.max(np.abs(x))
    return x / m if m > 0 else x


def place(buf, x, t0, gain=1.0, pan=0.0):
    if x.ndim == 1:
        ang = (pan + 1) * math.pi / 4
        x = np.stack([x * math.cos(ang), x * math.sin(ang)], 1) * math.sqrt(2)
    x = x * gain
    i0 = int(round(t0 * SR))
    if i0 < 0:
        x, i0 = x[-i0:], 0
    i1 = min(len(buf), i0 + len(x))
    if i1 > i0:
        buf[i0:i1] += x[:i1 - i0]


def env_ad(t, att, dec):
    return np.minimum(1.0, t / max(att, 1e-6)) * np.exp(-t / dec)


def saw(f, d, cents=0.0):
    t = tt(d)
    return 2 * ((f * 2 ** (cents / 1200) * t + rng.random()) % 1.0) - 1


def kick():
    t = tt(0.40)
    f = 46 + 125 * np.exp(-t / 0.03)
    x = np.sin(2 * np.pi * np.cumsum(f) / SR) * env_ad(t, 0.002, 0.17)
    c = filt(noise(0.006), 'highpass', 2000) * np.exp(-tt(0.006) / 0.0015) * 0.4
    x[:len(c)] += c
    return np.tanh(1.7 * x) / np.tanh(1.7)


def clap():
    t = tt(0.26)
    n = filt(noise(0.26), 'bandpass', [900, 3200])
    e = sum((t >= o) * np.exp(-np.maximum(t - o, 0) / 0.007) * 0.6 for o in (0, 0.011, 0.022))
    e = e + (t >= 0.03) * np.exp(-np.maximum(t - 0.03, 0) / 0.09)
    return norm(n * e)


def hat(d, dec, f):
    return norm(filt(noise(d), 'highpass', f) * np.exp(-tt(d) / dec))


def bass(f, d):
    t = tt(d)
    x = filt(0.7 * saw(f, d) + 0.6 * np.sin(2 * np.pi * f / 2 * t), 'lowpass', 520)
    e = env_ad(t, 0.004, d * 0.9)
    e[-int(0.01 * SR):] *= np.linspace(1, 0, int(0.01 * SR))
    return x * e


def pluck(f):
    d = 0.18
    t = tt(d)
    return filt(saw(f, d) + 0.5 * saw(f, d, 7), 'lowpass', 2900) * env_ad(t, 0.002, 0.06) * 0.6


def pad(chord, d, cents):
    t = tt(d)
    x = sum(saw(f, d, c) for f in chord for c in cents)
    x = filt(x, 'lowpass', 2100) / (len(chord) * len(cents))
    return x * np.minimum(1, t / 0.03) * np.minimum(1, (d - t) / 0.05)


def bell(f, d, dec, partials=((1, 1, 1), (2.0, .55, .5), (2.76, .35, .32), (5.4, .18, .15))):
    t = tt(d)
    return sum(a * np.sin(2 * np.pi * f * r * t) * np.exp(-t / (dec * k)) for r, a, k in partials) * np.minimum(1, t / 0.002)


def whoosh(d, up=True):
    n = noise(d)
    lo, hi = filt(n, 'lowpass', 700), filt(n, 'bandpass', [1800, 7000])
    x = np.linspace(0, 1, len(n))
    mixw = x if up else 1 - x
    return norm(lo * (1 - mixw) + hi * mixw) * np.sin(np.pi * x) ** 2


def riser(d):
    t = tt(d)
    n = noise(d)
    centers = [300, 600, 1200, 2400, 4800, 9000]
    bands = [filt(n, 'bandpass', [c / 1.5, min(c * 1.5, SR / 2 - 100)]) for c in centers]
    pos = np.linspace(0, len(centers) - 1, len(t))
    x = sum(b * np.exp(-((pos - i) ** 2) / 0.8) for i, b in enumerate(bands))
    tone = np.sin(2 * np.pi * np.cumsum(220 * 4 ** (t / d)) / SR) * 0.25
    out = norm(x) * 0.8 + tone
    out *= (t / d) ** 2.2
    out[-int(0.012 * SR):] *= np.linspace(1, 0, int(0.012 * SR))
    return out


def scratch(orig, start):
    """Turntable scratch: müziğin kendisini ileri-geri oynatıp durdurur."""
    segs = [(0.012, 1.0, -3.0), (0.09, -3.0, -3.0), (0.015, -3.0, 3.0), (0.065, 3.0, 3.0), (0.015, 3.0, -3.0),
            (0.065, -3.0, -3.0), (0.015, -3.0, 2.2), (0.12, 2.2, 0.0)]
    rates = np.concatenate([np.linspace(r0, r1, int(d * SR), endpoint=False) for d, r0, r1 in segs])
    rates = np.convolve(rates, np.ones(64) / 64, mode='same')
    pos = np.clip(start * SR + np.cumsum(rates), 0, len(orig) - 2)
    i0 = np.floor(pos).astype(int)
    fr = (pos - i0)[:, None]
    out = orig[i0] * (1 - fr) + orig[i0 + 1] * fr
    fric = filt(noise(len(rates) / SR), 'bandpass', [600, 5000]) * np.abs(rates) / 3 * 0.10
    out = out + fric[:, None]
    out[-int(0.03 * SR):] *= np.linspace(1, 0, int(0.03 * SR))[:, None]
    return out * 1.15


def buzz(d):
    t = tt(d)
    x = sum(np.sin(2 * np.pi * 100 * h * t + rng.random() * 6.28) / h for h in range(1, 13))
    crackle = filt((rng.random(len(t)) < 0.003) * rng.standard_normal(len(t)), 'highpass', 1800) * 3
    return filt(norm(x) + crackle, 'bandpass', [140, 4500])


def stage_audio(a):
    m = meta(a)
    n1 = m['n1']
    t1 = n1 / FPS
    total = (n1 + n_cont()) / FPS
    N = int(round(total * SR))
    sr_in, raw = wavfile.read(os.path.join(a.work, 'assets', 'orig.wav'))
    assert sr_in == SR
    orig = raw.astype(np.float32) / 32768.0
    mix = np.zeros((N, 2))
    k = min(len(orig), int(t1 * SR))
    mix[:k] += orig[:k]
    mix[max(0, k - 220):k] *= np.linspace(1, 0, min(220, k))[:, None]
    loud = np.abs(orig).mean(1)
    orig_rms = float(np.sqrt(np.mean(orig[loud > 0.01] ** 2)))

    sfx = np.zeros((N, 2))
    U = lambda u: t1 + u                                   # devam zamanı -> mutlak zaman
    # A: plak cızırtısı + iğne düşmesi
    place(sfx, scratch(orig, len(orig) / SR - 0.005), U(0.0))
    place(sfx, np.sin(2 * np.pi * 70 * tt(0.12)) * np.exp(-tt(0.12) / 0.05), U(0.40), 0.30)
    # B: geçiş + neon vızıltısı
    place(sfx, whoosh(0.28), U(B0 - 0.10), 0.20)
    bz = buzz(C0 - B0)
    tb = tt(C0 - B0)
    gate = np.zeros_like(tb)
    for times in FLICK.values():
        st = np.zeros_like(tb)
        for x in times:
            st = np.where(tb >= x, 1 - st, st)
        gate = np.maximum(gate, st)
        for j, x in enumerate(times):
            if j % 2 == 0:                                  # her "yanma" anı: tık + kıvılcım
                place(sfx, filt(noise(0.03), 'highpass', 2500) * np.exp(-tt(0.03) / 0.006), U(B0 + x), 0.22, 0.2)
                gate = np.maximum(gate, np.where((tb >= x) & (tb < x + 0.05), 2.2, 0))
    gate = np.convolve(gate, np.ones(130) / 130, mode='same')
    fade = np.minimum(1, (len(tb) - np.arange(len(tb))) / (0.02 * SR))
    place(sfx, bz * gate * 0.12 * fade, U(B0))
    zap = filt(noise(0.06), 'highpass', 1500) * np.exp(-tt(0.06) / 0.015) + 0.5 * np.sin(2 * np.pi * np.cumsum(np.linspace(3000, 800, int(0.06 * SR))) / SR) * np.exp(-tt(0.06) / 0.02)
    place(sfx, zap, U(B0 + 0.56), 0.22)
    place(sfx, np.sin(2 * np.pi * np.cumsum(np.linspace(880, 1320, int(0.12 * SR))) / SR) * env_ad(tt(0.12), 0.005, 0.05), U(B0 + 0.80), 0.10)
    # yükselen gerilim -> drop
    place(sfx, riser(D0 - 1.85), U(1.85), 0.20)
    # C: sahne kesmesi + ampul "ding"
    place(sfx, np.sin(2 * np.pi * np.cumsum(90 * np.exp(-tt(0.18) / 0.08) + 45) / SR) * np.exp(-tt(0.18) / 0.06), U(C0), 0.45)
    place(sfx, bell(1760, 1.0, 0.8), U(C0 + 0.12), 0.20)
    # D: drop
    tdrop = U(D0)
    sub = np.sin(2 * np.pi * np.cumsum(50 + 45 * np.exp(-tt(1.0) / 0.12)) / SR) * np.exp(-tt(1.0) / 0.45)
    place(sfx, sub, tdrop, 0.42)
    crash = np.stack([filt(noise(1.4), 'highpass', 3500), filt(noise(1.4), 'highpass', 3500)], 1) * np.exp(-tt(1.4) / 0.8)[:, None]
    place(sfx, crash / np.max(np.abs(crash)), tdrop, 0.20)
    for off, f in ((0.12, 392.0), (0.30, 329.6)):          # "uzaklaş" uyarı bip'i
        x = filt(np.sign(np.sin(2 * np.pi * f * tt(0.1))), 'lowpass', 2500) * env_ad(tt(0.1), 0.003, 0.08)
        place(sfx, x, U(D0 + off), 0.12)
    place(sfx, whoosh(0.3, up=False), U(D0 + 0.68), 0.16)
    for k_rep, f in enumerate((1318.5, 1568.0, 1760.0), start=1):   # her tekrarda yükselen "ding"
        place(sfx, bell(f, 0.5, 0.25), U(D0 + R0 + k_rep * PER), 0.18)
    # E: kapanış
    place(sfx, whoosh(0.3), U(E0 - 0.12), 0.20)
    for j, f in enumerate((880.0, 1108.7, 1318.5, 1760.0)):
        place(sfx, bell(f, 0.8, 0.35, ((1, 1, 1), (2, .3, .5))), U(E0 + 0.12 + j * 0.06), 0.11)

    # müzik: 128 BPM, Am-F-C-G
    music = np.zeros((N, 2))
    padbus = np.zeros((N, 2))
    beat = 60 / 128
    chords = [[220.0, 261.63, 329.63], [174.61, 220.0, 261.63], [261.63, 329.63, 392.0], [196.0, 246.94, 293.66]]
    roots = [110.0, 87.31, 130.81, 98.0]
    nb = int(math.ceil((total - tdrop) / beat)) + 1
    kicks = []
    for n in range(nb):
        t0 = tdrop + n * beat
        bar = (n // 4) % 4
        kicks.append(t0)
        place(music, kick(), t0, 0.95)
        if n % 4 in (1, 3):
            place(music, clap(), t0, 0.40)
        place(music, hat(0.2, 0.07, 6500), t0 + beat / 2, 0.15, 0.15)
        for q in (1, 3):
            place(music, hat(0.05, 0.012, 7500), t0 + q * beat / 4, 0.09, -0.2)
        place(music, bass(roots[bar], 0.19), t0 + beat / 2, 0.42)
        place(music, bass(roots[bar], 0.10), t0 + 3 * beat / 4, 0.28)
        ch = chords[bar]
        for q, f in enumerate((ch[0] * 2, ch[1] * 2, ch[2] * 2, ch[1] * 2)):
            place(music, pluck(f), t0 + q * beat / 4, 0.08, 0.25 if q % 2 else -0.25)
        if n % 4 == 0:
            d = 4 * beat
            place(padbus, np.stack([pad(ch, d, (-11, 0, 7)), pad(ch, d, (-7, 0, 11))], 1), t0, 0.5)
    tt_all = np.arange(N) / SR
    last_k = np.full(N, -10.0)
    for kt in kicks:
        last_k[tt_all >= kt] = kt
    duck = 1 - 0.65 * np.exp(-(tt_all - last_k) / 0.11)
    music += padbus * duck[:, None]
    music_rms = float(np.sqrt(np.mean(music[int(tdrop * SR):] ** 2)))
    music *= (orig_rms * 1.0) / max(music_rms, 1e-9)
    music = np.tanh(music * 1.1) / 1.1
    fo = int(0.45 * SR)
    music[-fo:] *= np.linspace(1, 0, fo)[:, None]
    sfx[-fo:] *= np.linspace(1, 0.2, fo)[:, None]

    mix += music + sfx * (orig_rms / 0.18)
    peak = float(np.max(np.abs(mix)))
    mix = np.clip(limit(mix), -0.99, 0.99)
    wavfile.write(os.path.join(a.work, 'audio.wav'), SR, (mix * 32767).astype(np.int16))
    win = int(0.5 * SR)
    lv = [20 * math.log10(max(1e-6, float(np.sqrt(np.mean(mix[i:i + win] ** 2))))) for i in range(0, N - win + 1, win)]
    print(f'audio: {total:.3f}s, orig_rms={orig_rms:.3f}, peak(önce)={peak:.2f}, peak(sonra)={np.max(np.abs(mix)):.2f}')
    print('RMS dBFS / 0.5 sn:', ' '.join(f'{v:.0f}' for v in lv))


def limit(x, ceil=0.95, look=0.004, rel=0.08):
    """Basit ileri bakışlı limiter: yalnızca tavanı aşan tepeleri bastırır."""
    from scipy.ndimage import maximum_filter1d, minimum_filter1d, uniform_filter1d
    n = int(look * SR)
    env = maximum_filter1d(np.max(np.abs(x), axis=1), size=2 * n + 1)
    g = minimum_filter1d(np.minimum(1.0, ceil / np.maximum(env, 1e-9)), size=2 * n + 1)
    k = math.exp(-1 / (rel * SR))
    gs = np.minimum(signal.lfilter([1 - k], [1, -k], g, zi=[g[0] * k])[0], g)
    return x * uniform_filter1d(gs, size=int(0.002 * SR))[:, None]


# ----------------------------------------------------------------------------- mux
def stage_mux(a):
    n1 = meta(a)['n1']
    fc = (f'[0:v]scale=-2:{H}:flags=lanczos,crop={W}:{H},setsar=1,fps={FPS},'
          f'tpad=stop_mode=clone:stop_duration=0.5,trim=end_frame={n1},setpts=PTS-STARTPTS[v0];'
          f'[2:v]format=rgba,fade=t=in:st=0.15:d=0.25:alpha=1,fade=t=out:st=8.20:d=0.25:alpha=1[cap];'
          f'[v0][cap]overlay=0:0:shortest=1,format=yuv420p,setsar=1[v0c];'
          f'[1:v]format=yuv420p,setsar=1[v1];'
          f'[v0c][v1]concat=n=2:v=1:a=0[v]')
    cmd = [FF, '-hide_banner', '-loglevel', 'error', '-y',
           '-i', a.src,
           '-framerate', str(FPS), '-i', os.path.join(a.work, 'frames', 'f_%04d.png'),
           '-loop', '1', '-framerate', str(FPS), '-i', os.path.join(a.work, 'intro_cap.png'),
           '-i', os.path.join(a.work, 'audio.wav'),
           '-filter_complex', fc, '-map', '[v]', '-map', '3:a',
           '-c:v', 'libx264', '-preset', 'slow', '-crf', '17', '-profile:v', 'high', '-pix_fmt', 'yuv420p', '-r', str(FPS),
           '-c:a', 'aac', '-b:a', '192k', '-ar', str(SR), '-movflags', '+faststart', '-shortest', a.out]
    subprocess.run(cmd, check=True)
    print('mux:', a.out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--src', required=True)
    ap.add_argument('--work', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--stage', default='all', choices=['all', 'assets', 'frames', 'audio', 'mux', 'preview'])
    ap.add_argument('--at', default='0.3,1.0,1.6,2.0,2.7,3.4,4.3,5.6,6.9,7.6,8.4,9.6')
    ap.add_argument('--range', default='', help='frames aşamasında yalnızca bu aralığı bas: "baş,son" (devam sn)')
    a = ap.parse_args()
    os.makedirs(a.work, exist_ok=True)
    steps = {'assets': [stage_assets], 'frames': [stage_frames], 'audio': [stage_audio], 'mux': [stage_mux],
             'preview': [stage_preview], 'all': [stage_assets, stage_frames, stage_audio, stage_mux]}[a.stage]
    for s in steps:
        s(a)


if __name__ == '__main__':
    main()
