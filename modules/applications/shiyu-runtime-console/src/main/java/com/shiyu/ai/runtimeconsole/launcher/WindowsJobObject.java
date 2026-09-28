package com.shiyu.ai.runtimeconsole.launcher;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/** 独占一个后端进程树，关闭 Job 时只终止已分配的子进程。 */
final class WindowsJobObject implements AutoCloseable {

    private static final int JOB_OBJECT_EXTENDED_LIMIT_INFORMATION = 9;
    private static final int JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE = 0x00002000;
    private static final JobApi API = Native.load("kernel32", JobApi.class, W32APIOptions.DEFAULT_OPTIONS);

    private final WinNT.HANDLE job;

    private WindowsJobObject(WinNT.HANDLE job) {
        this.job = job;
    }

    static WindowsJobObject create() {
        WinNT.HANDLE handle = API.CreateJobObjectW(null, new WString("ShiYuRuntimeConsole-" + ProcessHandle.current().pid()));
        if (handle == null || handle.getPointer() == Pointer.NULL) {
            throw new IllegalStateException("无法创建 Windows Job Object（错误码 " + Kernel32.INSTANCE.GetLastError() + "）");
        }
        ExtendedLimitInformation limits = new ExtendedLimitInformation();
        limits.basicLimitInformation.limitFlags = new WinDef.DWORD(JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE);
        limits.write();
        if (!API.SetInformationJobObject(handle, JOB_OBJECT_EXTENDED_LIMIT_INFORMATION, limits.getPointer(), limits.size())) {
            int error = Kernel32.INSTANCE.GetLastError();
            Kernel32.INSTANCE.CloseHandle(handle);
            throw new IllegalStateException("无法启用受托管进程清理（错误码 " + error + "）");
        }
        return new WindowsJobObject(handle);
    }

    void assign(Process process) {
        int pid = Math.toIntExact(process.pid());
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
                WinNT.PROCESS_SET_QUOTA | WinNT.PROCESS_TERMINATE, false, pid);
        if (processHandle == null || processHandle.getPointer() == Pointer.NULL) {
            throw new IllegalStateException("无法打开后端进程句柄（PID " + pid + "，错误码 " + Kernel32.INSTANCE.GetLastError() + "）");
        }
        try {
            if (!API.AssignProcessToJobObject(job, processHandle)) {
                throw new IllegalStateException("无法将后端进程加入 Job Object（PID " + pid + "，错误码 " + Kernel32.INSTANCE.GetLastError() + "）");
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }

    @Override
    public void close() {
        if (job != null && job.getPointer() != Pointer.NULL) {
            Kernel32.INSTANCE.CloseHandle(job);
        }
    }

    /** 声明 Windows Job Object 所需的内核进程管理调用。 */
    private interface JobApi extends StdCallLibrary {
        /** 创建 Windows Job Object。 */
        WinNT.HANDLE CreateJobObjectW(Pointer securityAttributes, WString name);
        /** 设置 Windows Job Object 的进程限制。 */
        boolean SetInformationJobObject(WinNT.HANDLE job, int informationClass, Pointer information, int informationLength);
        /** 将后端进程加入 Windows Job Object。 */
        boolean AssignProcessToJobObject(WinNT.HANDLE job, WinNT.HANDLE process);
    }

    /** 保存 Windows Job Object 的基础进程限制信息。 */
    @Structure.FieldOrder({"perProcessUserTimeLimit", "perJobUserTimeLimit", "limitFlags", "minimumWorkingSetSize",
            "maximumWorkingSetSize", "activeProcessLimit", "affinity", "priorityClass", "schedulingClass"})
    public static final class BasicLimitInformation extends Structure {
        public long perProcessUserTimeLimit;
        public long perJobUserTimeLimit;
        public WinDef.DWORD limitFlags = new WinDef.DWORD();
        public BaseTSD.SIZE_T minimumWorkingSetSize = new BaseTSD.SIZE_T();
        public BaseTSD.SIZE_T maximumWorkingSetSize = new BaseTSD.SIZE_T();
        public WinDef.DWORD activeProcessLimit = new WinDef.DWORD();
        public BaseTSD.ULONG_PTR affinity = new BaseTSD.ULONG_PTR();
        public WinDef.DWORD priorityClass = new WinDef.DWORD();
        public WinDef.DWORD schedulingClass = new WinDef.DWORD();
    }

    /** 保存 Windows Job Object 的 I/O 计数信息。 */
    @Structure.FieldOrder({"readOperationCount", "writeOperationCount", "otherOperationCount", "readTransferCount",
            "writeTransferCount", "otherTransferCount"})
    public static final class IoCounters extends Structure {
        public long readOperationCount;
        public long writeOperationCount;
        public long otherOperationCount;
        public long readTransferCount;
        public long writeTransferCount;
        public long otherTransferCount;
    }

    /** 组合 Windows Job Object 的限制和 I/O 统计信息。 */
    @Structure.FieldOrder({"basicLimitInformation", "ioInfo", "processMemoryLimit", "jobMemoryLimit",
            "peakProcessMemoryUsed", "peakJobMemoryUsed"})
    public static final class ExtendedLimitInformation extends Structure {
        public BasicLimitInformation basicLimitInformation = new BasicLimitInformation();
        public IoCounters ioInfo = new IoCounters();
        public BaseTSD.SIZE_T processMemoryLimit = new BaseTSD.SIZE_T();
        public BaseTSD.SIZE_T jobMemoryLimit = new BaseTSD.SIZE_T();
        public BaseTSD.SIZE_T peakProcessMemoryUsed = new BaseTSD.SIZE_T();
        public BaseTSD.SIZE_T peakJobMemoryUsed = new BaseTSD.SIZE_T();
    }
}
