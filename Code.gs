/**
 * Belajar Alkitab Pro - Google Sheets Database Backend Script
 * 
 * Cara Penggunaan:
 * 1. Buka Google Sheets baru atau yang sudah ada di akun Google Anda.
 * 2. Klik menu "Ekstensi" > "Apps Script".
 * 3. Hapus kode default, lalu salin (paste) seluruh kode dari file ini ke editor tersebut.
 * 4. Simpan proyek Apps Script dengan nama "Belajar Alkitab Database".
 * 5. Klik "Terapkan" (Deploy) > "Terapkan Baru" (New Deployment).
 * 6. Pilih Jenis: "Aplikasi Web" (Web App).
 * 7. Konfigurasi Penyiapan:
 *    - Jalankan sebagai: "Saya" (akun email Anda).
 *    - Siapa saja yang memiliki akses: "Siapa saja" (Anyone) - Agar aplikasi Android Anda dapat melakukan POST tanpa login Google paksa.
 * 8. Klik "Terapkan" dan Berikan Izin Akun Google Anda jika diminta (ikuti peringatan Advanced > Go to... jika muncul).
 * 9. Salin URL Aplikasi Web yang diberikan (formatnya berakhir dengan "/exec") dan tempelkan ke kolom URL Google Sheet di menu Pengaturan aplikasi Android Anda!
 */

function doPost(e) {
  var responseHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
    "Content-Type": "application/json"
  };

  try {
    if (!e || !e.postData || !e.postData.contents) {
      return ContentService.createTextOutput(JSON.stringify({
        "status": "error",
        "message": "Tidak ada data payload POST yang dikirimkan."
      }))
      .setMimeType(ContentService.MimeType.JSON)
      .setHeaders(responseHeaders);
    }
    
    // Parsing JSON dari aplikasi Android
    var data = JSON.parse(e.postData.contents);
    var sheetUrl = data.sheetUrl;
    
    var sheet;
    if (sheetUrl && sheetUrl.trim() !== "") {
      // Membuka spreadsheet spesifik berdasarkan URL yang dimasukkan di handphone
      sheet = SpreadsheetApp.openByUrl(sheetUrl);
    } else {
      // Fallback ke spreadsheet tempat script ini ditanam jika URL kosong
      sheet = SpreadsheetApp.getActiveSpreadsheet();
    }
    
    // Dapatkan atau buat tab sheet khusus "Analisis Alkitab"
    var tabName = "Analisis Alkitab";
    var tab = sheet.getSheetByName(tabName);
    if (!tab) {
      tab = sheet.insertSheet(tabName);
      // Membuat header judul kolom di baris pertama
      tab.appendRow([
        "Waktu Input",
        "Referensi Ayat",
        "Teks TB LAI",
        "Teks KJV",
        "Garis Waktu (Timeline)",
        "Sejarah & Latar Belakang",
        "Komparasi Manuskrip",
        "Analogi Modern",
        "Kristosentris Analysis",
        "Referensi Silang",
        "Apologetika Corner",
        "Renungan Praktis",
        "Bahasa Asli Data (JSON)"
      ]);
      // Mempercantik tampilan header tabel
      tab.getRange(1, 1, 1, 13).setFontWeight("bold").setBackground("#1E3A8A").setFontColor("#FFFFFF");
      tab.setFrozenRows(1);
    }
    
    // Menambahkan baris data baru ke spreadsheet
    tab.appendRow([
      data.timestamp || new Date().toLocaleString("id-ID"),
      data.reference,
      data.textTb,
      data.textKjv,
      data.timeline,
      data.sejarah_latar_belakang,
      data.komparasi_manuskrip,
      data.analogi_modern,
      data.kristosentris,
      data.cross_reference,
      data.apologetika_corner,
      data.renungan_praktis,
      data.bahasa_asliJson
    ]);
    
    // Auto-fit kolom agar rapi secara berkala
    try {
      tab.autoResizeColumns(1, 13);
    } catch(err) {
      // Abaikan jika resize gagal
    }

    return ContentService.createTextOutput(JSON.stringify({
      "status": "success",
      "message": "Data analisis untuk ayat " + data.reference + " berhasil disimpan ke spreadsheet!"
    }))
    .setMimeType(ContentService.MimeType.JSON)
    .setHeaders(responseHeaders);
    
  } catch (error) {
    return ContentService.createTextOutput(JSON.stringify({
      "status": "error",
      "message": "Terjadi kesalahan sistem: " + error.toString()
    }))
    .setMimeType(ContentService.MimeType.JSON)
    .setHeaders(responseHeaders);
  }
}

// Menangani permintaan OPTIONS preflight CORS jika diperlukan oleh browser/client
function doOptions(e) {
  return ContentService.createTextOutput("")
    .setMimeType(ContentService.MimeType.TEXT)
    .setHeaders({
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    });
}
