package com.android.vending.expansion.zipfile;

import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import kotlin.UShort;

/* JADX INFO: loaded from: classes.dex */
public class ZipResourceFile {
    static final boolean LOGV = false;
    static final String LOG_TAG = "zipro";
    static final int kCDECRC = 16;
    static final int kCDECommentLen = 32;
    static final int kCDECompLen = 20;
    static final int kCDEExtraLen = 30;
    static final int kCDELen = 46;
    static final int kCDELocalOffset = 42;
    static final int kCDEMethod = 10;
    static final int kCDEModWhen = 12;
    static final int kCDENameLen = 28;
    static final int kCDESignature = 33639248;
    static final int kCDEUncompLen = 24;
    static final int kCompressDeflated = 8;
    static final int kCompressStored = 0;
    static final int kEOCDFileOffset = 16;
    static final int kEOCDLen = 22;
    static final int kEOCDNumEntries = 8;
    static final int kEOCDSignature = 101010256;
    static final int kEOCDSize = 12;
    static final int kLFHExtraLen = 28;
    static final int kLFHLen = 30;
    static final int kLFHNameLen = 26;
    static final int kLFHSignature = 67324752;
    static final int kMaxCommentLen = 65535;
    static final int kMaxEOCDSearch = 65557;
    static final int kZipEntryAdj = 10000;
    private HashMap<String, ZipEntryRO> mHashMap = new HashMap<>();
    public HashMap<File, ZipFile> mZipFiles = new HashMap<>();
    ByteBuffer mLEByteBuffer = ByteBuffer.allocate(4);

    private static int swapEndian(int i) {
        return ((i & 255) << 24) + ((65280 & i) << 8) + ((16711680 & i) >>> 8) + ((i >>> 24) & 255);
    }

    private static int swapEndian(short s) {
        return ((s & 65280) >>> 8) | ((s & 255) << 8);
    }

    public static final class ZipEntryRO {
        public long mCRC32;
        public long mCompressedLength;
        public final File mFile;
        public final String mFileName;
        public long mLocalHdrOffset;
        public int mMethod;
        public long mOffset = -1;
        public long mUncompressedLength;
        public long mWhenModified;
        public final String mZipFileName;

        public ZipEntryRO(String str, File file, String str2) {
            this.mFileName = str2;
            this.mZipFileName = str;
            this.mFile = file;
        }

        public void setOffsetFromFile(RandomAccessFile randomAccessFile, ByteBuffer byteBuffer) throws IOException {
            long j = this.mLocalHdrOffset;
            try {
                randomAccessFile.seek(j);
                randomAccessFile.readFully(byteBuffer.array());
                if (byteBuffer.getInt(0) != ZipResourceFile.kLFHSignature) {
                    Log.w(ZipResourceFile.LOG_TAG, "didn't find signature at start of lfh");
                    throw new IOException();
                }
                this.mOffset = j + 30 + ((long) (byteBuffer.getShort(26) & UShort.MAX_VALUE)) + ((long) (byteBuffer.getShort(28) & UShort.MAX_VALUE));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        public long getOffset() {
            return this.mOffset;
        }

        public boolean isUncompressed() {
            return this.mMethod == 0;
        }

        public AssetFileDescriptor getAssetFileDescriptor() {
            if (this.mMethod != 0) {
                return null;
            }
            try {
                return new AssetFileDescriptor(ParcelFileDescriptor.open(this.mFile, 268435456), getOffset(), this.mUncompressedLength);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getZipFileName() {
            return this.mZipFileName;
        }

        public File getZipFile() {
            return this.mFile;
        }
    }

    public ZipResourceFile(String str) throws IOException {
        addPatchFile(str);
    }

    ZipEntryRO[] getEntriesAt(String str) {
        Vector vector = new Vector();
        Collection<ZipEntryRO> collectionValues = this.mHashMap.values();
        if (str == null) {
            str = "";
        }
        int length = str.length();
        for (ZipEntryRO zipEntryRO : collectionValues) {
            if (zipEntryRO.mFileName.startsWith(str) && -1 == zipEntryRO.mFileName.indexOf(47, length)) {
                vector.add(zipEntryRO);
            }
        }
        return (ZipEntryRO[]) vector.toArray(new ZipEntryRO[vector.size()]);
    }

    public ZipEntryRO[] getAllEntries() {
        Collection<ZipEntryRO> collectionValues = this.mHashMap.values();
        return (ZipEntryRO[]) collectionValues.toArray(new ZipEntryRO[collectionValues.size()]);
    }

    public AssetFileDescriptor getAssetFileDescriptor(String str) {
        ZipEntryRO zipEntryRO = this.mHashMap.get(str);
        if (zipEntryRO != null) {
            return zipEntryRO.getAssetFileDescriptor();
        }
        return null;
    }

    public InputStream getInputStream(String str) throws IOException {
        ZipEntryRO zipEntryRO = this.mHashMap.get(str);
        if (zipEntryRO == null) {
            return null;
        }
        if (zipEntryRO.isUncompressed()) {
            return zipEntryRO.getAssetFileDescriptor().createInputStream();
        }
        ZipFile zipFile = this.mZipFiles.get(zipEntryRO.getZipFile());
        if (zipFile == null) {
            zipFile = new ZipFile(zipEntryRO.getZipFile(), 1);
            this.mZipFiles.put(zipEntryRO.getZipFile(), zipFile);
        }
        ZipEntry entry = zipFile.getEntry(str);
        if (entry != null) {
            return zipFile.getInputStream(entry);
        }
        return null;
    }

    private static int read4LE(RandomAccessFile randomAccessFile) throws IOException {
        return swapEndian(randomAccessFile.readInt());
    }

    void addPatchFile(String str) throws IOException {
        String str2 = str;
        File file = new File(str2);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        long length = randomAccessFile.length();
        if (length < 22) {
            throw new IOException();
        }
        long j = 65557 > length ? length : 65557L;
        randomAccessFile.seek(0L);
        int i = read4LE(randomAccessFile);
        if (i == kEOCDSignature) {
            Log.i(LOG_TAG, "Found Zip archive, but it looks empty");
            throw new IOException();
        }
        if (i != kLFHSignature) {
            Log.v(LOG_TAG, "Not a Zip archive");
            throw new IOException();
        }
        randomAccessFile.seek(length - j);
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate((int) j);
        byte[] bArrArray = byteBufferAllocate.array();
        randomAccessFile.readFully(bArrArray);
        byteBufferAllocate.order(ByteOrder.LITTLE_ENDIAN);
        int length2 = bArrArray.length - 22;
        while (length2 >= 0 && (bArrArray[length2] != 80 || byteBufferAllocate.getInt(length2) != kEOCDSignature)) {
            length2--;
        }
        if (length2 < 0) {
            Log.d(LOG_TAG, "Zip: EOCD not found, " + str2 + " is not zip");
        }
        short s = byteBufferAllocate.getShort(length2 + 8);
        long j2 = 4294967295L;
        long j3 = ((long) byteBufferAllocate.getInt(length2 + 12)) & 4294967295L;
        long j4 = ((long) byteBufferAllocate.getInt(length2 + 16)) & 4294967295L;
        if (j4 + j3 > length) {
            Log.w(LOG_TAG, "bad offsets (dir " + j4 + ", size " + j3 + ", eocd " + length2 + ")");
            throw new IOException();
        }
        if (s == 0) {
            Log.w(LOG_TAG, "empty archive?");
            throw new IOException();
        }
        MappedByteBuffer map = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, j4, j3);
        map.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bArr = new byte[65535];
        ByteBuffer byteBufferAllocate2 = ByteBuffer.allocate(30);
        byteBufferAllocate2.order(ByteOrder.LITTLE_ENDIAN);
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i3 < s) {
            if (map.getInt(i4) != kCDESignature) {
                Log.w(LOG_TAG, "Missed a central dir sig (at " + i4 + ")");
                throw new IOException();
            }
            int i5 = map.getShort(i4 + 28) & UShort.MAX_VALUE;
            int i6 = map.getShort(i4 + 30) & UShort.MAX_VALUE;
            long j5 = j2;
            int i7 = map.getShort(i4 + 32) & UShort.MAX_VALUE;
            map.position(i4 + 46);
            map.get(bArr, i2, i5);
            map.position(i2);
            String str3 = new String(bArr, i2, i5);
            ZipEntryRO zipEntryRO = new ZipEntryRO(str2, file, str3);
            zipEntryRO.mMethod = map.getShort(i4 + 10) & UShort.MAX_VALUE;
            zipEntryRO.mWhenModified = ((long) map.getInt(i4 + 12)) & j5;
            zipEntryRO.mCRC32 = map.getLong(i4 + 16) & j5;
            zipEntryRO.mCompressedLength = map.getLong(i4 + 20) & j5;
            zipEntryRO.mUncompressedLength = map.getLong(i4 + 24) & j5;
            zipEntryRO.mLocalHdrOffset = ((long) map.getInt(i4 + 42)) & j5;
            byteBufferAllocate2.clear();
            zipEntryRO.setOffsetFromFile(randomAccessFile, byteBufferAllocate2);
            this.mHashMap.put(str3, zipEntryRO);
            i4 += i5 + 46 + i6 + i7;
            i3++;
            str2 = str;
            j2 = j5;
            file = file;
            i2 = 0;
        }
    }
}
