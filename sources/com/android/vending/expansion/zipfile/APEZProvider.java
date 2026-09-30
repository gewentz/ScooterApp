package com.android.vending.expansion.zipfile;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public abstract class APEZProvider extends ContentProvider {
    public static final int COMPLEN_IDX = 5;
    public static final int COMPTYPE_IDX = 7;
    public static final int CRC_IDX = 4;
    public static final int FILEID_IDX = 0;
    public static final int FILENAME_IDX = 1;
    public static final int MOD_IDX = 3;
    private static final String NO_FILE = "N";
    public static final int UNCOMPLEN_IDX = 6;
    public static final int ZIPFILE_IDX = 2;
    private ZipResourceFile mAPKExtensionFile;
    private boolean mInit;
    public static final String FILEID = "_id";
    public static final String FILENAME = "ZPFN";
    public static final String ZIPFILE = "ZFIL";
    public static final String MODIFICATION = "ZMOD";
    public static final String CRC32 = "ZCRC";
    public static final String COMPRESSEDLEN = "ZCOL";
    public static final String UNCOMPRESSEDLEN = "ZUNL";
    public static final String COMPRESSIONTYPE = "ZTYP";
    public static final String[] ALL_FIELDS = {FILEID, FILENAME, ZIPFILE, MODIFICATION, CRC32, COMPRESSEDLEN, UNCOMPRESSEDLEN, COMPRESSIONTYPE};
    public static final int[] ALL_FIELDS_INT = {0, 1, 2, 3, 4, 5, 6, 7};

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public abstract String getAuthority();

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return "vnd.android.cursor.item/asset";
    }

    private boolean initIfNecessary() {
        int i;
        if (!this.mInit) {
            Context context = getContext();
            PackageManager packageManager = context.getPackageManager();
            ProviderInfo providerInfoResolveContentProvider = packageManager.resolveContentProvider(getAuthority(), 128);
            try {
                int i2 = packageManager.getPackageInfo(context.getPackageName(), 0).versionCode;
                String[] strArr = null;
                if (providerInfoResolveContentProvider.metaData != null) {
                    int i3 = providerInfoResolveContentProvider.metaData.getInt("mainVersion", i2);
                    int i4 = providerInfoResolveContentProvider.metaData.getInt("patchVersion", i2);
                    String string = providerInfoResolveContentProvider.metaData.getString("mainFilename", NO_FILE);
                    if (NO_FILE != string) {
                        String string2 = providerInfoResolveContentProvider.metaData.getString("patchFilename", NO_FILE);
                        if (NO_FILE != string2) {
                            strArr = new String[]{string, string2};
                        } else {
                            strArr = new String[]{string};
                        }
                    }
                    i = i4;
                    i2 = i3;
                } else {
                    i = i2;
                }
                try {
                    if (strArr == null) {
                        this.mAPKExtensionFile = APKExpansionSupport.getAPKExpansionZipFile(context, i2, i);
                    } else {
                        this.mAPKExtensionFile = APKExpansionSupport.getResourceZipFile(strArr);
                    }
                    this.mInit = true;
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (PackageManager.NameNotFoundException e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    @Override // android.content.ContentProvider
    public AssetFileDescriptor openAssetFile(Uri uri, String str) throws FileNotFoundException {
        initIfNecessary();
        String encodedPath = uri.getEncodedPath();
        if (encodedPath.startsWith("/")) {
            encodedPath = encodedPath.substring(1);
        }
        return this.mAPKExtensionFile.getAssetFileDescriptor(encodedPath);
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        initIfNecessary();
        return super.applyBatch(arrayList);
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        initIfNecessary();
        AssetFileDescriptor assetFileDescriptorOpenAssetFile = openAssetFile(uri, str);
        if (assetFileDescriptorOpenAssetFile != null) {
            return assetFileDescriptorOpenAssetFile.getParcelFileDescriptor();
        }
        return null;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        ZipResourceFile.ZipEntryRO[] allEntries;
        int[] iArr;
        initIfNecessary();
        ZipResourceFile zipResourceFile = this.mAPKExtensionFile;
        if (zipResourceFile == null) {
            allEntries = new ZipResourceFile.ZipEntryRO[0];
        } else {
            allEntries = zipResourceFile.getAllEntries();
        }
        if (strArr == null) {
            iArr = ALL_FIELDS_INT;
            strArr = ALL_FIELDS;
        } else {
            int length = strArr.length;
            iArr = new int[length];
            for (int i = 0; i < length; i++) {
                if (strArr[i].equals(FILEID)) {
                    iArr[i] = 0;
                } else if (strArr[i].equals(FILENAME)) {
                    iArr[i] = 1;
                } else if (strArr[i].equals(ZIPFILE)) {
                    iArr[i] = 2;
                } else if (strArr[i].equals(MODIFICATION)) {
                    iArr[i] = 3;
                } else if (strArr[i].equals(CRC32)) {
                    iArr[i] = 4;
                } else if (strArr[i].equals(COMPRESSEDLEN)) {
                    iArr[i] = 5;
                } else if (strArr[i].equals(UNCOMPRESSEDLEN)) {
                    iArr[i] = 6;
                } else if (strArr[i].equals(COMPRESSIONTYPE)) {
                    iArr[i] = 7;
                } else {
                    throw new RuntimeException();
                }
            }
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr, allEntries.length);
        int length2 = iArr.length;
        for (ZipResourceFile.ZipEntryRO zipEntryRO : allEntries) {
            MatrixCursor.RowBuilder rowBuilderNewRow = matrixCursor.newRow();
            for (int i2 = 0; i2 < length2; i2++) {
                switch (iArr[i2]) {
                    case 0:
                        rowBuilderNewRow.add(Integer.valueOf(i2));
                        break;
                    case 1:
                        rowBuilderNewRow.add(zipEntryRO.mFileName);
                        break;
                    case 2:
                        rowBuilderNewRow.add(zipEntryRO.getZipFileName());
                        break;
                    case 3:
                        rowBuilderNewRow.add(Long.valueOf(zipEntryRO.mWhenModified));
                        break;
                    case 4:
                        rowBuilderNewRow.add(Long.valueOf(zipEntryRO.mCRC32));
                        break;
                    case 5:
                        rowBuilderNewRow.add(Long.valueOf(zipEntryRO.mCompressedLength));
                        break;
                    case 6:
                        rowBuilderNewRow.add(Long.valueOf(zipEntryRO.mUncompressedLength));
                        break;
                    case 7:
                        rowBuilderNewRow.add(Integer.valueOf(zipEntryRO.mMethod));
                        break;
                }
            }
        }
        return matrixCursor;
    }
}
