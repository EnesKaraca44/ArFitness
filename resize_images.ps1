Add-Type -AssemblyName System.Drawing

$iconPath = "C:\Users\ENES\.gemini\antigravity\brain\8821e7c7-9803-4ff0-b88b-896470caf20b\arfitness_icon_1790880642610.jpg"
$bannerPath = "C:\Users\ENES\.gemini\antigravity\brain\8821e7c7-9803-4ff0-b88b-896470caf20b\arfitness_banner_1790880742085.jpg"

$outIcon = "C:\Users\ENES\Desktop\Mobil-\ArFitness\play_store_icon_512x512.png"
$outBanner = "C:\Users\ENES\Desktop\Mobil-\ArFitness\play_store_banner_1024x500.png"

# Resize Icon to 512x512
$iconImg = [System.Drawing.Image]::FromFile($iconPath)
$iconBitmap = New-Object System.Drawing.Bitmap(512, 512)
$iconGraphics = [System.Drawing.Graphics]::FromImage($iconBitmap)
$iconGraphics.DrawImage($iconImg, 0, 0, 512, 512)
$iconBitmap.Save($outIcon, [System.Drawing.Imaging.ImageFormat]::Png)
$iconGraphics.Dispose()
$iconBitmap.Dispose()
$iconImg.Dispose()

# Resize/Crop Banner to 1024x500
$bannerImg = [System.Drawing.Image]::FromFile($bannerPath)
$bannerBitmap = New-Object System.Drawing.Bitmap(1024, 500)
$bannerGraphics = [System.Drawing.Graphics]::FromImage($bannerBitmap)
# We will just stretch/resize it to 1024x500 to ensure strict compliance
$bannerGraphics.DrawImage($bannerImg, 0, 0, 1024, 500)
$bannerBitmap.Save($outBanner, [System.Drawing.Imaging.ImageFormat]::Png)
$bannerGraphics.Dispose()
$bannerBitmap.Dispose()
$bannerImg.Dispose()

Write-Output "Done resizing images."
