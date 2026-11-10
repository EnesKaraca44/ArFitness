# Gerçek sinyal kayıtları

Bu klasördeki `.csv` dosyaları `RecordedSessionsTest` tarafından tekrar oynatılır. Test, uygulamanın
saydığı tekrarın gerçek tekrar sayısına eşit olup olmadığını kontrol eder. Eşikleri değiştirdiğinde
bu test, daha önce doğru sayılan bir kaydın bozulup bozulmadığını gösterir.

## Kayıt nasıl alınır

1. Uygulamayı **debug** olarak telefona yükle (Android Studio > Run). Debug derlemesinde her egzersiz
   seansı arka planda otomatik kaydedilir (ekranda bir şey görünmez). Dosya adı Logcat'te
   `SignalRecorder` etiketiyle yazılır.
2. Hareketi yap ve **gerçek** tekrar sayını not et. "Bitir"e bastığında kayıt kapanır.
3. Dosyayı bilgisayara al:

   ```bash
   adb pull /sdcard/Android/data/com.enes.arfitness/files/recordings/
   ```

   ya da Android Studio > Device Explorer içinden aynı klasörü kullan.
4. Dosyayı gerçek tekrar sayısını içerecek şekilde yeniden adlandırıp bu klasöre koy:

   ```
   <hareket>_<gerçek tekrar>reps[_not].csv
   squat_10reps_yavas.csv
   bicep_curl_12reps_alternatif.csv
   push_up_0reps_sadece_bekleme.csv
   ```

   `<hareket>` kaydın adındaki ön ektir (`squat`, `push_up`, `bicep_curl`, `shoulder_press`, `lunge`,
   `dumbbell_fly`, `bench_press`, `pull_up`, `lat_pulldown`, `hammer_curl`, `cable_pushdown`,
   `lateral_raise`, `front_raise`, `deadlift`). Plank süreli olduğu için bu testte atlanır.

## Önerilen kayıt seti (her hareket için)

- 10 düzgün tekrar
- 10 yarım tekrar (`_0reps` olarak kaydet: hiçbiri sayılmamalı)
- 20–30 sn hareketsiz bekleme ve kadraja girip çıkma (`_0reps`)
- Hızlı tempo ve yavaş tempo

## CSV sütunları

`timestamp_ms, v0, v1, phase, rep_counted, is_holding`. Burada `v0`/`v1` sayaca giden sinyaldir
(tek kanallı hareketlerde yalnızca `v0` dolu, kol hareketlerinde `v0` = sol kol, `v1` = sağ kol).
Boş değer, o karede sinyalin güvenilmez olduğunu gösterir. Eşikleri ayarlarken `v0`/`v1`'in
başlangıç ve hedef pozisyonlarında aldığı değerlere bak; eşikler ilgili `*Analyzer.kt` dosyasındaki
`RepCounter(restThreshold, peakThreshold)` çağrısındadır.
