package ru.ilug.aumwindesktop.util;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;

import java.io.File;

public class WindowsApplicationUtil {

    public static ApplicationInfo getFocusedApplicationInfo() {
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();

        String windowClass = getWindowClass(hwnd);
        String exePath = getWindowExecuteFile(hwnd);

        return new ApplicationInfo(exePath, windowClass);
    }

    public static String getWindowClass(WinDef.HWND hwnd) {
        char[] className = new char[256];
        User32.INSTANCE.GetClassName(hwnd, className, className.length);
        return new String(className).trim();
    }

    public static String getWindowExecuteFile(WinDef.HWND hwnd) {
        String path = getWindowExePath(hwnd);
        return getFileFromPath(path);
    }

    public static String getWindowExePath(WinDef.HWND hwnd) {
        IntByReference pid = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);

        Kernel32.HANDLE process = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
                false,
                pid.getValue()
        );

        char[] exePath = new char[4096];
        Psapi.INSTANCE.GetModuleFileNameExW(process, null, exePath, exePath.length);
        return new String(exePath).trim();
    }

    public static String getFileFromPath(String path) {
        File file = new File(path);
        return file.getName();
    }
}
