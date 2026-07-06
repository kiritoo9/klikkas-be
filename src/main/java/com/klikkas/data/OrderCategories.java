package com.klikkas.data;

import java.util.List;

import com.klikkas.dto.order_categories.CategoryAccountTemplate;

public class OrderCategories {

    public static List<CategoryAccountTemplate> get() {
        List<CategoryAccountTemplate> templates = List.of(

                // OPENING BALANCE
                new CategoryAccountTemplate(
                        "Kas Awal",
                        "Kas Awal",
                        "kas_masuk",
                        "",
                        "" 
                ),

                // KAS MASUK
                new CategoryAccountTemplate(
                        "Penjualan Produk",
                        "Pendapatan dari penjualan produk",
                        "kas_masuk",
                        "1000", // Kas
                        "4000" // Pendapatan Penjualan
                ),

                new CategoryAccountTemplate(
                        "Pendapatan Jasa",
                        "Pendapatan dari jasa",
                        "kas_masuk",
                        "1000", // Kas
                        "4100" // Pendapatan Jasa
                ),

                new CategoryAccountTemplate(
                        "Pendapatan Lain-lain",
                        "Pendapatan di luar operasional utama",
                        "kas_masuk",
                        "1000", // Kas
                        "4200" // Pendapatan Lain-lain
                ),

                new CategoryAccountTemplate(
                        "Setoran Modal",
                        "Penambahan modal dari pemilik",
                        "kas_masuk",
                        "1000", // Kas
                        "3000" // Modal Pemilik
                ),

                // KAS KELUAR
                new CategoryAccountTemplate(
                        "Gaji Karyawan",
                        "Pembayaran gaji karyawan",
                        "kas_keluar",
                        "5100", // Beban Gaji
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Listrik",
                        "Pembayaran listrik",
                        "kas_keluar",
                        "5200", // Beban Listrik
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Internet",
                        "Pembayaran internet",
                        "kas_keluar",
                        "5300", // Beban Internet
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Transportasi",
                        "Biaya transportasi",
                        "kas_keluar",
                        "5400", // Beban Transportasi
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Operasional",
                        "Biaya operasional umum",
                        "kas_keluar",
                        "5500", // Beban Operasional
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Administrasi",
                        "Biaya administrasi",
                        "kas_keluar",
                        "5600", // Beban Administrasi
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Pengeluaran Lain-lain",
                        "Biaya lain-lain",
                        "kas_keluar",
                        "5700", // Beban Lain-lain
                        "1000" // Kas
                ),

                new CategoryAccountTemplate(
                        "Prive Pemilik",
                        "Pengambilan dana oleh pemilik",
                        "kas_keluar",
                        "3100", // Prive
                        "1000" // Kas
                ));

        return templates;
    }

}
