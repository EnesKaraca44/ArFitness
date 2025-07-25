# ArFitness: Yapay Zeka Destekli Fitness Koçu



**ArFitness**, kişisel bir yapay zeka fitness koçu olarak hareket eden modern bir Android uygulamasıdır. Cihazınızın kamerasını kullanarak egzersiz formunuzu gerçek zamanlı olarak analiz eder, hareketleri doğru yapmanıza, tekrarlarınızı saymanıza ve gelişiminizi takip etmenize yardımcı olmak için anında geri bildirim sağlar.

---

### ✨ Temel Özellikler

*   **Gerçek Zamanlı Duruş Tespiti:** Google ML Kit kullanarak 33 temel vücut noktasını takip eder ve kamera görüntüsü üzerine canlı bir iskelet çizer.
*   **Yapay Zeka ile Form Düzeltme:** Özel olarak geliştirilmiş egzersiz analizörleri, "Sırtını dik tut!" veya "Daha derine in!" gibi anlık sesli ve yazılı geri bildirimler sunar.
*   **Geniş Egzersiz Kütüphanesi:** Squat, Şınav, Lunge, Bicep Curl, Shoulder Press ve daha fazlasını içeren, sürekli genişleyen bir egzersiz listesi.
*   **Kategorilere Ayrılmış Antrenmanlar:** Kolay gezinme için kas gruplarına göre düzenlenmiş egzersizler.
*   **Animasyonlu Rehberler:** Her egzersizin, doğru tekniği sağlamak için animasyonlu bir GIF ve talimatlar içeren bir detay sayfası vardır.
*   **Antrenman Geçmişi ve İstatistikler:** Tekrar sayıları, süre ve form skoru dahil olmak üzere tamamlanan her antrenman, Room Veritabanı kullanılarak yerel olarak kaydedilir.
*   **Kullanıcı Profili:** Genel ilerlemenizi, toplam antrenman sürenizi takip edin ve haftalık aktivitenizi bir grafikte görüntüleyin.

---

### 🚀 Kullanılan Teknolojiler ve Mimari

Bu proje, modern Android geliştirme en iyi pratikleri ve kütüphaneleri kullanılarak oluşturulmuştur.

*   **Dil:** [Kotlin](https://kotlinlang.org/)
*   **Mimari:** MVVM (Model-View-ViewModel)
*   **Arayüz (UI):** XML ile ViewBinding
*   **Asenkron Programlama:** Kotlin Coroutines & Flow
*   **Bağımlılık Enjeksiyonu (DI):** Hilt
*   **Navigasyon:** Navigation Component ile Safe Args
*   **Veritabanı:** Room Persistence Library
*   **Bilgisayarlı Görü (Computer Vision):**
    *   [Google ML Kit - Pose Detection](https://developers.google.com/ml-kit/vision/pose-detection)
    *   [CameraX](https://developer.android.com/training/camerax)
*   **Resim Yükleme:** Glide (GIF animasyonları için)
*   **Grafik:** MPAndroidChart (İstatistikler için)

---



### 🛠️ Nasıl Çalışır?

1.  **Kamera Girdisi:** `CameraX` API'si, stabil bir görüntü akışı sağlar.
2.  **Duruş Analizi:** Her görüntü karesi, insan vücudu eklem noktalarını bulmak için **ML Kit Pose Detection** modelini kullanan özel bir `PoseAnalyzer` sınıfına iletilir.
3.  **İskelet Çizimi:** Özel bir `PoseOverlayView`, eklem noktası koordinatlarını alır, ekran boyutuna göre ölçeklendirir, ön kamera için yansıtır (aynlar) ve iskeleti kamera önizlemesinin üzerine çizer.
4.  **Egzersiz Mantığı:** Her egzersiz (örn: `SquatAnalyzer`) için, özel bir analizör sınıfı ilgili eklemlerin açılarını ve pozisyonlarını değerlendirerek egzersiz durumunu (örn: YUKARI, AŞAĞI), geçerli tekrarları ve yaygın form hatalarını belirler.
5.  **Geri Bildirim Döngüsü:** Analizörden gelen sonuçlar, `LiveData` aracılığıyla tekrar sayılarını ve geri bildirim mesajlarını güncelleyen `ExerciseViewModel`'e gönderilir. `TextToSpeech` motoru sesli ipuçları sağlar.
6.  **Veri Kalıcılığı:** Tamamlanan antrenmanlar, `Repository` deseni aracılığıyla **Room** veritabanına kaydedilir.

---

### 👨‍💻 Geliştirici

**Enes KARACA **

*   **LinkedIn:** https://www.linkedin.com/in/enes-karaca-277655299/
*   **GitHub:** https://github.com/EnesKaraca44

---
