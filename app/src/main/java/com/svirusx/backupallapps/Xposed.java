package com.svirusx.backupallapps;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Xposed implements IXposedHookLoadPackage {
    private static int indexOf(byte[] array) {
        final byte[] PATTERN = {48, 74, 4, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 10, 1, 2};
        outer:
        for (int i = 0; i < array.length - PATTERN.length + 1; i++) {
            for (int j = 0; j < PATTERN.length; j++) {
                if (array[i + j] != PATTERN[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            if (!"android".equals(lpparam.packageName)) {
                return;
            }
            XC_MethodHook backupAllApps = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    PackageInfo packageInfo = (PackageInfo) param.getResult();
                    if (packageInfo != null) {
                        int flags = packageInfo.applicationInfo.flags;
                        if ((flags & ApplicationInfo.FLAG_ALLOW_BACKUP) == 0) {
                            flags |= ApplicationInfo.FLAG_ALLOW_BACKUP;
                        }
                        packageInfo.applicationInfo.flags = flags;
                        param.setResult(packageInfo);
                    }

                }
            };
            XposedBridge.hookAllMethods(XposedHelpers.findClass(
                    "com.android.server.pm.PackageManagerService",
                    lpparam.classLoader), "getPackageInfo", backupAllApps);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
