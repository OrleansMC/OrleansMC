package com.orleansmc.common.servers;

public enum ServerType {
    REALMS_SPAWN,     // Realm oyun modunun başlangıç bölgesi
    REALMS,           // İklimlerin bulunduğu ana oyun alanı
    REALMS_OUTLAND,   // Ana bölgenin dışında keşfedilmemiş veya zorlu alanlar
    PROXY,            // Proxy sunucu
    UNKNOWN;           // Bilinmeyen bir oyun alanı
}